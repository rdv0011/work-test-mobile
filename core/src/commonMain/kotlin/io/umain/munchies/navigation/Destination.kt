package io.umain.munchies.navigation

sealed class Destination {
    data object RestaurantList : Destination()
    data class RestaurantDetail(val restaurantId: String) : Destination()
}
