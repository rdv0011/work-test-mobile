package io.umain.munchies.feature.restaurant.domain.repository

import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.Filter

interface RestaurantRepository {
    suspend fun getRestaurants(): List<Restaurant>
    suspend fun getRestaurantById(id: String): Restaurant?
    suspend fun getFilters(): List<Filter>
    suspend fun getRestaurantsByFilter(filterIds: Set<String>): List<Restaurant>
}
