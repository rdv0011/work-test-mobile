package io.umain.munchies.feature.restaurant.data.repository

import io.umain.munchies.feature.restaurant.domain.model.Filter
import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.data.mapper.toDomain
import io.umain.munchies.feature.restaurant.data.remote.RestaurantApi

class RestaurantRepositoryImpl(
    private val api: RestaurantApi? = null
) : RestaurantRepository {
    override suspend fun getFilterById(id: String): Filter? = api?.let { remote ->
        remote.getFilter(id)?.toDomain()
    }

    override suspend fun getRestaurants(): List<Restaurant> = api?.let { remote ->
        remote.getRestaurants().restaurants.map { it.toDomain() }
    } ?: listOf()

    override suspend fun getRestaurantsOpenById(id: String): RestaurantStatus =
        api?.let { remote ->
            remote.getOpen(id)?.toDomain()
        } ?: RestaurantStatus.CLOSED
}
