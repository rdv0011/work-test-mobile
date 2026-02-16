package io.umain.munchies.android.navigation

import io.umain.munchies.android.features.restaurant.navigation.AndroidRestaurantRouteProvider
import io.umain.munchies.navigation.RouteProvider

interface PlatformAppRouteProviders {
    fun getRestaurantProvider(): RouteProvider
    
    fun getAllProviders(): List<RouteProvider> = listOf(
        getRestaurantProvider()
    )
}

class AndroidAppRouteProviders : PlatformAppRouteProviders {
    override fun getRestaurantProvider(): RouteProvider = 
        AndroidRestaurantRouteProvider()
    
    companion object {
        fun create(): PlatformAppRouteProviders = AndroidAppRouteProviders()
    }
}
