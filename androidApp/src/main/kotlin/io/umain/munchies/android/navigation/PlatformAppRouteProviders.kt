package io.umain.munchies.android.navigation

import io.umain.munchies.android.features.restaurant.navigation.restaurantDetailRouteHandlerAndroid
import io.umain.munchies.android.features.restaurant.navigation.restaurantListRouteHandlerAndroid
import io.umain.munchies.android.features.settings.navigation.settingsRouteHandlerAndroid
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.navigation.RouteProvider

interface PlatformAppRouteProviders {
    fun getAllProviders(): List<RouteProvider>
}

class AndroidAppRouteProviders : PlatformAppRouteProviders {
    override fun getAllProviders(): List<RouteProvider> = listOf(
        RestaurantRouteProvider(),
        SettingsRouteProvider()
    )
    
    companion object {
        fun create(): PlatformAppRouteProviders = AndroidAppRouteProviders()
    }
}

private class RestaurantRouteProvider : RouteProvider {
    override fun getRoutes(): List<RouteHandler> = listOf(
        restaurantListRouteHandlerAndroid(),
        restaurantDetailRouteHandlerAndroid()
    )
}

private class SettingsRouteProvider : RouteProvider {
    override fun getRoutes(): List<RouteHandler> = listOf(
        settingsRouteHandlerAndroid()
    )
}
