package io.umain.munchies.navigation

/**
 * Constants for deep link handling across the application.
 * Centralizes all deep link-related string literals and patterns.
 * 
 * These constants are used by both Android and iOS platforms to ensure
 * consistent deep link handling and feature parity.
 */
object DeepLinkConstants {
    
    // Scheme
    const val SCHEME = "munchies"
    
    // Hosts
    const val HOST_RESTAURANTS = "restaurants"
    const val HOST_MODAL = "modal"
    const val HOST_SETTINGS = "settings"
    
    // Paths (without leading slash for cross-platform compatibility)
    const val PATH_FILTER = "filter"
    const val PATH_SUBMIT_REVIEW = "submit_review"
    const val PATH_CONFIRM = "confirm"
    const val PATH_DATE_PICKER = "date_picker"
    
    // Query parameter names
    const val QUERY_PARAM_FILTERS = "filters"
    const val QUERY_PARAM_MESSAGE = "message"
    const val QUERY_PARAM_CONFIRM_TEXT = "confirmText"
    const val QUERY_PARAM_CANCEL_TEXT = "cancelText"
    const val QUERY_PARAM_INITIAL_DATE = "initialDate"
    
    // Default values for optional parameters
    const val DEFAULT_CONFIRM_MESSAGE = "Are you sure?"
    const val DEFAULT_CONFIRM_TEXT = "OK"
    const val DEFAULT_CANCEL_TEXT = "Cancel"
    
    // Tab IDs
    const val TAB_ID_RESTAURANTS = "restaurants"
    const val TAB_ID_SETTINGS = "settings"
    
    // Path segment indices
    const val MODAL_TYPE_INDEX = 0
    const val SUBMIT_REVIEW_RESTAURANT_ID_INDEX = 1
    const val RESTAURANT_ID_INDEX = 0
    
    // Path segment lengths
    const val SINGLE_SEGMENT_PATH = 1
    const val TWO_SEGMENT_PATH = 2
}
