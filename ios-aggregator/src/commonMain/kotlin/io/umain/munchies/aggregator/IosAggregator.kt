package io.umain.munchies.aggregator

import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.SettingsRoute

object IosAggregator {
    const val VERSION = "1.0.0"
}

fun createRestaurantListRoute(): RestaurantListRoute = RestaurantListRoute()

fun createRestaurantDetailRoute(restaurantId: String): RestaurantDetailRoute =
    RestaurantDetailRoute(restaurantId)

fun createSettingsRoute(): SettingsRoute = SettingsRoute()
