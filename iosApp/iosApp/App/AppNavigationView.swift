import SwiftUI
import shared

struct AppNavigationView: View {
    @StateObject private var navigator: NavigationCoordinator
    @StateObject private var restaurantListHolder = RestaurantListViewModelHolder()
    
    init(coordinator: AppCoordinator) {
        _navigator = StateObject(wrappedValue: NavigationCoordinator(coordinator: coordinator))
    }
    
    var body: some View {
        NavigationStack(path: $navigator.path) {
            RestaurantListView(
                coordinator: navigator.coordinator,
                viewModel: restaurantListHolder.viewModel
            )
            .navigationDestination(for: Route.self) { route in
                destinationView(for: route)
            }
        }
    }
    
    @ViewBuilder
    private func destinationView(for route: Route) -> some View {
        switch route {
        case .restaurantDetail(let restaurantId):
            RestaurantDetailDestinationView(restaurantId: restaurantId)
        }
    }
}
