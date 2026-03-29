package io.umain.munchies.feature.settings.navigation

import io.umain.munchies.core.ui.IconId
import io.umain.munchies.navigation.DeepLinkHandler
import io.umain.munchies.navigation.DeepLinkResult
import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.TabNavigationState
import io.umain.munchies.navigation.TabDefinition
import io.umain.munchies.navigation.NavigationDirection
import io.umain.munchies.navigation.Route

class SettingsDeepLinkHandler : DeepLinkHandler {
    override fun canHandle(deepLink: String): Boolean {
        return deepLink.contains("settings")
    }

    override fun parseDeepLink(deepLink: String): DeepLinkResult {
        return try {
            val tabId = "main"
            val tabDef = TabDefinition(
                id = tabId,
                label = "Settings",
                icon = IconId.Settings,
                rootRoute = SettingsRoute()
            )
            val stacksByTab = mapOf(
                tabId to listOf<Route>(
                    SettingsRoute()
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
                    tabNavigation = tabNavigation
                )
            )
        } catch (e: Exception) {
            DeepLinkResult.Error(deepLink, e)
        }
    }
}
