import Foundation
import shared

@MainActor
final class RouteHolderRegistry {
    
    private let coordinator: AppCoordinator
    private var holders: [String: AnyObject] = [:]
    
    init(coordinator: AppCoordinator) {
        self.coordinator = coordinator
    }
    
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        let route = Route.restaurantList
        let key = route.key
        
        if let existing = holders[key] as? RestaurantListViewModelHolder {
            return existing
        }
        
        let created = RestaurantListViewModelHolder()
        holders[key] = created
        return created
    }
    
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        let route = Route.restaurantDetail(restaurantId)
        let key = route.key
        
        if let existing = holders[key] as? RestaurantDetailViewModelHolder {
             return existing
        }
        
        let created = RestaurantDetailViewModelHolder(restaurantId: restaurantId)
        holders[key] = created
        return created
    }
    
    func cleanup(activeRoutes: Set<String>) {
        let inactiveKeys = Set(holders.keys).subtracting(activeRoutes)
        
        if !inactiveKeys.isEmpty {
            inactiveKeys.forEach { key in
                holders[key] = nil
            }
        }
    }
}
