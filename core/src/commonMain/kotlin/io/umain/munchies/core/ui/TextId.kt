package io.umain.munchies.core.ui

sealed class TextId {
    object AppTitle : TextId()
    
    object RestaurantListTitle : TextId()
    object RestaurantDetailTitle : TextId()
    object FilterAll : TextId()
    
    object RestaurantStatusOpen : TextId()
    object RestaurantStatusClosed : TextId()
    
    object AccessibilityRestaurantCard : TextId()
    object AccessibilityFilterChip : TextId()
    object AccessibilityFilterSelected : TextId()
    object AccessibilityBackButton : TextId()
    
    object ErrorLoading : TextId()
    object ErrorNetwork : TextId()
    object Loading : TextId()
}
