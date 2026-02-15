package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.ScopedRouteHandler
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import io.umain.munchies.feature.restaurant.di.RestaurantListScope

object RestaurantListRouteHandler : ScopedRouteHandler {
    override val route: Route = RestaurantListRoute()

    override fun toRouteString(): String = "restaurant_list"

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantList

    override fun destinationToRoute(destination: Destination): Route? =
        if (canHandle(destination)) RestaurantListRoute() else null

    override fun createScope(route: Route): Scope {
        require(route is RestaurantListRoute) { "Expected RestaurantListRoute, got $route" }
        val koin = GlobalContext.get()
        val scopeId = route.key
        return koin.getScopeOrNull(scopeId)
            ?: koin.createScope(
                scopeId = scopeId,
                qualifier = named(RestaurantListScope.qualifierName)
            )
    }
}
