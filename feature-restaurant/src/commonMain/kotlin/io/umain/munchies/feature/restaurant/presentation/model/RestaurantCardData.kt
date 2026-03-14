package io.umain.munchies.feature.restaurant.presentation.model

import io.umain.munchies.feature.restaurant.domain.model.Restaurant

data class RestaurantCardData(
    val id: String,
    val restaurantName: String,
    val tags: List<String>,
    val deliveryTime: Int,
    val distance: Double,
    val rating: String,
    val imageUrl: String,
    val contentDescription: String = "Restaurant: $restaurantName"
)

internal fun Restaurant.toCardData(ratingPresentable: String): RestaurantCardData {
    val even = (this.rating * 10).toInt() % 2 == 0

    val tags = if (even)
        listOf("Take-Out", "Fast delivery", "Eat-In")
    else
        listOf("Take-Out")

    val deliveryTime = if (even) 30 else 15
    val distance = if (even) 0.5 else 2.3

    return RestaurantCardData(
        id = id,
        restaurantName = name,
        tags = tags,
        deliveryTime = deliveryTime,
        distance = distance,
        rating = ratingPresentable,
        imageUrl = imageUrl
    )
}