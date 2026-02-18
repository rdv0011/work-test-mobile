package io.umain.munchies.android.navigation

import io.umain.munchies.android.features.restaurant.navigation.restaurantDetailRouteHandlerAndroid
import io.umain.munchies.android.features.restaurant.navigation.restaurantListRouteHandlerAndroid
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.navigation.RouteProvider

interface PlatformAppRouteProviders {
    fun getAllProviders(): List<RouteProvider>
}

class AndroidAppRouteProviders : PlatformAppRouteProviders {
    override fun getAllProviders(): List<RouteProvider> = listOf(
        RestaurantRouteProvider()
    )
    
    companion object {
        fun create(): PlatformAppRouteProviders = AndroidAppRouteProviders()
    }
}

/**
 * Inline provider for restaurant routes on Android.
 * Previously was AndroidRestaurantRouteProvider, now integrated directly
 * to reduce indirection and dead code.
 */
private class RestaurantRouteProvider : RouteProvider {
    override fun getRoutes(): List<RouteHandler> = listOf(
        restaurantListRouteHandlerAndroid(),
        restaurantDetailRouteHandlerAndroid()
    )
}
