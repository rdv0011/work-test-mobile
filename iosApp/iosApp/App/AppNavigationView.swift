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
        ZStack {
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
            
            if let topModal = navigator.showingModal {
                ZStack {
                    Color.black.opacity(0.3)
                        .ignoresSafeArea()
                        .onTapGesture {
                            if topModal.dismissOnBackgroundTap {
                                navigator.coordinator.dismissModal()
                            }
                        }
                    
                    VStack {
                        Spacer()
                        ModalDestinationView(
                            modal: topModal,
                            onDismiss: {
                                navigator.coordinator.dismissModal()
                            }
                        )
                        Spacer()
                    }
                }
                .transition(.opacity.combined(with: .scale))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: navigator.showingModal)
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
