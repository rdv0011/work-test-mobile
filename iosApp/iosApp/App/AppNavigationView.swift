import SwiftUI
import shared

struct AppNavigationView: View {
    @StateObject private var navigationState: NavigationStateObserver
    
    init(coordinator: AppCoordinator) {
        _navigationState = StateObject(wrappedValue: NavigationStateObserver(coordinator: coordinator))
    }
    
    var body: some View {
        NavigationView {
            destinationView
        }
    }
    
    @ViewBuilder
    private var destinationView: some View {
        switch navigationState.currentDestination {
        case is Destination.RestaurantList:
            RestaurantListView(coordinator: navigationState.coordinator)
        case let detail as Destination.RestaurantDetail:
            RestaurantDetailView(
                restaurantId: detail.restaurantId,
                coordinator: navigationState.coordinator
            )
        default:
            Text("Unknown destination")
        }
    }
}
