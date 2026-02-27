import SwiftUI
import shared

@main
struct MunchiesApp: App {
    init() {
        KoinModule_iosKt.doInitKoinIos()
        FeatureRestaurantDiKt.registerFeatureRestaurantModule()
        FeatureSettingsDiKt.registerFeatureSettingsModule()
    }
    
    private var coordinator: AppCoordinator {
        KoinModule_iosKt.getAppCoordinator()
    }
    
    var body: some Scene {
        WindowGroup {
            AppNavigationView(coordinator: coordinator)
                .onOpenURL { url in
                    handleDeepLink(url)
                }
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        guard url.scheme == DeepLinkConstants().SCHEME else { return }
        
        let host = url.host ?? ""
        let path = url.path
        let pathComponents = path.split(separator: "/").map(String.init)
        
        print("🔗 DEBUG: handleDeepLink called")
        print("  URL: \(url)")
        print("  host: \(host)")
        print("  path: \(path)")
        print("  pathComponents: \(pathComponents)")
        
        // Extract query parameters
        var queryParams: [String: String] = [:]
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
           let queryItems = components.queryItems {
            for item in queryItems {
                queryParams[item.name] = item.value ?? ""
            }
        }
        
        print("  queryParams: \(queryParams)")
        
        // Use shared processor for routing
        print("🔗 DEBUG: Calling DeepLinkProcessor.processDeepLink")
        DeepLinkProcessor.shared.processDeepLink(
            host: host,
            pathSegments: pathComponents,
            queryParams: queryParams,
            coordinator: coordinator
        )
        print("🔗 DEBUG: DeepLinkProcessor.processDeepLink completed")
    }
}

extension Array {
    subscript(safe index: Int) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
}
