package io.umain.munchies.android.navigation

import android.util.Log
import io.umain.munchies.core.lifecycle.RouteLifetime
import io.umain.munchies.navigation.Route
import org.koin.core.context.GlobalContext
import org.koin.core.scope.Scope

class RouteRegistry(
    private val scopeHandlerRegistry: ScopedRouteHandlerRegistry
) {
    private val _lifetimes = mutableMapOf<String, RouteLifetime>()

    fun createScopeForRoute(route: Route): Scope {
        val scope = scopeHandlerRegistry.createScope(route)
        _lifetimes[route.key] = RouteLifetime(route.key, scope)
        Log.i("RouteRegistry", "Created scope and lifetime for route: ${route.key}")
        return scope
    }

    fun cleanup(activeRoutes: Set<String>) {
        val inactiveKeys = _lifetimes.keys - activeRoutes

        if (inactiveKeys.isNotEmpty()) {
            Log.i("RouteRegistry", "Cleaning up routes: ${inactiveKeys.sorted()}, keeping: ${activeRoutes.sorted()}")
            inactiveKeys.forEach { key ->
                _lifetimes.remove(key)?.let { lifetime ->
                    lifetime.close()
                    try {
                        GlobalContext.get().deleteScope(key)
                    } catch (e: Exception) {
                        Log.w("RouteRegistry", "Failed to delete scope from Koin: $key", e)
                    }
                }
            }
        }
    }

    fun clearAll() {
        Log.i("RouteRegistry", "Clearing all route lifetimes")
        _lifetimes.values.forEach { it.close() }
        _lifetimes.keys.forEach { key ->
            try {
                GlobalContext.get().deleteScope(key)
            } catch (e: Exception) {
                Log.w("RouteRegistry", "Failed to delete scope from Koin: $key", e)
            }
        }
        _lifetimes.clear()
    }
}
