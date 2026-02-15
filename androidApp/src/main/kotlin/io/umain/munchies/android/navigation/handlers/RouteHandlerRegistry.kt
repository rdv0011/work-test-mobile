package io.umain.munchies.android.navigation.handlers

import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import org.koin.core.scope.Scope

/**
 * Registry mapping routes to their scope factories.
 *
 * This enables dynamic scope creation based on route type without
 * hardcoding scope creation logic in the main navigation layer.
 */
class RouteHandlerRegistry {
    private val scopeFactories = mapOf<Class<out Route>, (Route) -> Scope>(
        RestaurantListRoute::class.java to { route ->
            RestaurantListScopeFactory().createScope(route)
        },
        RestaurantDetailRoute::class.java to { route ->
            RestaurantDetailScopeFactory().createScope(route)
        }
    )

    /**
     * Create a scope for the given route using the appropriate factory.
     *
     * @param route The route to create a scope for
     * @return A Koin scope configured for this route
     * @throws IllegalArgumentException if no factory is registered for this route type
     */
    fun createScope(route: Route): Scope {
        val factory = scopeFactories[route::class.java]
            ?: throw IllegalArgumentException("No scope factory registered for route type: ${route::class.simpleName}")
        return factory(route)
    }
}
