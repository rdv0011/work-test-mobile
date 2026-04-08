package io.umain.munchies.android.features.restaurant.navigation

import io.umain.munchies.feature.restaurant.di.RestaurantListScope
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.ScopedRouteHandler
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

class RestaurantListRouteHandlerAndroid : ScopedRouteHandler {
    override val route: Route = RestaurantListRoute()

    override fun toRouteString(): String = "restaurant_list"

    override fun destinationToRoute(destination: Destination): Route? =
        if (destination is Destination.RestaurantList) RestaurantListRoute() else null

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantList

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

fun restaurantListRouteHandlerAndroid(): RestaurantListRouteHandlerAndroid =
    RestaurantListRouteHandlerAndroid()
