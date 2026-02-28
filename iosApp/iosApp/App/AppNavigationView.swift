import SwiftUI
import shared

struct AppNavigationView: View {
    @StateObject private var navigator: NavigationCoordinator
    @Binding var pendingDeepLinkUrl: URL?
    
    init(coordinator: AppCoordinator, pendingDeepLinkUrl: Binding<URL?>) {
        let restaurantProvider = RestaurantRouteProvider(
            coordinator: coordinator,
            holderRegistry: RestaurantHolderProviderImpl()
        )
        let settingsProvider = SettingsRouteProvider(
            coordinator: coordinator,
            holderRegistry: SettingsHolderProviderImpl()
        )
        _navigator = StateObject(wrappedValue: NavigationCoordinator(
            coordinator: coordinator,
            routeProviders: [restaurantProvider, settingsProvider]
        ))
        _pendingDeepLinkUrl = pendingDeepLinkUrl
    }
    
    var body: some View {
        TabNavigationView(navigator: navigator)
            .onAppear {
                handlePendingDeepLink()
            }
    }
    
    private func handlePendingDeepLink() {
        guard let url = pendingDeepLinkUrl else { return }
        
        Task {
            // Wait for navigation event listener to be fully set up
            try? await Task.sleep(nanoseconds: 10_000_000) // 10ms
            
            // Process the deep link
            navigator.processPendingDeepLink(url)
            
            // Clear the pending URL
            pendingDeepLinkUrl = nil
        }
    }
}
