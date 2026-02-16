package io.umain.munchies.navigation

import io.umain.munchies.feature.restaurant.navigation.RestaurantListRouteHandler
import io.umain.munchies.feature.restaurant.navigation.RestaurantDetailRouteHandler

interface PlatformAppRouteProviders {
    fun getRestaurantProvider(): RouteProvider
    
    fun getAllProviders(): List<RouteProvider> = listOf(
        getRestaurantProvider()
    )
}

class IosAppRouteProviders : PlatformAppRouteProviders {
    override fun getRestaurantProvider(): RouteProvider = 
        IosRestaurantRouteProvider()
    
    companion object {
        fun create(): PlatformAppRouteProviders = IosAppRouteProviders()
    }
}

private class IosRestaurantRouteProvider : RouteProvider {
    override fun getRoutes(): List<io.umain.munchies.navigation.RouteHandler> = listOf(
        RestaurantListRouteHandler,
        RestaurantDetailRouteHandler
    )
}
