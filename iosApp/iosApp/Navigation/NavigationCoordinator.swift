import SwiftUI
import shared

@MainActor
class NavigationCoordinator: ObservableObject {
    @Published var activeTabId = "restaurants"
    @Published var tabStacks: [String: NavigationPath] = [
        "restaurants": NavigationPath(),
        "settings": NavigationPath()
    ]
    @Published private(set) var activeRoutes = Set<String>()
    @Published var modalStack: [CoreModalRoute] = []
    @Published var showingModal: CoreModalRoute?
    @Published private(set) var showingModalKey: String? = nil
    
    let coordinator: CoreAppCoordinator
    private let registry: RouteHolderRegistry
    private let routeProviders: [RouteProvider]
    
    init(
        coordinator: CoreAppCoordinator,
        routeProviders: [RouteProvider] = []
    ) {
        self.coordinator = coordinator
        self.routeProviders = routeProviders
        self.registry = RouteHolderRegistry(coordinator: coordinator, providers: routeProviders)
    }
    
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        return RestaurantListViewModelHolder(
            scope: IosAggregatorExportsKt.createRestaurantListScope(),
            viewModel: IosAggregatorExportsKt.getRestaurantListViewModelFromFramework()
        )
    }
    
    func restaurantDetailHolder(restaurantId: String) -> Feature_restaurantRestaurantDetailViewModelHolder {
        return Feature_restaurantRestaurantDetailViewModelHolder(
            restaurantId: restaurantId,
            scope: IosAggregatorExportsKt.createRestaurantDetailScope(restaurantId: restaurantId),
            viewModel: IosAggregatorExportsKt.getRestaurantDetailViewModelFromFramework(restaurantId: restaurantId)
        )
    }
    
    func settingsHolder() -> AnyObject? {
        return nil
    }
    
    func processPendingDeepLink(_ url: URL) {
        
    }
    
    func cleanup(activeRoutes: Set<String>) {
        registry.cleanup(activeRoutes: activeRoutes)
    }
}


