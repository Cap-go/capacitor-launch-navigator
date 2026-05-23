import XCTest
import MapKit
@testable import LaunchNavigatorPlugin

class LaunchNavigatorTests: XCTestCase {
    func testTeslaShareTextUsesGoogleMapsPosition() {
        let implementation = LaunchNavigator()
        let destination = CLLocationCoordinate2D(latitude: 47.6205, longitude: -122.3493)

        XCTAssertEqual(
            "Space Needle\n\nhttps://maps.google.com/?q=47.620500,-122.349300",
            implementation.teslaShareText(destination: destination, destinationName: "Space Needle")
        )
    }

    func testTeslaShareTextFallsBackToDroppedPinLabel() {
        let implementation = LaunchNavigator()
        let destination = CLLocationCoordinate2D(latitude: 47.6205, longitude: -122.3493)

        XCTAssertEqual(
            "Dropped pin\n\nhttps://maps.google.com/?q=47.620500,-122.349300",
            implementation.teslaShareText(destination: destination, destinationName: "  \n  ")
        )
    }
}
