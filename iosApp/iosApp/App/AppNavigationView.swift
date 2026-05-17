import SwiftUI
import shared

struct AppNavigationView: View {
    @StateObject private var navigator: NavigationCoordinator
    @Binding var pendingDeepLinkUrl: URL?
    
    init(coordinator: CoreAppCoordinator, pendingDeepLinkUrl: Binding<URL?>) {
        let restaurantProvider = RestaurantRouteProvider(
            coordinator: coordinator,
            holderRegistry: RestaurantHolderProviderImpl()
        )
        let settingsProvider = CoreSettingsRouteProvider(
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
        TabView(selection: $navigator.activeTabId) {
            Text("Restaurants Tab")
                .tabItem { Label("Restaurants", systemImage: "list.bullet") }
                .tag("restaurants")
            
            Text("Settings Tab")
                .tabItem { Label("Settings", systemImage: "gear") }
                .tag("settings")
        }
    }
}
