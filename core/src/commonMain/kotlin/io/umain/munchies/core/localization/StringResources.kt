package io.umain.munchies.core.localization

typealias StringKey = String

object StringResources {
    const val app_title: StringKey = "app_title"
    
    // Restaurant List
    const val restaurant_list_title: StringKey = "restaurant_list_title"
    const val filter_all: StringKey = "filter_all"
    
    // Restaurant Detail
    const val restaurant_detail_title: StringKey = "restaurant_detail_title"
    const val restaurant_status_open: StringKey = "restaurant_status_open"
    const val restaurant_status_closed: StringKey = "restaurant_status_closed"
    
    // Accessibility
    const val accessibility_restaurant_card: StringKey = "accessibility_restaurant_card"
    const val accessibility_filter_chip: StringKey = "accessibility_filter_chip"
    const val accessibility_filter_selected: StringKey = "accessibility_filter_selected"
    const val accessibility_back_button: StringKey = "accessibility_back_button"
    
    // Error & Loading States
    const val error_loading: StringKey = "error_loading"
    const val error_network: StringKey = "error_network"
    const val loading: StringKey = "loading"
    
    // Navigation Tabs
    const val tab_restaurants: StringKey = "tab_restaurants"
    const val tab_settings: StringKey = "tab_settings"
    
    // Settings Screen
    const val settings_title: StringKey = "settings_title"
    const val settings_dark_mode: StringKey = "settings_dark_mode"
    const val settings_notifications: StringKey = "settings_notifications"
    const val settings_about: StringKey = "settings_about"
    const val rating_format: StringKey = "rating_format"
    const val restaurantsResultCount: StringKey = "restaurants_result_count"
}

object Plurals {
    const val item_count: StringKey = "item_count"
    const val restaurant_count: StringKey = "restaurant_count"
}
