package io.umain.munchies.navigation

/**
 * Registry for all route handlers in the app.
 * Allows type-safe registration and lookup of routes and their handlers.
 */
class RouteRegistry(
    private val handlers: List<RouteHandler>
) {
    private val handlerMap: Map<String, RouteHandler> = handlers.associateBy { it.route.key }

    fun getHandlerForKey(key: String): RouteHandler? = handlerMap[key]

    fun getAllHandlers(): List<RouteHandler> = handlers

    // Cleanup logic for route lifetimes, if needed
    fun cleanup(activeRouteKeys: Set<String>) {
        // Implement scope cleanup if required
    }
}
