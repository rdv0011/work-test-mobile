package io.umain.munchies.android.navigation

import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.ScopedRouteHandler
import org.koin.core.scope.Scope

class ScopedRouteHandlerRegistry(
    private val handlers: List<ScopedRouteHandler>
) {
    private val handlerMap: Map<String, ScopedRouteHandler> = handlers.associateBy { it.route.key }

    fun findHandler(route: Route): ScopedRouteHandler? =
        handlerMap[route.key]
            ?: handlers.firstOrNull { it.route::class == route::class }

    fun createScope(route: Route): Scope {
        val handler = findHandler(route)
            ?: throw IllegalArgumentException("No scope handler registered for route type: ${route::class.simpleName}")
        return handler.createScope(route)
    }
}
