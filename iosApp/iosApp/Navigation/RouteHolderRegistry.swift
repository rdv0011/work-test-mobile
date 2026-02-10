import Foundation
import shared

/// iOS implementation that bridges between NavigationCoordinator and RouteRegistry.
///
/// This registry creates ViewModelHolders, which in turn request scopes from the global RouteRegistry.
/// The global RouteRegistry owns the scopes; this class just creates the holders.
///
/// OWNERSHIP CHAIN:
/// RouteRegistry (owns scopes)
///   ↓
/// RouteHolderRegistry (creates holders that use factories)
///   ↓
/// ViewModelHolder (retains ViewModel from scope)
///   ↓
/// View (retains holder)
///
/// CRITICAL: When cleanup is called, the global RouteRegistry will close scopes.
/// This class just removes references to holders.
@MainActor
final class RouteHolderRegistry {
    
    private let coordinator: AppCoordinator
    private var holders: [String: AnyObject] = [:]
    private let routeRegistry = RouteRegistry()
    
    init(coordinator: AppCoordinator) {
        self.coordinator = coordinator
    }
    
    /// Get or create a holder for the restaurant list route.
    ///
    /// This method:
    /// 1. Calls routeRegistry.lifetimeFor() with a factory to create/get the Scope
    /// 2. Creates a new RestaurantListViewModelHolder with that Scope
    /// 3. Caches and returns the holder
    ///
    /// - Returns: RestaurantListViewModelHolder that retains the ViewModel
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        let route = Route.restaurantList
        let key = route.key
        
        if let existing = holders[key] as? RestaurantListViewModelHolder {
            return existing
        }
        
        let scope = routeRegistry.lifetimeFor(routeId: key) {
            FeatureRestaurantIosKt.createRestaurantListScopeIos()
        }
        
        let created = RestaurantListViewModelHolder(scope: scope)
        holders[key] = created
        return created
    }
    
    /// Get or create a holder for the restaurant detail route.
    ///
    /// This method:
    /// 1. Calls routeRegistry.lifetimeFor() with a factory to create/get the Scope
    /// 2. Creates a new RestaurantDetailViewModelHolder with that Scope
    /// 3. Caches and returns the holder
    ///
    /// - Parameter restaurantId: The restaurant ID for this detail route
    /// - Returns: RestaurantDetailViewModelHolder that retains the ViewModel
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        let route = Route.restaurantDetail(restaurantId)
        let key = route.key
        
        if let existing = holders[key] as? RestaurantDetailViewModelHolder {
            return existing
        }
        
        let scope = routeRegistry.lifetimeFor(routeId: key) {
            FeatureRestaurantIosKt.createRestaurantDetailScopeIos(restaurantId: restaurantId)
        }
        
        let created = RestaurantDetailViewModelHolder(restaurantId: restaurantId, scope: scope)
        holders[key] = created
        return created
    }
    
    /// Clean up inactive routes.
    ///
    /// This method delegates to RouteRegistry, which:
    /// 1. Closes Koin scopes not in activeRoutes
    /// 2. Removes from Koin cache
    /// This method then removes holder references for those routes.
    ///
    /// - Parameter activeRoutes: Set of route IDs that should remain alive
    func cleanup(activeRoutes: Set<String>) {
        routeRegistry.cleanup(activeRoutes: activeRoutes)
        
        let inactiveKeys = Set(holders.keys).subtracting(activeRoutes)
        
        if !inactiveKeys.isEmpty {
            inactiveKeys.forEach { key in
                holders[key] = nil
            }
        }
    }
}
