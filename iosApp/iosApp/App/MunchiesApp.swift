import SwiftUI
import shared

@main
struct MunchiesApp: App {
    init() {
        KoinModule_iosKt.doInitKoinIos()
        FeatureRestaurantDiKt.registerFeatureRestaurantModule()
        FeatureSettingsDiKt.registerFeatureSettingsModule()
    }
    
    @State private var pendingDeepLinkUrl: URL?
    
    private var coordinator: AppCoordinator {
        KoinModule_iosKt.getAppCoordinator()
    }
    
    var body: some Scene {
        WindowGroup {
            AppNavigationView(coordinator: coordinator, pendingDeepLinkUrl: $pendingDeepLinkUrl)
                .onOpenURL { url in
                    handleDeepLink(url)
                }
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        guard url.scheme == DeepLinkConstants().SCHEME else { return }
        
        print("🔗 DEBUG: Deep link received: \(url)")
        pendingDeepLinkUrl = url
    }
}
