package io.umain.munchies.core

/**
 * Maps TextId sealed class instances to their corresponding translation keys.
 * This provides a single source of truth for text resource mapping.
 */
fun mapTextIdToKey(textId: TextId): String = when (textId) {
    TextId.AppTitle -> "app.title"
    
    TextId.RestaurantListTitle -> "restaurant.list.title"
    TextId.RestaurantDetailTitle -> "restaurant.detail.title"
    TextId.FilterAll -> "filter.all"
    
    TextId.RestaurantStatusOpen -> "restaurant.status.open"
    TextId.RestaurantStatusClosed -> "restaurant.status.closed"
    
    TextId.AccessibilityRestaurantCard -> "accessibility.restaurant.card"
    TextId.AccessibilityFilterChip -> "accessibility.filter.chip"
    TextId.AccessibilityFilterSelected -> "accessibility.filter.selected"
    TextId.AccessibilityBackButton -> "accessibility.back.button"
    
    TextId.ErrorLoading -> "error.loading"
    TextId.ErrorNetwork -> "error.network"
    TextId.Loading -> "loading"
}
