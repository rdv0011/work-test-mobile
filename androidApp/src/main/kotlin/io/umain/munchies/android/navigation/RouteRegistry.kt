package io.umain.munchies.android.navigation

import io.umain.munchies.core.lifecycle.Closeable
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.parameter.parametersOf

class RouteRegistry {
    private val holders = mutableMapOf<String, Closeable>()

    fun ownerFor(route: Route): ScopedViewModelOwner {
        val key = route.key

        holders[key]?.let {
            if (it is ScopedViewModelOwner) return it
        }

        val created = createOwner(route)
        holders[key] = created
        return created
    }

    fun cleanup(activeRoutes: Set<String>) {
        val inactiveKeys = holders.keys - activeRoutes

        inactiveKeys.forEach { key ->
            holders.remove(key)?.close()
        }
    }

    private fun createOwner(route: Route): ScopedViewModelOwner {
        val koin = GlobalContext.get()
        return when (route) {
            is RestaurantListRoute -> {
                val scope = koin.getScopeOrNull(route.key)
                    ?: koin.createScope(
                        scopeId = route.key,
                        qualifier = named("RestaurantListScope")
                    )
                ScopedViewModelOwner(scope)
            }
            is RestaurantDetailRoute -> {
                val scope = koin.getScopeOrNull(route.key)
                    ?: koin.createScope(
                        scopeId = route.key,
                        qualifier = named(RestaurantDetailScope("").qualifierName)
                    )
                val viewModel: RestaurantDetailViewModel = scope.get(
                    parameters = { parametersOf(route.restaurantId) }
                )
                ScopedViewModelOwner(scope)
            }
            else -> throw IllegalArgumentException("No owner registered for route $route")
        }
    }
}
