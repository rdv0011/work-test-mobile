package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.core.ui.IconId
import io.umain.munchies.navigation.DeepLinkHandler
import io.umain.munchies.navigation.DeepLinkResult
import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.TabNavigationState
import io.umain.munchies.navigation.TabDefinition
import io.umain.munchies.navigation.NavigationDirection
import io.umain.munchies.navigation.ScreenEntry
import io.umain.munchies.navigation.ReviewSuccessModalRoute

class ReviewsDeepLinkHandler : DeepLinkHandler {
    override fun canHandle(deepLink: String): Boolean {
        return deepLink.contains("/reviews")
    }

    override fun parseDeepLink(deepLink: String): DeepLinkResult {
        return try {
            val path = deepLink.substringAfter("://", "")
            val segments = path.split("/").filter { it.isNotEmpty() }
            val restaurantId = segments.getOrNull(1)
                ?: return DeepLinkResult.NotFound(deepLink)

            val tabId = "main"
            val tabDef = TabDefinition(
                id = tabId,
                label = "Restaurants",
                icon = IconId.Restaurant,
                rootRoute = RestaurantListRoute()
            )
            val listRoute = RestaurantListRoute()
            val detailRoute = RestaurantDetailRoute(restaurantId)
            val stacksByTab = mapOf(
                tabId to listOf(
                    ScreenEntry(listRoute, listRoute.key),
                    ScreenEntry(detailRoute, detailRoute.key)
                )
            )
            val tabNavigation = TabNavigationState(
                tabDefinitions = listOf(tabDef),
                activeTabId = tabId,
                stacksByTab = stacksByTab,
                navigationDirection = NavigationDirection.Forward
            )
            DeepLinkResult.Success(
                navigationState = NavigationState(
                    tabNavigation = tabNavigation,
                    modalStack = listOf(ReviewSuccessModalRoute)
                )
            )
        } catch (e: Exception) {
            DeepLinkResult.Error(deepLink, e)
        }
    }
}
