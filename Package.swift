// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapgoCapacitorLaunchNavigator",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapgoCapacitorLaunchNavigator",
            targets: ["LaunchNavigatorPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "LaunchNavigatorPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/LaunchNavigatorPlugin"),
        .testTarget(
            name: "LaunchNavigatorPluginTests",
            dependencies: ["LaunchNavigatorPlugin"],
            path: "ios/Tests/LaunchNavigatorPluginTests")
    ]
)
