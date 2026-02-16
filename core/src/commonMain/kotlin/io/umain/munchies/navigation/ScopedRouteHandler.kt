package io.umain.munchies.navigation

import org.koin.core.scope.Scope

/**
 * Extended RouteHandler that also knows how to create dependency scopes for its routes.
 *
 * This allows features to declare both routing logic AND scope creation in one place,
 * eliminating the need for hard-coded scope creation in the app layer.
 *
 * OWNERSHIP:
 * - Each feature owns both routing AND scope creation for its routes
 * - App layer simply delegates to handlers (no hard-coded logic)
 * - New screens can be added without modifying app layer
 *
 * USAGE:
 * Implement this interface when your route needs dependency injection via Koin scopes.
 * For routes that don't need scopes, just implement RouteHandler.
 */
interface ScopedRouteHandler : RouteHandler {
    /**
     * Create a Koin scope for the given route.
     *
     * Called when the route is navigated to. The scope should be configured
     * with all dependencies needed for this route's ViewModel and components.
     *
     * @param route The route to create a scope for
     * @return A Koin scope configured for this route
     */
    fun createScope(route: Route): Scope
}
