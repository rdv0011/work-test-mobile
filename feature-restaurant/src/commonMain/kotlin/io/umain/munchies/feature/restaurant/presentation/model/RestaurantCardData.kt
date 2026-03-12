package io.umain.munchies.feature.restaurant.presentation.model

data class RestaurantCardData(
    val id: String,
    val restaurantName: String,
    val tags: List<String>,
    val deliveryTime: Int,
    val distance: Double,
    val rating: Double,
    val imageUrl: String,
    val contentDescription: String = "Restaurant: $restaurantName"
)