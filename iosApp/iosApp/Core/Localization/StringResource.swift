import Foundation
import shared

func stringResource(key: String, _ args: Any...) -> String {
    // Simplified implementation - just returns the key as a fallback
    // In a real implementation, this would use CoreStringResourceProvider from Koin
    if args.isEmpty {
        return key
    }
    let argsStr = args.map { String(describing: $0) }.joined(separator: ", ")
    return "\(key)(\(argsStr))"
}
