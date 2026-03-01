package io.umain.munchies.navigation

private data class SubmitReviewModal(val restaurantId: String) : ModalRoute {
    override val key: String = "submit_review_$restaurantId"
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
 * Supports host-based routing with path segments and query parameters:
 * - Restaurant host: /restaurants[/{restaurantId}]
 * - Modal host: /modal/{modalType}[?queryParams]
 * - Settings host: /settings
 */
object DeepLinkParser {
    
    /**
     * Parse a deep link URL and return the corresponding navigation state.
     *
     * Supported deep link formats:
     * 
     * Restaurant navigation:
     * - "munchies://restaurants" → RestaurantListRoute
     * - "munchies://restaurants/123" → RestaurantDetailRoute("123")
     * 
     * Modal dialogs:
     * - "munchies://modal/filter?filters=tag1,tag2" → FilterModal with selected tags
     * - "munchies://modal/submit_review/123" → SubmitReviewModal for restaurant 123
     * - "munchies://modal/confirm?message=...&confirmText=...&cancelText=..." → ConfirmActionModal
     * - "munchies://modal/date_picker?initialDate=2026-02-25" → DatePickerModal with initial date
     * 
     * Settings tab:
     * - "munchies://settings" → Shows settings screen
     */
    fun parseDeepLink(deepLink: String): NavigationState {
        return when {
            deepLink.isEmpty() -> NavigationState(
                primaryStack = listOf(RestaurantListRoute())
            )
            deepLink.startsWith("munchies://") -> {
                parseAppScheme(deepLink.substring(11))
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
            "restaurants" -> {
                val restaurantId = segments.getOrNull(1)
                if (restaurantId != null) {
                    // munchies://restaurants/{restaurantId}
                    NavigationState(
                        primaryStack = listOf(
                            RestaurantListRoute(),
                            RestaurantDetailRoute(restaurantId)
                        ),
                        modalStack = parseModalsFromQuery(queryParams)
                    )
                } else {
                    // munchies://restaurants
                    NavigationState(
                        primaryStack = listOf(RestaurantListRoute()),
                        modalStack = parseModalsFromQuery(queryParams)
                    )
                }
            }
            "settings" -> {
                NavigationState(
                    primaryStack = listOf(RestaurantListRoute()),
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
        
        if (params["submit_review"] == "true") {
            val restaurantId = params["restaurantId"] ?: return modals
            modals.add(SubmitReviewModal(restaurantId))
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
            "submit_review" -> {
                val restaurantId = params["restaurantId"] ?: return emptyList()
                listOf(SubmitReviewModal(restaurantId))
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
