import SwiftUI
import shared

struct AppNavigationView: View {
    @StateObject private var navigator: NavigationCoordinator
    
    init(coordinator: AppCoordinator) {
        _navigator = StateObject(wrappedValue: NavigationCoordinator(coordinator: coordinator))
    }
    
    var body: some View {
        NavigationStack(path: $navigator.path) {
            let restaurantListHolder = navigator.restaurantListHolder()
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
            let holder = navigator.restaurantDetailHolder(restaurantId: restaurantId)
            RestaurantDetailView(
                restaurantId: restaurantId,
                coordinator: navigator.coordinator,
                viewModel: holder.viewModel,
                holder: holder
            )
        case .restaurantList:
            EmptyView()
        }
    }
}
