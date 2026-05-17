import Foundation
import shared

@MainActor
final class RouteHolderRegistry {
    
    private let coordinator: CoreAppCoordinator
    private let providers: [RouteProvider]
    private var holders: [String: AnyObject] = [:]
    private let routeRegistry = RouteRegistry()
    
    init(
        coordinator: CoreAppCoordinator,
        providers: [RouteProvider] = []
    ) {
        self.coordinator = coordinator
        self.providers = providers
    }
    
    func restaurantListHolder() -> AnyObject? {
        return nil
    }
    
    func restaurantDetailHolder(restaurantId: String) -> AnyObject? {
        return nil
    }
    
    func settingsHolder() -> AnyObject? {
        return nil
    }
    
    func cleanup(activeRoutes: Set<String>) {
        routeRegistry.cleanup(activeRoutes: activeRoutes)
        let inactiveKeys = Set(holders.keys).subtracting(activeRoutes)
        if !inactiveKeys.isEmpty {
            inactiveKeys.forEach { key in
                holders[key] = nil
            }
        }
    }
    
    func holderFor(route: CoreRoute) -> AnyObject? {
        let key = route.key
        
        if let existing = holders[key] {
            return existing
        }
        
        for provider in providers {
            if let holder = provider.getHolder(for: route) {
                holders[key] = holder
                return holder
            }
        }
        
        return nil
    }
}
