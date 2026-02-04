package io.umain.munchies.feature.restaurant.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote DTOs and API abstraction for restaurants.
 *
 * NOTE: This is an abstraction layer. A real Ktor-backed implementation
 * can be provided later and injected via DI. For now a fake implementation
 * can be used during development and tests.
 */
interface RestaurantApi {
    suspend fun getFilter(id: String): FilterDto?
    suspend fun getRestaurants(): RestaurantContainerDto
    suspend fun getOpen(id: String): RestaurantOpenDto?
}

@Serializable
data class FilterDto(
    val id: String,
    val name: String,
    @SerialName("image_url")
    val imageUrl: String
)

@Serializable
data class RestaurantContainerDto(
    val restaurants: List<RestaurantDto>
)

@Serializable
data class RestaurantDto(
    val id: String,
    val name: String,

    @SerialName("image_url")
    val imageUrl: String,

    val rating: Float,

    @SerialName("delivery_time_minutes")
    val deliveryTimeMinutes: Int,

    val filterIds: List<String>
)

@Serializable
data class RestaurantOpenDto(
    @SerialName("restaurant_id")
    val id: String,
    @SerialName("is_currently_open")
    val open: Boolean,
)
