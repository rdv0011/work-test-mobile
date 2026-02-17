package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteConstants
import io.umain.munchies.navigation.RouteHandler

object RestaurantListRouteHandler : RouteHandler {
    override val route: Route = RestaurantListRoute()

    override fun toRouteString(): String = RouteConstants.ROUTE_RESTAURANT_LIST

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantList

    override fun destinationToRoute(destination: Destination): Route? =
        if (canHandle(destination)) RestaurantListRoute() else null
}
