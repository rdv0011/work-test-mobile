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
        guard url.scheme == DeepLinkConstants.scheme else { return }
        
        let host = url.host ?? ""
        let path = url.path
        let pathComponents = path.split(separator: "/").map(String.init)
        
        // Extract query parameters
        var queryParams: [String: String] = [:]
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
           let queryItems = components.queryItems {
            for item in queryItems {
                queryParams[item.name] = item.value ?? ""
            }
        }
        
        // Use shared processor for routing
        DeepLinkProcessor.shared.processDeepLink(
            host: host,
            pathSegments: pathComponents,
            queryParams: queryParams,
            coordinator: coordinator
        )
    }
}

extension Array {
    subscript(safe index: Int) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
}
