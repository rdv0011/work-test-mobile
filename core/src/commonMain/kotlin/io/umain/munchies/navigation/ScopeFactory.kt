package io.umain.munchies.navigation
/**
 * Factory for creating and managing Koin scopes for routes.
 *
 * This interface is primarily used on Android to create scope instances
 * for each route. Scopes contain route-scoped dependencies managed by Koin.
 *
 * Each route type may have its own scope factory implementation that knows
 * how to configure scope with the correct dependencies for that route.
 */
interface ScopeFactory {
    /**
     * Create a new scope for the given route.
     *
     * The scope should be pre-configured with all dependencies needed
     * for this route type.
     *
     * @param route The route to create a scope for
     * @return A new Scope instance configured for this route
     */
    fun createScope(route: Route): Any // Scope type from Koin, using Any to avoid Android dependency
}
