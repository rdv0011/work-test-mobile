import SwiftUI
import shared

// MARK: - Type Aliases (Kotlin Native name mappings)
typealias RestaurantNavigationViewModel = Feature_restaurantRestaurantNavigationViewModel
typealias RestaurantListViewModel = Feature_restaurantRestaurantListViewModel
typealias RestaurantDetailViewModel = Feature_restaurantRestaurantDetailViewModel
typealias SettingsViewModel = Feature_settingsSettingsViewModel

@main
struct MunchiesApp: App {
    @State private var pendingDeepLinkUrl: URL?
    @State private var isInitialized = false
    @State private var coordinator: CoreAppCoordinator?
    
    init() {
        // Initialize Koin framework first
        IosAggregatorExportsKt.doInitKoinFramework()
        
        // Then get the coordinator from Koin
        let appCoordinator = IosAggregatorExportsKt.getAppCoordinatorFromFramework()
        
        // Use _coordinator to directly set the State value in init
        _coordinator = State(initialValue: appCoordinator)
        _isInitialized = State(initialValue: true)
    }
    
    var body: some Scene {
        WindowGroup {
            if let coordinator = coordinator {
                AppNavigationView(coordinator: coordinator, pendingDeepLinkUrl: $pendingDeepLinkUrl)
                    .onOpenURL { url in
                        handleDeepLink(url)
                    }
            }
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        pendingDeepLinkUrl = url
    }
}

