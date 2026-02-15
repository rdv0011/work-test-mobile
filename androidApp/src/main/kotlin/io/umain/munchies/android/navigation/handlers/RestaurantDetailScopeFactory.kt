package io.umain.munchies.android.navigation.handlers

import android.util.Log
import io.umain.munchies.core.lifecycle.RouteLifetime
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.Route
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

/**
 * Factory for creating Koin scopes for the RestaurantDetail route.
 *
 * Each route instance (identified by restaurantId) gets its own scope.
 * The scope is pre-configured with dependencies specific to that restaurant detail.
 */
class RestaurantDetailScopeFactory {
    fun createScope(route: Route): Scope {
        require(route is RestaurantDetailRoute) { "Expected RestaurantDetailRoute, got $route" }

        Log.i("RestaurantDetailScopeFactory", "Creating scope for RestaurantDetail route: ${route.restaurantId}")

        val koin = GlobalContext.get()
        return koin.getScopeOrNull(route.key)
            ?: koin.createScope(
                scopeId = route.key,
                qualifier = named(RestaurantDetailScope("").qualifierName)
            ).also { scope ->
                scope.get<RestaurantDetailViewModel>(
                    parameters = { parametersOf(route.restaurantId) }
                )
            }
    }
}
