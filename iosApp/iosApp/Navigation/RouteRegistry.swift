import Foundation
import shared

/// iOS wrapper for the Kotlin RouteRegistry.
///
/// Single source of truth for route scope lifetime management on iOS.
/// This class wraps Koin operations and provides an iOS-friendly API.
///
/// OWNERSHIP MODEL:
/// - RouteRegistry owns all route lifetimes (Scopes)
/// - Feature ViewModelHolders do NOT own scopes
/// - NavigationCoordinator triggers cleanup on navigation changes
/// - ARC integration: Views keep holders alive; registry keeps scopes alive
///
/// LIFECYCLE:
/// 1. NavigationCoordinator calls registry.lifetimeFor(routeId, factory)
/// 2. Registry creates Koin scope or returns cached one
/// 3. Registry caches the RouteLifetime
/// 4. ViewModelHolder retains both ViewModel and Scope
/// 5. On navigation change, NavigationCoordinator calls cleanup(activeRoutes)
/// 6. Registry closes and removes from Koin any route not in activeRoutes
/// 7. When holder is deallocated, scope is already closed by Registry
@MainActor
final class RouteRegistry {
    /// Maps route ID to its lifetime holder (Scope wrapper)
    private var lifetimes: [String: RouteLifetimeWrapper] = [:]
    
    private let koin: Koin
    
    init() {
        guard let koin = try? GlobalContext.shared.get() else {
            fatalError("Koin must be initialized before RouteRegistry")
        }
        self.koin = koin
    }
    
    /// Get or create a route lifetime using the provided factory.
    ///
    /// MUST NOT: Create multiple scopes for the same routeId.
    /// The factory is called only on first access for each routeId.
    ///
    /// - Parameters:
    ///   - routeId: Unique identifier for this route instance
    ///   - factory: Closure that creates a Koin Scope (called only if scope doesn't exist)
    /// - Returns: A Scope that will be closed by this registry
    func lifetimeFor(routeId: String, factory: () -> Scope) -> Scope {
        if let existing = lifetimes[routeId] {
            Logger.shared.log("RouteRegistry", "Reusing existing scope for: \(routeId)")
            return existing.scope
        }
        
        Logger.shared.log("RouteRegistry", "Creating new scope for: \(routeId)")
        let scope = factory()
        let wrapper = RouteLifetimeWrapper(routeId: routeId, scope: scope)
        lifetimes[routeId] = wrapper
        return scope
    }
    
    /// Close all inactive routes and cleanup Koin cache.
    ///
    /// MUST BE CALLED: Whenever the navigation stack changes (push, pop, etc)
    /// with the set of routes that are currently ACTIVE.
    ///
    /// Routes not in [activeRoutes] will be:
    /// 1. Removed from this registry
    /// 2. Have their scope closed
    /// 3. Removed from Koin's cache (preventing resurrection)
    ///
    /// - Parameter activeRoutes: Set of route IDs that should remain alive
    func cleanup(activeRoutes: Set<String>) {
        let inactiveKeys = Set(lifetimes.keys).subtracting(activeRoutes)
        
        if !inactiveKeys.isEmpty {
            Logger.shared.log("RouteRegistry", "Cleaning up routes: \(inactiveKeys.sorted()), keeping: \(activeRoutes.sorted())")
            inactiveKeys.forEach { key in
                if let wrapper = lifetimes.removeValue(forKey: key) {
                    wrapper.close()
                    // Also remove from Koin's cache to prevent scope resurrection
                    do {
                        try koin.deleteScope(scopeId: key)
                    } catch {
                        Logger.shared.log("RouteRegistry", "Failed to delete scope from Koin: \(key), error: \(error)")
                    }
                }
            }
        }
    }
    
    /// Emergency cleanup: Close all routes and release all resources.
    ///
    /// USAGE: Call when app is destroyed or explicitly resetting state.
    /// Not typically called during normal navigation (use cleanup() instead).
    func clearAll() {
        Logger.shared.log("RouteRegistry", "Clearing all route lifetimes")
        lifetimes.values.forEach { wrapper in
            wrapper.close()
        }
        lifetimes.values.forEach { wrapper in
            do {
                try koin.deleteScope(scopeId: wrapper.routeId)
            } catch {
                Logger.shared.log("RouteRegistry", "Failed to delete scope from Koin: \(wrapper.routeId), error: \(error)")
            }
        }
        lifetimes.removeAll()
    }
}

/// Wrapper for a Scope to track its lifetime and ensure cleanup.
private final class RouteLifetimeWrapper {
    let routeId: String
    let scope: Scope
    
    init(routeId: String, scope: Scope) {
        self.routeId = routeId
        self.scope = scope
    }
    
    func close() {
        do {
            try scope.close()
        } catch {
            Logger.shared.log("RouteRegistry", "Error closing scope \(routeId): \(error)")
        }
    }
}

/// Simple logging helper for RouteRegistry
private enum Logger {
    static let shared = Self()
    
    func log(_ tag: String, _ message: String) {
        #if DEBUG
        print("[\(tag)] \(message)")
        #endif
    }
}
