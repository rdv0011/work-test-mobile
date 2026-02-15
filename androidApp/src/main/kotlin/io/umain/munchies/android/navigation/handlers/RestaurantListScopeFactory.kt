package io.umain.munchies.android.navigation.handlers

import android.util.Log
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * Factory for creating Koin scopes for the RestaurantList route.
 *
 * The RestaurantList scope contains dependencies needed for the list screen.
 */
class RestaurantListScopeFactory {
    fun createScope(route: Route): Scope {
        require(route is RestaurantListRoute) { "Expected RestaurantListRoute, got $route" }

        Log.i("RestaurantListScopeFactory", "Creating scope for RestaurantList route")

        val koin = GlobalContext.get()
        return koin.getScopeOrNull(route.key)
            ?: koin.createScope(
                scopeId = route.key,
                qualifier = named("RestaurantListScope")
            )
    }
}
