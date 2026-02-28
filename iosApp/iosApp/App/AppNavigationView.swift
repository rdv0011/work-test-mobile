import SwiftUI
import shared
import Combine

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
                processPendingDeepLinkIfAvailable()
            }
            .onReceive(Just(pendingDeepLinkUrl).compactMap { $0 }, perform: handlePendingDeepLink)
    }
    
    private func processPendingDeepLinkIfAvailable() {
        guard let url = pendingDeepLinkUrl else { return }
        handlePendingDeepLink(url)
    }
    
    private func handlePendingDeepLink(_ url: URL) {
        print("🔗 DEBUG: handlePendingDeepLink() processing: \(url)")
        
        Task {
            print("🔗 DEBUG: Waiting 50ms to ensure listener is fully subscribed...")
            try? await Task.sleep(nanoseconds: 50_000_000)
            
            print("🔗 DEBUG: Sleep complete, calling navigator.processPendingDeepLink()")
            navigator.processPendingDeepLink(url)
            
            print("🔗 DEBUG: Clearing pendingDeepLinkUrl")
            pendingDeepLinkUrl = nil
        }
    }
}

