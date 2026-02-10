package io.umain.munchies.android.navigation

import android.util.Log
import io.umain.munchies.core.lifecycle.RouteLifetime
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.parameter.parametersOf

/**
 * Single source of truth for route-scoped state lifetime management.
 *
 * OWNERSHIP MODEL:
 * - RouteRegistry owns and manages ALL route scope lifetimes
 * - Features provide scope factories only (no ownership)
 * - Each route gets exactly one RouteLifetime for its lifetime
 * - Cleanup is deterministic: scope closes only when Registry removes it
 *
 * USAGE:
 * - Call lifetimeFor(routeId, factory) to get/create a scope
 * - Call cleanup(activeRoutes) when navigation stack changes
 * - All routes not in activeRoutes are closed immediately
 *
 * CRITICAL: Coordinates with Koin's scope cache. After closing a scope,
 * also removes it from Koin to prevent scope resurrection.
 */
class RouteRegistry {
    private val _lifetimes = mutableMapOf<String, RouteLifetime>()
    val lifetimes: Map<String, RouteLifetime> = _lifetimes

    /**
     * Get or create a RouteLifetime for the given route.
     *
     * MUST NOT: Create multiple scopes for the same routeId.
     * The factory is called only on first access for each routeId.
     *
     * @param routeId Unique identifier for this route instance
     * @param factory Scope factory (called only if scope doesn't exist)
     * @return RouteLifetime that owns the scope for this route
     */
    fun lifetimeFor(
        routeId: String,
        factory: () -> org.koin.core.scope.Scope
    ): RouteLifetime {
        val isNew = routeId !in _lifetimes
        return _lifetimes.getOrPut(routeId) {
            Log.i("RouteRegistry", "Creating new RouteLifetime for: $routeId")
            RouteLifetime(routeId, factory())
        }.also {
            if (!isNew) {
                Log.i("RouteRegistry", "Reusing existing RouteLifetime for: $routeId")
            }
        }
    }

    /**
     * Close all inactive routes based on current navigation stack.
     *
     * MUST BE CALLED: Whenever the navigation stack changes (push, pop, etc)
     * with the set of routes that are currently ACTIVE (visible or in stack).
     *
     * Routes not in [activeRoutes] will be immediately:
     * 1. Removed from this registry
     * 2. Have their scope closed
     * 3. Removed from Koin's cache (preventing resurrection)
     *
     * @param activeRoutes Set of route IDs that should remain alive
     */
    fun cleanup(activeRoutes: Set<String>) {
        val inactiveKeys = _lifetimes.keys - activeRoutes

        if (inactiveKeys.isNotEmpty()) {
            Log.i("RouteRegistry", "Cleaning up routes: ${inactiveKeys.sorted()}, keeping: ${activeRoutes.sorted()}")
            inactiveKeys.forEach { key ->
                _lifetimes.remove(key)?.let { lifetime ->
                    lifetime.close()
                    // Also remove from Koin's cache so stale scopes don't resurrect
                    try {
                        GlobalContext.get().deleteScope(key)
                    } catch (e: Exception) {
                        Log.w("RouteRegistry", "Failed to delete scope from Koin: $key", e)
                    }
                }
            }
        }
    }

    /**
     * Emergency cleanup: Close all routes and release all resources.
     *
     * USAGE: Call when app is destroyed or explicitly resetting state.
     * Not typically called during normal navigation (use cleanup() instead).
     */
    fun clearAll() {
        Log.i("RouteRegistry", "Clearing all route lifetimes")
        _lifetimes.values.forEach { it.close() }
        // Also clean up Koin scopes
        _lifetimes.keys.forEach { key ->
            try {
                GlobalContext.get().deleteScope(key)
            } catch (e: Exception) {
                Log.w("RouteRegistry", "Failed to delete scope from Koin: $key", e)
            }
        }
        _lifetimes.clear()
    }

    fun createScopeForRoute(route: Route) = GlobalContext.get().let { koin ->
        when (route) {
            is RestaurantListRoute -> {
                Log.i("RouteRegistry", "Creating scope for RestaurantList route")
                koin.getScopeOrNull(route.key)
                    ?: koin.createScope(
                        scopeId = route.key,
                        qualifier = named("RestaurantListScope")
                    )
            }
            is RestaurantDetailRoute -> {
                Log.i("RouteRegistry", "Creating scope for RestaurantDetail route: ${route.restaurantId}")
                koin.getScopeOrNull(route.key)
                    ?: koin.createScope(
                        scopeId = route.key,
                        qualifier = named(RestaurantDetailScope("").qualifierName)
                    ).also { scope ->
                        scope.get<RestaurantDetailViewModel>(
                            parameters = { parametersOf(route.restaurantId) }
                        )
                    }
            }
            else -> throw IllegalArgumentException("No scope factory registered for route $route")
        }
    }
}
