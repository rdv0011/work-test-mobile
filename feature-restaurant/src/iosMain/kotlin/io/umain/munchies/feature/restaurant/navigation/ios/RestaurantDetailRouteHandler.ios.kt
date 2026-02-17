package io.umain.munchies.feature.restaurant.navigation.ios

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteConstants
import io.umain.munchies.navigation.RouteHandler

object RestaurantDetailRouteHandlerImpl : RouteHandler {
    override val route: Route = RestaurantDetailRoute("")

    override fun toRouteString(): String = RouteConstants.ROUTE_RESTAURANT_DETAIL

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantDetail

    override fun destinationToRoute(destination: Destination): Route? {
        return (destination as? Destination.RestaurantDetail)?.let {
            RestaurantDetailRoute(it.restaurantId)
        }
    }
}
