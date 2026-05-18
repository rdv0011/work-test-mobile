package io.umain.munchies.navigation

import io.umain.munchies.core.localization.StringResources

/**
 * Force-export types to Swift by including them in function signatures.
 * These functions don't perform runtime operations; they exist purely to force
 * the KMP compiler to include types in the generated Swift framework.
 */
fun _exportDeepLinkProcessorType(processor: DeepLinkProcessor): DeepLinkProcessor = processor
fun _exportDeepLinkConstantsType(): String = DeepLinkConstants.HOST_RESTAURANTS
fun _exportRouteConstantsType(): String = RouteConstants.ROUTE_RESTAURANT_LIST
fun _exportFilterModalRouteType(route: FilterModalRoute): FilterModalRoute = route
fun _exportConfirmActionModalRouteType(route: ConfirmActionModalRoute): ConfirmActionModalRoute = route
fun _exportSubmitReviewModalRouteType(route: SubmitReviewModalRoute): SubmitReviewModalRoute = route
fun _exportDatePickerModalRouteType(route: DatePickerModalRoute): DatePickerModalRoute = route

// ============================================================================
// String Resources Factory Getters
// ============================================================================

fun getStringResourcesApp_title(): String = StringResources.app_title
fun getStringResourcesRestaurant_list_title(): String = StringResources.restaurant_list_title
fun getStringResourcesFilter_all(): String = StringResources.filter_all
fun getStringResourcesRestaurant_detail_title(): String = StringResources.restaurant_detail_title
fun getStringResourcesRestaurant_status_open(): String = StringResources.restaurant_status_open
fun getStringResourcesRestaurant_status_closed(): String = StringResources.restaurant_status_closed
fun getStringResourcesError_loading(): String = StringResources.error_loading
fun getStringResourcesError_network(): String = StringResources.error_network
fun getStringResourcesLoading(): String = StringResources.loading
fun getStringResourcesTab_restaurants(): String = StringResources.tab_restaurants
fun getStringResourcesTab_settings(): String = StringResources.tab_settings
fun getStringResourcesSettings_title(): String = StringResources.settings_title

// ============================================================================
// Route Constants Getters
// ============================================================================

fun getRouteConstantsParam_restaurant_id(): String = RouteConstants.PARAM_RESTAURANT_ID
fun getRouteConstantsRoute_restaurant_list(): String = RouteConstants.ROUTE_RESTAURANT_LIST
fun getRouteConstantsRoute_restaurant_detail(): String = RouteConstants.ROUTE_RESTAURANT_DETAIL
fun getRouteConstantsRoute_restaurant_detail_pattern(): String = RouteConstants.ROUTE_RESTAURANT_DETAIL_PATTERN
fun getRouteConstantsKey_restaurant_list(): String = RouteConstants.KEY_RESTAURANT_LIST
fun getRouteConstantsKey_restaurant_detail_prefix(): String = RouteConstants.KEY_RESTAURANT_DETAIL_PREFIX

// ============================================================================
// Deep Link Constants Getters
// ============================================================================

fun getDeepLinkConstantsHost_restaurants(): String = DeepLinkConstants.HOST_RESTAURANTS
fun getDeepLinkConstantsHost_settings(): String = DeepLinkConstants.HOST_SETTINGS
fun getDeepLinkConstantsHost_modal(): String = DeepLinkConstants.HOST_MODAL
fun getDeepLinkConstantsTab_id_restaurants(): String = DeepLinkConstants.TAB_ID_RESTAURANTS
fun getDeepLinkConstantsTab_id_settings(): String = DeepLinkConstants.TAB_ID_SETTINGS
fun getDeepLinkConstantsSingle_segment_path(): Int = DeepLinkConstants.SINGLE_SEGMENT_PATH
fun getDeepLinkConstantsRestaurant_id_index(): Int = DeepLinkConstants.RESTAURANT_ID_INDEX
