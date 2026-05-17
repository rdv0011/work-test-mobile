import Foundation
import shared

@MainActor
final class RouteRegistry {
    private var lifetimes: [String: RouteLifetimeWrapper] = [:]
    private let koin: Koin_coreKoin
    
    init() {
        self.koin = Koin_coreKoin()
    }
    
    func lifetimeFor(routeId: String, factory: () -> Koin_coreScope) -> Koin_coreScope {
        if let existing = lifetimes[routeId] {
            return existing.scope
        }
        
        let scope = factory()
        let wrapper = RouteLifetimeWrapper(routeId: routeId, scope: scope)
        lifetimes[routeId] = wrapper
        return scope
    }
    
    func cleanup(activeRoutes: Set<String>) {
        let inactiveKeys = Set(lifetimes.keys).subtracting(activeRoutes)
        
        if !inactiveKeys.isEmpty {
            inactiveKeys.forEach { key in
                if let wrapper = lifetimes.removeValue(forKey: key) {
                    wrapper.close()
                }
            }
        }
    }
    
    func clearAll() {
        lifetimes.values.forEach { wrapper in
            wrapper.close()
        }
        lifetimes.removeAll()
    }
}

private final class RouteLifetimeWrapper {
    let routeId: String
    let scope: Koin_coreScope
    
    init(routeId: String, scope: Koin_coreScope) {
        self.routeId = routeId
        self.scope = scope
    }
    
    func close() {
        scope.close()
    }
}
