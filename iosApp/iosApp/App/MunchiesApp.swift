import SwiftUI
import shared

@main
struct MunchiesApp: App {
    init() {
        KoinModule_iosKt.doInitKoinIos()
        // register feature modules
        // Try the original generated symbol first; fall back to wrapper if needed
        // Some Kotlin/Native symbol names vary; the wrapper ensures a stable symbol
        if (FeatureRestaurantDiKt.responds(to: Selector(("registerFeatureRestaurantModule")))) {
            FeatureRestaurantDiKt.registerFeatureRestaurantModule()
        } else {
            FeatureRestaurantDiWrapperKt.doRegisterFeatureRestaurantModule()
        }
    }
    
    private var coordinator: AppCoordinator {
        KoinModule_iosKt.getAppCoordinator()
    }
    
    var body: some Scene {
        WindowGroup {
            AppNavigationView(coordinator: coordinator)
        }
    }
}
