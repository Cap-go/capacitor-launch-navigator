import Foundation
import UIKit
import MapKit

struct NavigationAppInfo {
    let name: String
    let urlScheme: String
    let url: String
}

// swiftlint:disable type_body_length
@objc public class LaunchNavigator: NSObject {

    public var webPathResolver: ((URL) -> String?)?

    let navigationApps: [String: NavigationAppInfo] = [
        "apple_maps": NavigationAppInfo(name: "Apple Maps", urlScheme: "maps://", url: "https://www.apple.com/maps/"),
        "google_maps": NavigationAppInfo(name: "Google Maps", urlScheme: "comgooglemaps://", url: "https://www.google.com/maps"),
        "waze": NavigationAppInfo(name: "Waze", urlScheme: "waze://", url: "https://www.waze.com"),
        "citymapper": NavigationAppInfo(name: "Citymapper", urlScheme: "citymapper://", url: "https://citymapper.com"),
        "navigon": NavigationAppInfo(name: "Garmin Navigon", urlScheme: "navigon://", url: "https://www.garmin.com"),
        "transit_app": NavigationAppInfo(name: "Transit App", urlScheme: "transit://", url: "https://transitapp.com"),
        "yandex": NavigationAppInfo(name: "Yandex Navigator", urlScheme: "yandexnavi://", url: "https://yandex.com/maps"),
        "uber": NavigationAppInfo(name: "Uber", urlScheme: "uber://", url: "https://www.uber.com"),
        "tomtom": NavigationAppInfo(name: "TomTom", urlScheme: "tomtomgo://", url: "https://www.tomtom.com"),
        "sygic": NavigationAppInfo(name: "Sygic", urlScheme: "com.sygic.aura://", url: "https://www.sygic.com/gps-navigation"),
        "here_maps": NavigationAppInfo(name: "HERE Maps", urlScheme: "here-route://", url: "https://wego.here.com"),
        "moovit": NavigationAppInfo(name: "Moovit", urlScheme: "moovit://", url: "https://moovitapp.com"),
        "lyft": NavigationAppInfo(name: "Lyft", urlScheme: "lyft://", url: "https://www.lyft.com"),
        "maps_me": NavigationAppInfo(name: "MAPS.ME", urlScheme: "mapsme://", url: "https://maps.me"),
        "guru_maps": NavigationAppInfo(name: "Guru Maps", urlScheme: "guru://", url: "https://gurumaps.app"),
        "organic_maps": NavigationAppInfo(name: "Organic Maps", urlScheme: "om://", url: "https://organicmaps.app"),
        "yandex_maps": NavigationAppInfo(name: "Yandex Maps", urlScheme: "yandexmaps://", url: "https://yandex.com/maps"),
        "2gis": NavigationAppInfo(name: "2GIS", urlScheme: "dgis://", url: "https://2gis.com"),
        "cabify": NavigationAppInfo(name: "Cabify", urlScheme: "cabify://", url: "https://cabify.com"),
        "baidu": NavigationAppInfo(name: "Baidu Maps", urlScheme: "baidumap://", url: "https://map.baidu.com"),
        "gaode": NavigationAppInfo(name: "Gaode Maps", urlScheme: "iosamap://", url: "https://www.amap.com"),
        "tesla": NavigationAppInfo(name: "Tesla", urlScheme: "tesla://", url: "https://www.tesla.com"),
        "taxis_99": NavigationAppInfo(name: "99 Taxi", urlScheme: "taxis99://", url: "https://99app.com")
    ]

    let appAliases: [String: String] = [
        "garmin_navigon": "navigon",
        "here": "here_maps",
        "mapsme": "maps_me",
        "99taxi": "taxis_99"
    ]

    // swiftlint:disable:next cyclomatic_complexity function_body_length function_parameter_count
    public func navigate(
        app: String,
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        startName: String?,
        destinationName: String?,
        transportMode: String,
        viewController: UIViewController? = nil,
        completion: @escaping (Bool, String?) -> Void
    ) {
        switch canonicalApp(app) {
        case "apple_maps":
            launchAppleMaps(
                destination: destination,
                start: start,
                startName: startName,
                destinationName: destinationName,
                transportMode: transportMode,
                completion: completion
            )
        case "google_maps":
            launchGoogleMaps(
                destination: destination,
                start: start,
                transportMode: transportMode,
                completion: completion
            )
        case "waze":
            launchWaze(
                destination: destination,
                completion: completion
            )
        case "citymapper":
            launchCitymapper(
                destination: destination,
                start: start,
                completion: completion
            )
        case "navigon":
            launchNavigon(
                destination: destination,
                destinationName: destinationName,
                completion: completion
            )
        case "transit_app":
            launchTransitApp(
                destination: destination,
                start: start,
                completion: completion
            )
        case "uber":
            launchUber(
                destination: destination,
                start: start,
                completion: completion
            )
        case "lyft":
            launchLyft(
                destination: destination,
                completion: completion
            )
        case "moovit":
            launchMoovit(
                destination: destination,
                start: start,
                completion: completion
            )
        case "yandex":
            launchYandex(
                destination: destination,
                start: start,
                completion: completion
            )
        case "sygic":
            launchSygic(
                destination: destination,
                completion: completion
            )
        case "here_maps":
            launchHere(
                destination: destination,
                start: start,
                completion: completion
            )
        case "tomtom":
            launchTomTom(
                destination: destination,
                completion: completion
            )
        case "maps_me":
            launchMapsMe(
                destination: destination,
                completion: completion
            )
        case "guru_maps":
            launchGuruMaps(
                destination: destination,
                start: start,
                transportMode: transportMode,
                completion: completion
            )
        case "organic_maps":
            launchOrganicMaps(
                destination: destination,
                start: start,
                destinationName: destinationName,
                transportMode: transportMode,
                completion: completion
            )
        case "yandex_maps":
            launchYandexMaps(
                destination: destination,
                start: start,
                completion: completion
            )
        case "2gis":
            launch2Gis(
                destination: destination,
                start: start,
                transportMode: transportMode,
                completion: completion
            )
        case "cabify":
            launchCabify(
                destination: destination,
                start: start,
                completion: completion
            )
        case "baidu":
            launchBaidu(
                destination: destination,
                start: start,
                completion: completion
            )
        case "gaode":
            launchGaode(
                destination: destination,
                start: start,
                completion: completion
            )
        case "taxis_99":
            launch99Taxi(
                destination: destination,
                start: start,
                startName: startName,
                destinationName: destinationName,
                completion: completion
            )
        case "tesla":
            shareToTesla(
                destination: destination,
                destinationName: destinationName,
                viewController: viewController,
                completion: completion
            )
        default:
            completion(false, "Unsupported navigation app: \(app)")
        }
    }

    private func launchAppleMaps(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        startName: String?,
        destinationName: String?,
        transportMode: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let mapItem = MKMapItem(placemark: MKPlacemark(coordinate: destination))
        mapItem.name = destinationName

        var options: [String: Any] = [:]

        switch transportMode {
        case "driving":
            options[MKLaunchOptionsDirectionsModeKey] = MKLaunchOptionsDirectionsModeDriving
        case "walking":
            options[MKLaunchOptionsDirectionsModeKey] = MKLaunchOptionsDirectionsModeWalking
        case "bicycling":
            options[MKLaunchOptionsDirectionsModeKey] = "MKLaunchOptionsDirectionsModeCycling"
        case "transit":
            options[MKLaunchOptionsDirectionsModeKey] = MKLaunchOptionsDirectionsModeTransit
        default:
            options[MKLaunchOptionsDirectionsModeKey] = MKLaunchOptionsDirectionsModeDriving
        }

        if let startCoord = start {
            let startItem = MKMapItem(placemark: MKPlacemark(coordinate: startCoord))
            startItem.name = startName
            MKMapItem.openMaps(with: [startItem, mapItem], launchOptions: options)
        } else {
            mapItem.openInMaps(launchOptions: options)
        }

        completion(true, nil)
    }

    private func launchGoogleMaps(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        transportMode: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "comgooglemaps://?daddr=\(destination.latitude),\(destination.longitude)"

        if let startCoord = start {
            urlString += "&saddr=\(startCoord.latitude),\(startCoord.longitude)"
        }

        let modeMap: [String: String] = [
            "driving": "driving",
            "walking": "walking",
            "bicycling": "bicycling",
            "transit": "transit"
        ]

        if let mode = modeMap[transportMode] {
            urlString += "&directionsmode=\(mode)"
        }

        openURL(urlString, completion: completion)
    }

    private func launchWaze(
        destination: CLLocationCoordinate2D,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let urlString = "waze://?ll=\(destination.latitude),\(destination.longitude)&navigate=yes"
        openURL(urlString, completion: completion)
    }

    private func launchCitymapper(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "citymapper://directions?endcoord=\(destination.latitude),\(destination.longitude)"

        if let startCoord = start {
            urlString += "&startcoord=\(startCoord.latitude),\(startCoord.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func launchNavigon(
        destination: CLLocationCoordinate2D,
        destinationName: String?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let name = destinationName.flatMap { $0.trimmedNonEmpty } ?? "Destination"
        let urlString = "navigon://coordinate/\(encoded(name))/\(destination.longitude)/\(destination.latitude)"
        openURL(urlString, completion: completion)
    }

    private func launchTransitApp(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var params: [String] = []

        if let startCoord = start {
            params.append("from=\(startCoord.latitude),\(startCoord.longitude)")
        }

        params.append("to=\(destination.latitude),\(destination.longitude)")
        let urlString = "transit://directions?\(params.joined(separator: "&"))"
        openURL(urlString, completion: completion)
    }

    private func launchUber(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "uber://?action=setPickup&pickup=my_location&dropoff[latitude]=\(destination.latitude)&dropoff[longitude]=\(destination.longitude)"

        if let startCoord = start {
            urlString = "uber://?action=setPickup&pickup[latitude]=\(startCoord.latitude)&pickup[longitude]=\(startCoord.longitude)&dropoff[latitude]=\(destination.latitude)&dropoff[longitude]=\(destination.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func launchLyft(
        destination: CLLocationCoordinate2D,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let urlString = "lyft://ridetype?id=lyft&destination[latitude]=\(destination.latitude)&destination[longitude]=\(destination.longitude)"
        openURL(urlString, completion: completion)
    }

    private func launchMoovit(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "moovit://directions?dest_lat=\(destination.latitude)&dest_lon=\(destination.longitude)"

        if let startCoord = start {
            urlString += "&orig_lat=\(startCoord.latitude)&orig_lon=\(startCoord.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func launchYandex(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let urlString: String
        if let startCoord = start {
            urlString = "yandexnavi://build_route_on_map?lat_from=\(startCoord.latitude)&lon_from=\(startCoord.longitude)&lat_to=\(destination.latitude)&lon_to=\(destination.longitude)"
        } else {
            urlString = "yandexnavi://build_route_on_map?lat_to=\(destination.latitude)&lon_to=\(destination.longitude)"
        }
        openURL(urlString, completion: completion)
    }

    private func launchSygic(
        destination: CLLocationCoordinate2D,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let urlString = "com.sygic.aura://coordinate|\(destination.longitude)|\(destination.latitude)|drive"
        openURL(urlString, completion: completion)
    }

    private func launchHere(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "here-route://\(destination.latitude),\(destination.longitude)"

        if let startCoord = start {
            urlString = "here-route://\(startCoord.latitude),\(startCoord.longitude)/\(destination.latitude),\(destination.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func launchTomTom(
        destination: CLLocationCoordinate2D,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let urlString = "tomtomgo://x-callback-url/navigate?destination=\(destination.latitude),\(destination.longitude)"
        openURL(urlString, completion: completion)
    }

    private func launchMapsMe(
        destination: CLLocationCoordinate2D,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let urlString = "mapsme://map?ll=\(destination.latitude),\(destination.longitude)"
        openURL(urlString, completion: completion)
    }

    private func launchGuruMaps(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        transportMode: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "guru://nav?finish=\(destination.latitude),\(destination.longitude)&mode=\(guruMapsMode(transportMode))&start_navigation=true"

        if let startCoord = start {
            urlString += "&start=\(startCoord.latitude),\(startCoord.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func guruMapsMode(_ transportMode: String) -> String {
        switch transportMode {
        case "walking":
            return "pedestrian"
        case "bicycling":
            return "bicycle"
        default:
            return "auto"
        }
    }

    private func launchOrganicMaps(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        destinationName: String?,
        transportMode: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        let origin = start.map { "\($0.latitude),\($0.longitude)" } ?? "currentLocation"
        var urlString = "om://v2/nav?origin=\(encoded(origin))&destination=\(destination.latitude),\(destination.longitude)&mode=\(organicMapsMode(transportMode))"

        if let destinationName = destinationName, !destinationName.isEmpty {
            urlString += "&destination_name=\(encoded(destinationName))"
        }

        openURL(urlString, completion: completion)
    }

    private func organicMapsMode(_ transportMode: String) -> String {
        switch transportMode {
        case "walking":
            return "pedestrian"
        case "bicycling":
            return "bicycle"
        case "transit":
            return "transit"
        default:
            return "drive"
        }
    }

    private func launchYandexMaps(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "yandexmaps://build_route_on_map/?lat_to=\(destination.latitude)&lon_to=\(destination.longitude)"

        if let startCoord = start {
            urlString += "&lat_from=\(startCoord.latitude)&lon_from=\(startCoord.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func launch2Gis(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        transportMode: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "dgis://2gis.ru/routeSearch/rsType/\(twoGisMode(transportMode))"

        if let startCoord = start {
            urlString += "/from/\(startCoord.longitude),\(startCoord.latitude)"
        }

        urlString += "/to/\(destination.longitude),\(destination.latitude)"
        openURL(urlString, completion: completion)
    }

    private func twoGisMode(_ transportMode: String) -> String {
        switch transportMode {
        case "walking":
            return "pedestrian"
        case "transit":
            return "ctx"
        default:
            return "car"
        }
    }

    private func launchCabify(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "cabify://rideto?lat=\(destination.latitude)&lng=\(destination.longitude)"

        if let startCoord = start {
            urlString = "cabify://ride?pickup[latitude]=\(startCoord.latitude)&pickup[longitude]=\(startCoord.longitude)&dropoff[latitude]=\(destination.latitude)&dropoff[longitude]=\(destination.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func launchBaidu(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "baidumap://map/direction?destination=\(destination.latitude),\(destination.longitude)&mode=driving"

        if let startCoord = start {
            urlString += "&origin=\(startCoord.latitude),\(startCoord.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func launchGaode(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        var urlString = "iosamap://path?dlat=\(destination.latitude)&dlon=\(destination.longitude)&dev=0&t=0"

        if let startCoord = start {
            urlString += "&slat=\(startCoord.latitude)&slon=\(startCoord.longitude)"
        }

        openURL(urlString, completion: completion)
    }

    private func launch99Taxi(
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        startName: String?,
        destinationName: String?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        guard let startCoord = start else {
            completion(false, "Start location is required for 99 Taxi")
            return
        }

        let pickupTitle = encoded(startName.flatMap { $0.trimmedNonEmpty } ?? "Pickup")
        let dropoffTitle = encoded(destinationName.flatMap { $0.trimmedNonEmpty } ?? "Dropoff")
        let urlString = "taxis99://call?pickup_latitude=\(startCoord.latitude)&pickup_longitude=\(startCoord.longitude)&pickup_title=\(pickupTitle)&dropoff_latitude=\(destination.latitude)&dropoff_longitude=\(destination.longitude)&dropoff_title=\(dropoffTitle)&deep_link_product_id=316&client_id=MAP_123"

        openURL(urlString, completion: completion)
    }

    private func shareToTesla(
        destination: CLLocationCoordinate2D,
        destinationName: String?,
        viewController: UIViewController?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        guard let viewController = viewController else {
            completion(false, "Unable to present share sheet")
            return
        }

        let activityViewController = UIActivityViewController(
            activityItems: [teslaShareText(destination: destination, destinationName: destinationName)],
            applicationActivities: nil
        )
        activityViewController.popoverPresentationController?.sourceView = viewController.view
        activityViewController.completionWithItemsHandler = { _, completed, _, error in
            if let error = error {
                completion(false, error.localizedDescription)
            } else if completed {
                completion(true, nil)
            } else {
                completion(false, "Share cancelled")
            }
        }

        viewController.present(activityViewController, animated: true)
    }

    func teslaShareText(destination: CLLocationCoordinate2D, destinationName: String?) -> String {
        "\(teslaShareLabel(destinationName))\n\n\(googleMapsPositionURLString(destination: destination))"
    }

    func googleMapsPositionURLString(destination: CLLocationCoordinate2D) -> String {
        let coordinates = String(
            format: "%.6f,%.6f",
            locale: Locale(identifier: "en_US_POSIX"),
            destination.latitude,
            destination.longitude
        )
        return "https://maps.google.com/?q=\(coordinates)"
    }

    private func teslaShareLabel(_ destinationName: String?) -> String {
        guard let destinationName = destinationName else {
            return "Dropped pin"
        }

        let normalizedName = destinationName
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "[\\r\\n]+", with: " ", options: .regularExpression)
        return normalizedName.isEmpty ? "Dropped pin" : normalizedName
    }

    private func encoded(_ value: String) -> String {
        var allowed = CharacterSet.urlQueryAllowed
        allowed.remove(charactersIn: "&=+/?#")
        return value.addingPercentEncoding(withAllowedCharacters: allowed) ?? value
    }

    private func openURL(_ urlString: String, completion: @escaping (Bool, String?) -> Void) {
        guard let url = URL(string: urlString) else {
            completion(false, "Invalid URL")
            return
        }

        if UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url, options: [:]) { success in
                if success {
                    completion(true, nil)
                } else {
                    completion(false, "Failed to open URL")
                }
            }
        } else {
            completion(false, "App not installed or URL scheme not supported")
        }
    }

    func canonicalApp(_ app: String) -> String {
        appAliases[app] ?? app
    }

    public func isAppAvailable(app: String) -> Bool {
        guard let appInfo = navigationApps[canonicalApp(app)] else {
            return false
        }

        guard let url = URL(string: appInfo.urlScheme) else {
            return false
        }

        return UIApplication.shared.canOpenURL(url)
    }

    public func getAvailableApps() -> [[String: Any]] {
        var availableApps: [[String: Any]] = []

        for (key, value) in navigationApps {
            let available = isAppAvailable(app: key)
            availableApps.append([
                "app": key,
                "name": value.name,
                "available": available
            ])
        }

        return availableApps
    }

    public func getSupportedApps() -> [String] {
        return Array(navigationApps.keys)
    }
}

private extension String {
    var trimmedNonEmpty: String? {
        let trimmed = trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? nil : trimmed
    }
}
// swiftlint:enable type_body_length
