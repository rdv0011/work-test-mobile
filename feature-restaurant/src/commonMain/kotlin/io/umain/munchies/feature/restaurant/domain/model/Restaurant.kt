package io.umain.munchies.feature.restaurant.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Restaurant(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val status: RestaurantStatus,
    val filterIds: List<String>
)
