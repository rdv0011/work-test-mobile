package io.umain.munchies.feature.restaurant.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Restaurant(
    val id: String,
    val name: String,
    val imageUrl: String,
    val rating: Float,
    val deliveryTimeMinutes: Int,
    val filterIds: List<String>
)

@Serializable
data class RestaurantOpen(
    val id: String,
    val open: Boolean
)