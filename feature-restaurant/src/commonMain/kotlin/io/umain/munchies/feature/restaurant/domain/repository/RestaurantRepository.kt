package io.umain.munchies.feature.restaurant.domain.repository

import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.Filter
import io.umain.munchies.feature.restaurant.domain.model.RestaurantOpen
import io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus

interface RestaurantRepository {
    suspend fun getFilterById(id: String): Filter?
    suspend fun getRestaurants(): List<Restaurant>
    suspend fun getRestaurantsOpenById(id: String): RestaurantStatus
}
