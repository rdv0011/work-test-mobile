package io.umain.munchies.navigation

private data class ReviewsModal(val restaurantId: String) : ModalRoute {
    override val key: String = "reviews_$restaurantId"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
    override val dismissOnBackgroundTap: Boolean = true
}

private data class FilterModal(val preSelectedFilters: List<String>) : ModalRoute {
    override val key: String = "filter"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
    override val dismissOnBackgroundTap: Boolean = true
}

private data class ConfirmActionModal(
    val message: String,
    val confirmText: String,
    val cancelText: String
) : ModalRoute {
    override val key: String = "confirm"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.DIALOG
    override val dismissOnBackgroundTap: Boolean = false
}

/**
 * Handles deep link URL parsing and navigation state reconstruction.
 *
 * Supports three URL schemes:
 * - Linear: /screen/route/params
 * - Modal: /modal/type?params
 * - Tabs: /tabs/tabId/screen/route?params
 */
object DeepLinkParser {
    
    /**
     * Parse a deep link URL and return the corresponding navigation state.
     *
     * Examples:
     * - "app://restaurant-list" → RestaurantListRoute
     * - "app://restaurant-detail/123" → RestaurantDetailRoute("123")
     * - "app://restaurant-list/filters" → Shows filter modal
     * - "app://restaurant-detail/123?reviews=true" → Detail + reviews modal
     */
    fun parseDeepLink(deepLink: String): NavigationState {
        return when {
            deepLink.isEmpty() -> NavigationState(
                primaryStack = listOf(RestaurantListRoute())
            )
            deepLink.startsWith("app://") -> {
                parseAppScheme(deepLink.substring(6))
            }
            else -> NavigationState(
                primaryStack = listOf(RestaurantListRoute())
            )
        }
    }
    
    private fun parseAppScheme(path: String): NavigationState {
        val (routePath, queryParams) = path.split("?", limit = 2).let { parts ->
            parts[0] to (if (parts.size > 1) parts[1] else "")
        }
        
        val segments = routePath.trim('/').split('/').filter { it.isNotEmpty() }
        
        if (segments.isEmpty()) {
            return NavigationState(primaryStack = listOf(RestaurantListRoute()))
        }
        
        return when (segments[0]) {
            "restaurant-list" -> {
                NavigationState(
                    primaryStack = listOf(RestaurantListRoute()),
                    modalStack = parseModalsFromQuery(queryParams)
                )
            }
            "restaurant-detail" -> {
                val restaurantId = segments.getOrNull(1) ?: return NavigationState(
                    primaryStack = listOf(RestaurantListRoute())
                )
                NavigationState(
                    primaryStack = listOf(
                        RestaurantListRoute(),
                        RestaurantDetailRoute(restaurantId)
                    ),
                    modalStack = parseModalsFromQuery(queryParams)
                )
            }
            "modal" -> {
                NavigationState(
                    primaryStack = listOf(RestaurantListRoute()),
                    modalStack = parseModalType(segments.getOrNull(1) ?: "", queryParams)
                )
            }
            else -> NavigationState(primaryStack = listOf(RestaurantListRoute()))
        }
    }
    
    private fun parseModalsFromQuery(queryParams: String): List<ModalRoute> {
        val modals = mutableListOf<ModalRoute>()
        val params = parseQueryString(queryParams)
        
        if (params["reviews"] == "true") {
            val restaurantId = params["restaurantId"] ?: return modals
            modals.add(ReviewsModal(restaurantId))
        }
        
        if (params["filters"] == "true") {
            val selected = params["selectedFilters"]?.split(",") ?: emptyList()
            modals.add(FilterModal(selected))
        }
        
        return modals
    }
    
    private fun parseModalType(type: String, queryParams: String): List<ModalRoute> {
        val params = parseQueryString(queryParams)
        
        return when (type) {
            "filter" -> {
                val selected = params["selected"]?.split(",") ?: emptyList()
                listOf(FilterModal(selected))
            }
            "reviews" -> {
                val restaurantId = params["restaurantId"] ?: return emptyList()
                listOf(ReviewsModal(restaurantId))
            }
            "confirm" -> {
                val message = params["message"] ?: return emptyList()
                val confirmText = params["confirmText"] ?: "OK"
                val cancelText = params["cancelText"] ?: "Cancel"
                listOf(ConfirmActionModal(message, confirmText, cancelText))
            }
            else -> emptyList()
        }
    }
    
    private fun parseQueryString(query: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        if (query.isEmpty()) return params
        
        query.split("&").forEach { param ->
            val (key, value) = param.split("=", limit = 2).let { parts ->
                parts[0] to (if (parts.size > 1) simpleUrlDecode(parts[1]) else "")
            }
            params[key] = value
        }
        
        return params
    }
    
    private fun simpleUrlDecode(encoded: String): String {
        return encoded.replace("%20", " ")
            .replace("%2F", "/")
            .replace("%3F", "?")
            .replace("%3D", "=")
            .replace("%26", "&")
            .replace("%2B", "+")
    }
}
