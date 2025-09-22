import Foundation

@objc public class LaunchNavigator: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
