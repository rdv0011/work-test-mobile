package io.umain.munchies.navigation

sealed class Destination {
    data object RestaurantList : Destination()
    data class RestaurantDetail(val restaurantId: String) : Destination()
    
    fun toRoute(): String = when (this) {
        is RestaurantList -> "restaurant_list"
        is RestaurantDetail -> "restaurant_detail/$restaurantId"
    }
    
    companion object {
        const val ROUTE_RESTAURANT_LIST = "restaurant_list"
        const val ROUTE_RESTAURANT_DETAIL_BASE = "restaurant_detail"
        const val ARG_RESTAURANT_ID = "restaurantId"
        const val ROUTE_RESTAURANT_DETAIL = "$ROUTE_RESTAURANT_DETAIL_BASE/{$ARG_RESTAURANT_ID}"
    }
}
