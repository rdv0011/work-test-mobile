import Foundation
import shared

class RestaurantRouteProvider: RouteProvider {
    private let coordinator: CoreAppCoordinator
    private let holderRegistry: RestaurantHolderProvider

    init(coordinator: CoreAppCoordinator, holderRegistry: RestaurantHolderProvider) {
        self.coordinator = coordinator
        self.holderRegistry = holderRegistry
    }

    func getRoutes() -> [CoreRouteHandler] {
        return []
    }

    func getHolder(for route: CoreRoute) -> AnyObject? {
        switch route {
        case _ as CoreRestaurantListRoute:
            return holderRegistry.restaurantListHolder()
        case let r as CoreRestaurantDetailRoute:
            return holderRegistry.restaurantDetailHolder(restaurantId: r.restaurantId)
        default:
            return nil
        }
    }
}

protocol RestaurantHolderProvider {
    func restaurantListHolder() -> RestaurantListViewModelHolder
    func restaurantDetailHolder(restaurantId: String) -> AnyObject?
}

struct RestaurantHolderProviderImpl: RestaurantHolderProvider {
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        fatalError("Use NavigationCoordinator.restaurantListHolder() instead")
    }
    
    func restaurantDetailHolder(restaurantId: String) -> AnyObject? {
        fatalError("Use NavigationCoordinator.restaurantDetailHolder() instead")
    }
}
