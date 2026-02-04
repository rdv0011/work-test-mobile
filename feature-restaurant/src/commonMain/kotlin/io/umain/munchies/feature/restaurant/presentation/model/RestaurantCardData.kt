package io.umain.munchies.feature.restaurant.presentation.model

data class RestaurantCardData(
    val id: String,
    val restaurantName: String,
    val tags: List<String>,
    val deliveryTime: String,
    val distance: String,
    val rating: Double,
    val imageUrl: String,
    val contentDescription: String = "Restaurant: $restaurantName"
)