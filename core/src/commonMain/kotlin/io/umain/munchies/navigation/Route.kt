package io.umain.munchies.navigation

/**
 * Base protocol for all routes in the app.
 * 
 * Each route declares exactly which holder owns its ViewModel through the associated type.
 * This enables type-safe access to ViewModels without casting.
 */
interface Route : Comparable<Route> {
    val key: String
    val isRootRoute: Boolean
        get() = false
    
    /**
     * Optional handler for this route.
     * Features can provide handlers for their routes to enable
     * provider-based navigation without app-layer coupling.
     *
     * This property is optional to maintain backward compatibility
     * with existing routes that don't yet use handlers.
     */
    val handler: RouteHandler?
        get() = null

    override fun compareTo(other: Route): Int = key.compareTo(other.key)
    
    /**
     * Optional cleanup method called when the route becomes inactive.
     *
     * Implement this in route classes that need to perform cleanup
     * (e.g., cancel background jobs, reset state, etc.).
     *
     * Called only when the route is explicitly removed from the
     * active routes stack, typically during navigation events.
     */
    fun cleanup() {
        // Default no-op implementation for backward compatibility
    }

    companion object {
        val rootRoutes: List<Route>
            get() = listOf(RestaurantListRoute())
    }
}
