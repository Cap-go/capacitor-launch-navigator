import Foundation
import Capacitor
import UIKit
import MapKit

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(LaunchNavigatorPlugin)
public class LaunchNavigatorPlugin: CAPPlugin, CAPBridgedPlugin {
    private let pluginVersion: String = "8.0.23"
    public let identifier = "LaunchNavigatorPlugin"
    public let jsName = "LaunchNavigator"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "navigate", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isAppAvailable", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getAvailableApps", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getSupportedApps", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getDefaultApp", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getAppIcons", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "refreshAppIcons", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "clearIconCache", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPluginVersion", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = LaunchNavigator()
    private let iconCacheQueue = DispatchQueue(label: "app.capgo.plugin.launch_navigator.icons")

    override public func load() {
        super.load()
        implementation.webPathResolver = { [weak self] localURL in
            guard let portable = self?.bridge?.portablePath(fromLocalURL: localURL) else {
                return nil
            }
            return portable.absoluteString
        }
    }

    @objc func navigate(_ call: CAPPluginCall) {
        guard let destination = call.getArray("destination") as? [Double],
              destination.count == 2 else {
            call.reject("Destination coordinates [latitude, longitude] are required")
            return
        }

        let lat = destination[0]
        let lon = destination[1]
        let options = call.getObject("options") ?? [:]

        let app = options["app"] as? String ?? "apple_maps"
        let transportMode = options["transportMode"] as? String ?? "driving"

        var start: CLLocationCoordinate2D?
        if let startCoords = options["start"] as? [Double], startCoords.count == 2 {
            start = CLLocationCoordinate2D(latitude: startCoords[0], longitude: startCoords[1])
        }

        let startName = options["startName"] as? String
        let destinationName = options["destinationName"] as? String

        DispatchQueue.main.async {
            self.implementation.navigate(
                app: app,
                destination: CLLocationCoordinate2D(latitude: lat, longitude: lon),
                start: start,
                startName: startName,
                destinationName: destinationName,
                transportMode: transportMode,
                viewController: self.bridge?.viewController
            ) { success, error in
                if success {
                    call.resolve()
                } else {
                    call.reject(error ?? "Failed to launch navigation app")
                }
            }
        }
    }

    @objc func isAppAvailable(_ call: CAPPluginCall) {
        guard let app = call.getString("app") else {
            call.reject("App identifier is required")
            return
        }

        let available = implementation.isAppAvailable(app: app)
        call.resolve([
            "available": available
        ])
    }

    @objc func getAvailableApps(_ call: CAPPluginCall) {
        let apps = implementation.getAvailableApps()
        call.resolve([
            "apps": apps
        ])
    }

    @objc func getSupportedApps(_ call: CAPPluginCall) {
        let apps = implementation.getSupportedApps()
        call.resolve([
            "apps": apps
        ])
    }

    @objc func getDefaultApp(_ call: CAPPluginCall) {
        call.resolve([
            "app": "apple_maps"
        ])
    }

    @objc func getAppIcons(_ call: CAPPluginCall) {
        let options = call.options as? [String: Any] ?? [:]
        iconCacheQueue.async {
            let result = self.implementation.getAppIcons(options: options, forceRefresh: false)
            DispatchQueue.main.async {
                call.resolve(result)
            }
        }
    }

    @objc func refreshAppIcons(_ call: CAPPluginCall) {
        let options = call.options as? [String: Any] ?? [:]
        iconCacheQueue.async {
            let result = self.implementation.getAppIcons(options: options, forceRefresh: true)
            DispatchQueue.main.async {
                call.resolve(result)
            }
        }
    }

    @objc func clearIconCache(_ call: CAPPluginCall) {
        let options = call.options as? [String: Any] ?? [:]
        iconCacheQueue.async {
            let result = self.implementation.clearIconCache(options: options)
            DispatchQueue.main.async {
                call.resolve(result)
            }
        }
    }

    @objc func getPluginVersion(_ call: CAPPluginCall) {
        call.resolve(["version": self.pluginVersion])
    }

}
