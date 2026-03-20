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
     * - "munchies://restaurants" → RestaurantListRoute
     * - "munchies://restaurants/123" → RestaurantDetailRoute("123")
     * - "munchies://settings" → SettingsRoute
     * - "munchies://modal/..." → Modal overlays
     */
    fun parseDeepLink(deepLink: String): NavigationState {
        val tabDef = TabDefinition(
            id = "main",
            label = io.umain.munchies.core.localization.StringResources.app_title,
            icon = io.umain.munchies.core.ui.IconId.Logo,
            rootRoute = RestaurantListRoute()
        )
        val tabId = tabDef.id
        val stacksByTab = mutableMapOf(tabId to mutableListOf<Route>(tabDef.rootRoute))
        val modals = mutableListOf<ModalRoute>()
        if (deepLink.isEmpty() || deepLink == "munchies://restaurants") {
            // Default: restaurant list
            // Already set
        } else if (deepLink.startsWith("munchies://restaurants/")) {
            val restaurantId = deepLink.removePrefix("munchies://restaurants/")
            stacksByTab[tabId]?.add(RestaurantDetailRoute(restaurantId))
        } else if (deepLink == "munchies://settings") {
            stacksByTab[tabId]?.add(SettingsRoute())
        } else if (deepLink.startsWith("munchies://modal/")) {
            // Parse modal type and params
            val modalPath = deepLink.removePrefix("munchies://modal/")
            val (type, queryParams) = modalPath.split("?", limit = 2).let { parts ->
                parts[0] to (if (parts.size > 1) parts[1] else "")
            }
            modals.addAll(parseModalType(type, queryParams))
        }
        return NavigationState(
            tabNavigation = TabNavigationState(
                tabDefinitions = listOf(tabDef),
                stacksByTab = stacksByTab.mapValues { it.value.toList() },
                activeTabId = tabId
            ),
            modalStack = modals
        )
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
