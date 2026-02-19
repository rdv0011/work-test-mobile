import SwiftUI
import shared

struct AppNavigationView: View {
    @StateObject private var navigator: NavigationCoordinator
    
    init(coordinator: AppCoordinator) {
        let restaurantProvider = RestaurantRouteProvider(
            coordinator: coordinator,
            holderRegistry: RestaurantHolderProviderImpl()
        )
        _navigator = StateObject(wrappedValue: NavigationCoordinator(
            coordinator: coordinator,
            routeProviders: [restaurantProvider]
        ))
    }
    
    var body: some View {
        TabNavigationView(navigator: navigator)
    }
}
