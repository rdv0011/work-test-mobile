package io.umain.munchies.feature.restaurant.data.remote

import kotlinx.serialization.Serializable

/**
 * Remote DTOs and API abstraction for restaurants.
 *
 * NOTE: This is an abstraction layer. A real Ktor-backed implementation
 * can be provided later and injected via DI. For now a fake implementation
 * can be used during development and tests.
 */
interface RestaurantApi {
    suspend fun getRestaurants(): List<RestaurantDto>
    suspend fun getRestaurantById(id: String): RestaurantDto?
    suspend fun getFilters(): List<FilterDto>
}

@Serializable
data class RestaurantDto(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val status: String,
    val filterIds: List<String>
)

@Serializable
data class FilterDto(
    val id: String,
    val name: String,
    val iconUrl: String
)
