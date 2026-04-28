package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.core.lifecycle.Closeable
import io.umain.munchies.feature.restaurant.navigation.android.restaurantDetailRouteHandlerAndroid
import io.umain.munchies.feature.restaurant.navigation.android.restaurantListRouteHandlerAndroid
import io.umain.munchies.navigation.RouteHandler
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun loadRestaurantKoinModules() {
    val restaurantModule = module {
        single { restaurantDetailRouteHandlerAndroid() } bind RouteHandler::class
        single { restaurantListRouteHandlerAndroid() } bind RouteHandler::class
    }
    loadKoinModules(restaurantModule)
}
