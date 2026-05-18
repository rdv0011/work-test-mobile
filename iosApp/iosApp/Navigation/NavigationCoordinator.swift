import SwiftUI
import shared
import os.log

private let logger = Logger(subsystem: "com.munchies.ios", category: "NavigationCoordinator")

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
    private var navigationStateSubscriptionJob: Kotlinx_coroutines_coreJob?
    
    init(
        coordinator: CoreAppCoordinator,
        routeProviders: [RouteProvider] = []
    ) {
        self.coordinator = coordinator
        self.routeProviders = routeProviders
        self.registry = RouteHolderRegistry(coordinator: coordinator, providers: routeProviders)
    }
    
    func startObservingNavigationState(lifecycleOwner: CoreLifecycleOwner?) {
        logger.debug("startObservingNavigationState: Setting up subscription")
        
        var previousState: CoreNavigationState?
        let stateFlow = coordinator.navigationState
        
        navigationStateSubscriptionJob = IosAggregatorExportsKt.subscribeToStateFlow(
            lifecycle: lifecycleOwner ?? (coordinator as! CoreLifecycleOwner),
            stateFlow: stateFlow,
            onStateChanged: { [weak self] (value: Any?) in
                guard let state = value as? CoreNavigationState else {
                    logger.warning("Failed to cast state to CoreNavigationState")
                    return
                }
                
                if previousState != state {
                    DispatchQueue.main.async {
                        self?.updateTabStacksFromState(state)
                    }
                    previousState = state
                }
            }
        )
    }
    
    func updateTabStacksFromState(_ state: CoreNavigationState) {
        logger.debug("updateTabStacksFromState: Processing navigation state")
        
        let tabNav = state.tabNavigation
        activeTabId = tabNav.activeTabId
        
        for (tabId, routeList) in tabNav.stacksByTab {
            var navigationPath = NavigationPath()
            
            for route in routeList {
                let swiftRoute = kotlinRouteToSwiftRoute(route)
                if !swiftRoute.isRootRoute {
                    navigationPath.append(swiftRoute)
                }
            }
            
            tabStacks[tabId] = navigationPath
            logger.debug("updateTabStacksFromState: Tab '\(tabId)' has \(navigationPath.count) routes")
        }
        
        let topModal = state.modalStack.last as? CoreModalRoute
        showingModal = topModal
        if topModal != nil {
            showingModalKey = UUID().uuidString
        }
    }
    
    private func kotlinRouteToSwiftRoute(_ route: CoreRoute) -> Route {
        let key = route.key
        
        if key.hasPrefix("RestaurantDetail_") {
            let restaurantId = String(key.dropFirst("RestaurantDetail_".count))
            return .restaurantDetail(restaurantId)
        } else if key == "RestaurantList" {
            return .restaurantList
        } else if key == "settings" {
            return .settings
        }
        
        return .restaurantList
    }
    
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        return RestaurantListViewModelHolder(
            scope: IosAggregatorExportsKt.createRestaurantListScope(),
            viewModel: IosAggregatorExportsKt.getRestaurantListViewModelFromFramework()
        )
    }
    
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        return RestaurantDetailViewModelHolder(
            restaurantId: restaurantId,
            scope: IosAggregatorExportsKt.createRestaurantDetailScope(restaurantId: restaurantId),
            viewModel: IosAggregatorExportsKt.getRestaurantDetailViewModelFromFramework(restaurantId: restaurantId)
        )
    }
    
    func settingsViewModelHolder() -> SettingsViewModelHolder {
        let scope = IosAggregatorExportsKt.createSettingsScope()
        return SettingsViewModelHolder(scope: scope)
    }
    
    func processPendingDeepLink(_ url: URL) {
        
    }
    
    func cleanup(activeRoutes: Set<String>) {
        registry.cleanup(activeRoutes: activeRoutes)
    }
    
    deinit {
        // Job will be cleaned up automatically when deallocated
    }
}



