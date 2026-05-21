import Foundation

private let defaultIconCacheMaxAgeMs = 24.0 * 60.0 * 60.0 * 1000.0
private let iconRequestTimeout: TimeInterval = 8
private let maxIconBytes = 2 * 1024 * 1024
private let maxHTMLBytes = 512 * 1024
private let iconCacheDirectoryName = "LaunchNavigatorIcons"

private struct IconProvider {
    let app: String
    let name: String?
    let url: String?
    let iconUrl: String?
}

private struct CachedIcon {
    let fileURL: URL
    let metadata: [String: Any]
}

private struct DownloadedIcon {
    let data: Data
    let sourceUrl: String
    let mimeType: String?
}

private struct IconDownloadError: LocalizedError {
    let message: String

    var errorDescription: String? {
        message
    }
}

extension LaunchNavigator {
    func getAppIcons(options: [String: Any], forceRefresh forceRefreshOverride: Bool) -> [String: Any] {
        let maxAgeMs = maxAge(from: options)
        let forceRefresh = forceRefreshOverride || (options["forceRefresh"] as? Bool ?? false)
        var icons: [[String: Any]] = []
        var failures: [[String: Any]] = []

        for provider in resolveIconProviders(options: options) {
            do {
                icons.append(try resolveProviderIcon(provider: provider, maxAgeMs: maxAgeMs, forceRefresh: forceRefresh))
            } catch {
                failures.append(iconFailure(provider: provider, error: error))
            }
        }

        return [
            "icons": icons,
            "failures": failures
        ]
    }

    func clearIconCache(options: [String: Any]) -> [String: Any] {
        var cleared = 0

        if let apps = options["apps"] as? [String], !apps.isEmpty {
            for app in apps {
                cleared += deleteCachedFiles(app: app)
            }
        } else if let files = try? FileManager.default.contentsOfDirectory(
            at: iconCacheDirectory(),
            includingPropertiesForKeys: nil
        ) {
            for file in files where deleteFileIfExists(file) {
                cleared += 1
            }
        }

        return ["cleared": cleared]
    }

    private func resolveProviderIcon(
        provider: IconProvider,
        maxAgeMs: Double,
        forceRefresh: Bool
    ) throws -> [String: Any] {
        let cachedIcon = readCachedIcon(app: provider.app)
        let now = Date().timeIntervalSince1970 * 1000

        if let cachedIcon = cachedIcon,
           !forceRefresh,
           let fetchedAt = cachedIcon.metadata["fetchedAt"] as? Double,
           now - fetchedAt < maxAgeMs {
            return iconObject(cachedIcon: cachedIcon, fromCache: true, stale: false)
        }

        do {
            let downloadedIcon = try downloadIcon(provider: provider)
            let iconFileURL = try writeIcon(app: provider.app, downloadedIcon: downloadedIcon)
            var metadata: [String: Any] = [
                "app": provider.app,
                "sourceUrl": downloadedIcon.sourceUrl,
                "fetchedAt": now,
                "fileName": iconFileURL.lastPathComponent
            ]
            if let name = provider.name, !name.isEmpty {
                metadata["name"] = name
            }
            if let mimeType = downloadedIcon.mimeType, !mimeType.isEmpty {
                metadata["mimeType"] = mimeType
            }
            try writeMetadata(metadata, app: provider.app)
            return iconObject(cachedIcon: CachedIcon(fileURL: iconFileURL, metadata: metadata), fromCache: false, stale: false)
        } catch {
            if let cachedIcon = cachedIcon {
                return iconObject(cachedIcon: cachedIcon, fromCache: true, stale: true)
            }
            throw error
        }
    }

    private func iconObject(cachedIcon: CachedIcon, fromCache: Bool, stale: Bool) -> [String: Any] {
        var icon: [String: Any] = [
            "app": cachedIcon.metadata["app"] as? String ?? "",
            "localUrl": webPath(for: cachedIcon.fileURL),
            "sourceUrl": cachedIcon.metadata["sourceUrl"] as? String ?? "",
            "fetchedAt": cachedIcon.metadata["fetchedAt"] as? Double ?? 0,
            "fromCache": fromCache,
            "stale": stale
        ]

        if let name = cachedIcon.metadata["name"] as? String, !name.isEmpty {
            icon["name"] = name
        }
        if let mimeType = cachedIcon.metadata["mimeType"] as? String, !mimeType.isEmpty {
            icon["mimeType"] = mimeType
        }

        return icon
    }

    private func iconFailure(provider: IconProvider, error: Error) -> [String: Any] {
        var failure: [String: Any] = [
            "app": provider.app,
            "message": error.localizedDescription
        ]

        if let name = provider.name, !name.isEmpty {
            failure["name"] = name
        }
        if let sourceUrl = provider.iconUrl ?? provider.url, !sourceUrl.isEmpty {
            failure["sourceUrl"] = sourceUrl
        }

        return failure
    }

    private func downloadIcon(provider: IconProvider) throws -> DownloadedIcon {
        let sourceUrl: URL
        if let iconUrl = provider.iconUrl, !iconUrl.isEmpty {
            sourceUrl = try resolvedURL(iconUrl, relativeTo: provider.url)
        } else {
            guard let providerUrl = provider.url, !providerUrl.isEmpty else {
                throw IconDownloadError(message: "Provider url or iconUrl is required")
            }
            sourceUrl = try discoverIconURL(providerUrl)
        }

        let (data, response) = try fetch(sourceUrl, maxBytes: maxIconBytes)
        let mimeType = normalizeMimeType(response.mimeType)
        guard isSupportedImageResponse(mimeType: mimeType, sourceUrl: response.url ?? sourceUrl) else {
            throw IconDownloadError(message: "Icon response is not an image")
        }

        return DownloadedIcon(data: data, sourceUrl: (response.url ?? sourceUrl).absoluteString, mimeType: mimeType)
    }

    private func discoverIconURL(_ providerUrl: String) throws -> URL {
        let pageURL = try resolvedURL(providerUrl, relativeTo: nil)
        let (data, response) = try fetch(pageURL, maxBytes: maxHTMLBytes)
        let html = String(data: data, encoding: .utf8) ?? String(data: data, encoding: .ascii) ?? ""
        let baseURL = response.url ?? pageURL

        if let iconPath = firstIconHref(in: html) {
            return try resolvedURL(iconPath, relativeTo: baseURL.absoluteString)
        }

        return try resolvedURL("/favicon.ico", relativeTo: baseURL.absoluteString)
    }

    private func fetch(_ url: URL, maxBytes: Int) throws -> (Data, URLResponse) {
        var request = URLRequest(url: url)
        request.timeoutInterval = iconRequestTimeout
        request.setValue("CapgoLaunchNavigator/8", forHTTPHeaderField: "User-Agent")

        let semaphore = DispatchSemaphore(value: 0)
        var result: Result<(Data, URLResponse), Error>?
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            defer { semaphore.signal() }
            result = self.responseResult(data: data, response: response, error: error, maxBytes: maxBytes)
        }
        task.resume()

        if semaphore.wait(timeout: .now() + iconRequestTimeout + 2) == .timedOut {
            task.cancel()
            throw IconDownloadError(message: "Request timed out")
        }

        guard let result = result else {
            throw IconDownloadError(message: "Request failed")
        }
        return try result.get()
    }

    private func responseResult(
        data: Data?,
        response: URLResponse?,
        error: Error?,
        maxBytes: Int
    ) -> Result<(Data, URLResponse), Error> {
        if let error = error {
            return .failure(error)
        }
        guard let data = data, let response = response else {
            return .failure(IconDownloadError(message: "Empty response"))
        }
        guard data.count <= maxBytes else {
            return .failure(IconDownloadError(message: "Response is too large"))
        }
        if let httpResponse = response as? HTTPURLResponse,
           httpResponse.statusCode < 200 || httpResponse.statusCode >= 300 {
            return .failure(IconDownloadError(message: "Request failed with status \(httpResponse.statusCode)"))
        }
        return .success((data, response))
    }

    private func firstIconHref(in html: String) -> String? {
        let linkPattern = #"<link\s+[^>]*rel=["'][^"']*(?:apple-touch-icon|icon)[^"']*["'][^>]*>"#
        let hrefPattern = #"\shref=["']([^"']+)["']"#

        guard let linkRegex = try? NSRegularExpression(pattern: linkPattern, options: [.caseInsensitive]),
              let hrefRegex = try? NSRegularExpression(pattern: hrefPattern, options: [.caseInsensitive]) else {
            return nil
        }

        let range = NSRange(html.startIndex..<html.endIndex, in: html)
        guard let linkMatch = linkRegex.firstMatch(in: html, range: range),
              let linkRange = Range(linkMatch.range, in: html) else {
            return nil
        }

        let linkTag = String(html[linkRange])
        let hrefRange = NSRange(linkTag.startIndex..<linkTag.endIndex, in: linkTag)
        guard let hrefMatch = hrefRegex.firstMatch(in: linkTag, range: hrefRange),
              hrefMatch.numberOfRanges > 1,
              let valueRange = Range(hrefMatch.range(at: 1), in: linkTag) else {
            return nil
        }

        return String(linkTag[valueRange])
    }

    private func resolvedURL(_ value: String, relativeTo baseUrl: String?) throws -> URL {
        if let baseUrl = baseUrl,
           let base = URL(string: baseUrl),
           let url = URL(string: value, relativeTo: base)?.absoluteURL {
            return url
        }

        guard let url = URL(string: value) else {
            throw IconDownloadError(message: "Invalid icon URL: \(value)")
        }
        return url
    }

    private func writeIcon(app: String, downloadedIcon: DownloadedIcon) throws -> URL {
        _ = deleteCachedFiles(app: app)
        let fileName = cacheKey(app) + iconExtension(mimeType: downloadedIcon.mimeType, sourceUrl: downloadedIcon.sourceUrl)
        let fileURL = iconCacheDirectory().appendingPathComponent(fileName)
        try FileManager.default.createDirectory(at: iconCacheDirectory(), withIntermediateDirectories: true)
        try downloadedIcon.data.write(to: fileURL, options: .atomic)
        return fileURL
    }

    private func readCachedIcon(app: String) -> CachedIcon? {
        let metadataURL = metadataURL(app: app)
        guard let data = try? Data(contentsOf: metadataURL),
              let metadata = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let fileName = metadata["fileName"] as? String else {
            return nil
        }

        let fileURL = iconCacheDirectory().appendingPathComponent(fileName)
        guard FileManager.default.fileExists(atPath: fileURL.path) else {
            return nil
        }

        return CachedIcon(fileURL: fileURL, metadata: metadata)
    }

    private func writeMetadata(_ metadata: [String: Any], app: String) throws {
        let data = try JSONSerialization.data(withJSONObject: metadata)
        try FileManager.default.createDirectory(at: iconCacheDirectory(), withIntermediateDirectories: true)
        try data.write(to: metadataURL(app: app), options: .atomic)
    }

    private func iconCacheDirectory() -> URL {
        FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0].appendingPathComponent(
            iconCacheDirectoryName,
            isDirectory: true
        )
    }

    private func metadataURL(app: String) -> URL {
        iconCacheDirectory().appendingPathComponent(cacheKey(app) + ".json")
    }

    private func deleteCachedFiles(app: String) -> Int {
        let prefix = cacheKey(app) + "."
        guard let files = try? FileManager.default.contentsOfDirectory(
            at: iconCacheDirectory(),
            includingPropertiesForKeys: nil
        ) else {
            return 0
        }

        var deleted = 0
        for file in files where file.lastPathComponent.hasPrefix(prefix) && deleteFileIfExists(file) {
            deleted += 1
        }
        return deleted
    }

    private func deleteFileIfExists(_ fileURL: URL) -> Bool {
        do {
            try FileManager.default.removeItem(at: fileURL)
            return true
        } catch {
            return false
        }
    }

    private func webPath(for fileURL: URL) -> String {
        webPathResolver?(fileURL) ?? fileURL.absoluteString
    }

    private func maxAge(from options: [String: Any]) -> Double {
        if let value = options["maxAgeMs"] as? Double, value >= 0 {
            return value
        }
        if let value = options["maxAgeMs"] as? NSNumber, value.doubleValue >= 0 {
            return value.doubleValue
        }
        return defaultIconCacheMaxAgeMs
    }

    private func resolveIconProviders(options: [String: Any]) -> [IconProvider] {
        var providers: [String: IconProvider] = [:]

        for (app, appInfo) in navigationApps {
            providers[app] = IconProvider(app: app, name: appInfo.name, url: appInfo.url, iconUrl: nil)
        }

        let customProviders = options["providers"] as? [[String: Any]] ?? []
        for providerObject in customProviders {
            guard let app = providerObject["app"] as? String, !app.isEmpty else {
                continue
            }

            let existing = providers[app]
            providers[app] = IconProvider(
                app: app,
                name: providerObject["name"] as? String ?? existing?.name,
                url: providerObject["url"] as? String ?? existing?.url,
                iconUrl: providerObject["iconUrl"] as? String ?? existing?.iconUrl
            )
        }

        if let apps = options["apps"] as? [String], !apps.isEmpty {
            return apps.map { app in
                providers[app] ?? IconProvider(app: app, name: nil, url: nil, iconUrl: nil)
            }
        }

        if !customProviders.isEmpty {
            return customProviders.compactMap { providerObject in
                guard let app = providerObject["app"] as? String else {
                    return nil
                }
                return providers[app]
            }
        }

        return navigationApps.keys.compactMap { providers[$0] }
    }

    private func normalizeMimeType(_ mimeType: String?) -> String? {
        mimeType?.components(separatedBy: ";").first?.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    }

    private func isSupportedImageResponse(mimeType: String?, sourceUrl: URL) -> Bool {
        guard let mimeType = mimeType, !mimeType.isEmpty else {
            return hasKnownImageExtension(sourceUrl)
        }

        return mimeType.hasPrefix("image/") || (mimeType == "application/octet-stream" && hasKnownImageExtension(sourceUrl))
    }

    private func hasKnownImageExtension(_ sourceUrl: URL) -> Bool {
        let path = sourceUrl.path.lowercased()
        return [".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".ico"].contains { path.hasSuffix($0) }
    }

    private func iconExtension(mimeType: String?, sourceUrl: String) -> String {
        switch mimeType {
        case "image/jpeg":
            return ".jpg"
        case "image/png":
            return ".png"
        case "image/gif":
            return ".gif"
        case "image/webp":
            return ".webp"
        case "image/svg+xml":
            return ".svg"
        case "image/x-icon", "image/vnd.microsoft.icon":
            return ".ico"
        default:
            let lowerUrl = sourceUrl.lowercased()
            for ext in [".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".ico"] where lowerUrl.hasSuffix(ext) {
                return ext == ".jpeg" ? ".jpg" : ext
            }
            return ".img"
        }
    }

    private func cacheKey(_ app: String) -> String {
        let safeApp = app.map { character in
            character.isLetter || character.isNumber || character == "." || character == "_" || character == "-" ? character : "_"
        }
        return String(safeApp) + "_" + stableHash(app)
    }

    private func stableHash(_ value: String) -> String {
        var hash: UInt64 = 5381
        for byte in value.utf8 {
            hash = ((hash << 5) &+ hash) &+ UInt64(byte)
        }
        return String(format: "%016llx", hash)
    }
}
