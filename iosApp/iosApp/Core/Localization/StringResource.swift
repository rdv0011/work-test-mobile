import Foundation
import shared

let StringResources = IosAggregatorExportsKt.getStringResourcesObject() as? NSObject ?? NSObject()

func stringResource(key: String, _ args: Any...) -> String {
    let localizedString = NSLocalizedString(key, comment: "")
    if localizedString != key {
        return localizedString
    }
    
    if args.isEmpty {
        return key
    }
    let argsStr = args.map { String(describing: $0) }.joined(separator: ", ")
    return "\(key)(\(argsStr))"
}
