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
    
    override fun compareTo(other: Route): Int = key.compareTo(other.key)
    
    companion object {
        val rootRoutes: List<Route>
            get() = listOf(RestaurantListRoute())
    }
}
