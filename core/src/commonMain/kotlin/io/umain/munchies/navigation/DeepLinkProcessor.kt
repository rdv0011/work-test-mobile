package io.umain.munchies.navigation

/**
 * Platform-agnostic deep link routing processor.
 *
 * Handles route pattern matching and coordinator method dispatch.
 * Input parsing (platform-specific URI/URL extraction) is handled by platform layers.
 * Output (coordinator method calls) is identical across platforms.
 *
 * This processor ensures identical deep link behavior on Android and iOS.
 */
object DeepLinkProcessor {
    
    /**
     * Process a deep link and dispatch to appropriate coordinator actions.
     *
     * @param host The deep link host component (e.g., "restaurants", "modal", "settings")
     * @param pathSegments List of path components as strings (e.g., ["restaurants", "123"])
     * @param queryParams Map of query parameters (e.g., {"filters" -> "tag1,tag2"})
     * @param coordinator AppCoordinator to dispatch navigation actions to
     */
    fun processDeepLink(
        host: String,
        pathSegments: List<String>,
        queryParams: Map<String, String>,
        coordinator: AppCoordinator
    ) {
        when (host) {
            DeepLinkConstants.HOST_RESTAURANTS -> 
                routeRestaurantDeepLink(pathSegments, coordinator)
            DeepLinkConstants.HOST_SETTINGS -> 
                coordinator.selectTab(tabId = DeepLinkConstants.TAB_ID_SETTINGS)
            DeepLinkConstants.HOST_MODAL -> 
                routeModalDeepLink(pathSegments, queryParams, coordinator)
        }
    }
    
    private fun routeRestaurantDeepLink(
        pathSegments: List<String>,
        coordinator: AppCoordinator
    ) {
        when {
            pathSegments.isEmpty() -> {
                // munchies://restaurants
                coordinator.navigateToScreen(Destination.RestaurantList)
            }
            pathSegments.size == DeepLinkConstants.SINGLE_SEGMENT_PATH -> {
                // munchies://restaurants/{restaurantId}
                val restaurantId = pathSegments[DeepLinkConstants.RESTAURANT_ID_INDEX]
                coordinator.navigateToScreen(Destination.RestaurantDetail(restaurantId))
            }
        }
    }
    
    private fun routeModalDeepLink(
        pathSegments: List<String>,
        queryParams: Map<String, String>,
        coordinator: AppCoordinator
    ) {
        if (pathSegments.isEmpty()) return
        
        val modalType = pathSegments[DeepLinkConstants.MODAL_TYPE_INDEX]
        
        when (modalType) {
            DeepLinkConstants.PATH_FILTER -> {
                // munchies://modal/filter?filters=tag1,tag2
                val filtersParam = queryParams[DeepLinkConstants.QUERY_PARAM_FILTERS] ?: ""
                val preSelectedFilters = if (filtersParam.isNotEmpty()) {
                    filtersParam.split(",").map { it.trim() }
                } else {
                    emptyList()
                }
                coordinator.showFilterModal(preSelectedFilters)
            }
            
            DeepLinkConstants.PATH_SUBMIT_REVIEW -> {
                // munchies://modal/submit_review/{restaurantId}
                if (pathSegments.size > DeepLinkConstants.SUBMIT_REVIEW_RESTAURANT_ID_INDEX) {
                    val restaurantId = pathSegments[DeepLinkConstants.SUBMIT_REVIEW_RESTAURANT_ID_INDEX]
                    coordinator.submitReview(restaurantId)
                }
            }
            
            DeepLinkConstants.PATH_CONFIRM -> {
                // munchies://modal/confirm?message=...&confirmText=...&cancelText=...
                val message = queryParams[DeepLinkConstants.QUERY_PARAM_MESSAGE] 
                    ?: DeepLinkConstants.DEFAULT_CONFIRM_MESSAGE
                val confirmText = queryParams[DeepLinkConstants.QUERY_PARAM_CONFIRM_TEXT] 
                    ?: DeepLinkConstants.DEFAULT_CONFIRM_TEXT
                val cancelText = queryParams[DeepLinkConstants.QUERY_PARAM_CANCEL_TEXT] 
                    ?: DeepLinkConstants.DEFAULT_CANCEL_TEXT
                coordinator.showConfirmation(message, confirmText, cancelText)
            }
            
            DeepLinkConstants.PATH_DATE_PICKER -> {
                // munchies://modal/date_picker?initialDate=2026-02-25
                val initialDate = queryParams[DeepLinkConstants.QUERY_PARAM_INITIAL_DATE]
                coordinator.showModal(ModalDestination.DatePicker(initialDate))
            }
        }
    }
}
