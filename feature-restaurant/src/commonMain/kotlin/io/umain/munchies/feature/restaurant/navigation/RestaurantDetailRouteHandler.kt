package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.ScopedRouteHandler
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel

object RestaurantDetailRouteHandler : ScopedRouteHandler {
    override val route: Route = RestaurantDetailRoute("")

    override fun toRouteString(): String = "restaurant_detail/{restaurantId}"

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantDetail

    override fun destinationToRoute(destination: Destination): Route? {
        return (destination as? Destination.RestaurantDetail)?.let {
            RestaurantDetailRoute(it.restaurantId)
        }
    }

    override fun createScope(route: Route): Scope {
        require(route is RestaurantDetailRoute) { "Expected RestaurantDetailRoute, got $route" }
        val koin = GlobalContext.get()
        val scopeId = route.key
        return koin.getScopeOrNull(scopeId)
            ?: koin.createScope(
                scopeId = scopeId,
                qualifier = named(RestaurantDetailScope("").qualifierName)
            ).also { scope ->
                scope.get<RestaurantDetailViewModel>(
                    parameters = { parametersOf(route.restaurantId) }
                )
            }
    }
}
