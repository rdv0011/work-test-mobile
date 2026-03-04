import SwiftUI
import shared
import FirebaseCore

private let tag = "MunchiesApp"

@main
struct MunchiesApp: App {
    init() {
//        FirebaseApp.configure()
         
        KoinModule_iosKt.doInitKoinIos()
        logInfo(tag: tag, message: "✅ Koin initialized")
        
        FeatureRestaurantDiKt.registerFeatureRestaurantModule()
        logInfo(tag: tag, message: "✅ Feature-restaurant module registered")
        KoinModule_iosKt.debugKoinState(tag: "after-restaurant")
        
        FeatureSettingsDiKt.registerFeatureSettingsModule()
        logInfo(tag: tag, message: "✅ Feature-settings module registered")
        KoinModule_iosKt.debugKoinState(tag: "after-settings")
        
        _ = KoinModule_iosKt.createAppCoordinator()
        logInfo(tag: tag, message: "✅ AppCoordinator created")
        
        _ = KoinModule_iosKt.getAnalyticsService()
        _ = FirebaseAnalyticsService()
        logInfo(tag: tag, message: "✅ Firebase Analytics initialized")
         
        AnalyticsModuleIosKt.startAnalyticsTracking()
        logInfo(tag: tag, message: "✅ Analytics tracking started")
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
         guard url.scheme == DeepLinkConstants().SCHEME else {
             logInfo(tag: tag, message: "🔗 Ignoring URL with scheme: \(url.scheme ?? "nil")")
             return
         }
         
         logInfo(tag: tag, message: "🔗 handleDeepLink() received: \(url)")
         pendingDeepLinkUrl = url
         logInfo(tag: tag, message: "🔗 pendingDeepLinkUrl set to: \(String(describing: pendingDeepLinkUrl))")
     }
}
