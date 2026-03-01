package io.umain.munchies.navigation

/**
 * Unified constants for all route names and parameters across platforms.
 *
 * Uses camelCase for platform-agnostic route identifiers (used by iOS).
 * Concrete platform implementations (Android) may use different naming,
 * but all refer to these constants for consistency.
 */
object RouteConstants {
    // == ROUTE PARAMETERS ==
    
    const val PARAM_RESTAURANT_ID = "restaurantId"
    
    // == ROUTE NAMES ==
    
    const val ROUTE_RESTAURANT_LIST = "restaurantList"
    
    const val ROUTE_RESTAURANT_DETAIL = "restaurantDetail"
    
    const val ROUTE_RESTAURANT_DETAIL_PATTERN = "$ROUTE_RESTAURANT_DETAIL/{$PARAM_RESTAURANT_ID}"
    
    // == ROUTE KEYS (for scope/holder identification) ==
    
    const val KEY_RESTAURANT_LIST = "RestaurantList"
    
    const val KEY_RESTAURANT_DETAIL_PREFIX = "RestaurantDetail_"
}

