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
            logInfo(tag: "RouteHolderRegistry", message: "Reusing existing RestaurantList holder")
            return existing
        }
        
        logInfo(tag: "RouteHolderRegistry", message: "Creating new RestaurantList holder for key: \(key)")
        let created = RestaurantListViewModelHolder()
        holders[key] = created
        return created
    }
    
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        let route = Route.restaurantDetail(restaurantId)
        let key = route.key
        
        if let existing = holders[key] as? RestaurantDetailViewModelHolder {
            logInfo(tag: "RouteHolderRegistry", message: "Reusing existing detail holder for key: \(key)")
            return existing
        }
        
        logInfo(tag: "RouteHolderRegistry", message: "Creating new detail holder for restaurantId: \(restaurantId), key: \(key)")
        let created = RestaurantDetailViewModelHolder(restaurantId: restaurantId)
        holders[key] = created
        return created
    }
    
    func cleanup(activeRoutes: Set<String>) {
        let inactiveKeys = Set(holders.keys).subtracting(activeRoutes)
        
        if !inactiveKeys.isEmpty {
            logInfo(tag: "RouteHolderRegistry", message: "Cleaning up holders: \(inactiveKeys.sorted().joined(separator: ", ")), keeping: \(activeRoutes.sorted().joined(separator: ", "))")
            inactiveKeys.forEach { key in
                holders[key] = nil
            }
        }
    }
}
