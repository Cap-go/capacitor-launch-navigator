import Foundation
import UIKit
import MapKit

@objc public class LaunchNavigator: NSObject {

    private let navigationApps: [String: (name: String, urlScheme: String)] = [
        "apple_maps": ("Apple Maps", "maps://"),
        "google_maps": ("Google Maps", "comgooglemaps://"),
        "waze": ("Waze", "waze://"),
        "citymapper": ("Citymapper", "citymapper://"),
        "garmin_navigon": ("Garmin Navigon", "navigon://"),
        "transit_app": ("Transit App", "transit://"),
        "yandex": ("Yandex Navigator", "yandexnavi://"),
        "uber": ("Uber", "uber://"),
        "tomtom": ("TomTom", "tomtomgo://"),
        "sygic": ("Sygic", "com.sygic.aura://"),
        "here": ("HERE Maps", "here-route://"),
        "moovit": ("Moovit", "moovit://"),
        "lyft": ("Lyft", "lyft://"),
        "mapsme": ("MAPS.ME", "mapsme://"),
        "cabify": ("Cabify", "cabify://"),
        "baidu": ("Baidu Maps", "baidumap://"),
        "gaode": ("Gaode Maps", "iosamap://"),
        "99taxi": ("99 Taxi", "99app://")
    ]

    public func navigate(
        app: String,
        destination: CLLocationCoordinate2D,
        start: CLLocationCoordinate2D?,
        startName: String?,
        destinationName: String?,
        transportMode: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        switch app {
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
        case "here":
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
        case "mapsme":
            launchMapsMe(
                destination: destination,
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

    public func isAppAvailable(app: String) -> Bool {
        guard let appInfo = navigationApps[app] else {
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