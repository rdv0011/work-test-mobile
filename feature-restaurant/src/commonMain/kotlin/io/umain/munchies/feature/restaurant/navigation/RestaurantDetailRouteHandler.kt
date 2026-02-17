package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteConstants
import io.umain.munchies.navigation.RouteHandler

object RestaurantDetailRouteHandler : RouteHandler {
    override val route: Route = RestaurantDetailRoute("")

    override fun toRouteString(): String = RouteConstants.ROUTE_RESTAURANT_DETAIL_PATTERN

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantDetail

    override fun destinationToRoute(destination: Destination): Route? {
        return (destination as? Destination.RestaurantDetail)?.let {
            RestaurantDetailRoute(it.restaurantId)
        }
    }
}
