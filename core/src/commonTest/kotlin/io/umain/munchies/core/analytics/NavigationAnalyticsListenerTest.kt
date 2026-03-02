package io.umain.munchies.core.analytics

import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.TabDefinition
import io.umain.munchies.navigation.TabNavigationState
import io.umain.munchies.core.ui.TextId
import io.umain.munchies.core.ui.IconId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationAnalyticsListenerTest {

    @Test
    fun testScreenViewEventCreation() {
        val event = AnalyticsEvent.ScreenView(
            screenName = "restaurant_detail",
            screenClass = "RestaurantDetailScreen",
            properties = mapOf("restaurant_id" to "123")
        )
        assertEquals("screen_view", event.eventName)
        assertEquals("123", event.properties["restaurant_id"])
    }

    @Test
    fun testTabSwitchEventCreation() {
        val event = AnalyticsEvent.TabSwitch(
            tabId = "settings",
            tabName = "Settings"
        )
        assertEquals("tab_switch", event.eventName)
        assertEquals("settings", event.tabId)
    }

    @Test
    fun testModalOpenEventCreation() {
        val event = AnalyticsEvent.ModalOpen(
            modalName = "filter",
            modalClass = "FilterModal"
        )
        assertEquals("modal_open", event.eventName)
        assertEquals("filter", event.modalName)
    }

    @Test
    fun testModalDismissEventCreation() {
        val event = AnalyticsEvent.ModalDismiss(
            modalName = "filter",
            timeSpentMs = 5000
        )
        assertEquals("modal_dismiss", event.eventName)
        assertEquals(5000, event.timeSpentMs)
    }

    @Test
    fun testCustomEventCreation() {
        val event = AnalyticsEvent.CustomEvent(
            eventName = "restaurant_filtered",
            properties = mapOf("filter_type" to "cuisine")
        )
        assertEquals("restaurant_filtered", event.eventName)
        assertEquals("cuisine", event.properties["filter_type"])
    }

    @Test
    fun testErrorEventCreation() {
        val event = AnalyticsEvent.ErrorEvent(
            errorType = "NetworkError",
            errorMessage = "Failed to fetch",
            errorContext = "RestaurantListScreen"
        )
        assertEquals("error", event.eventName)
        assertEquals("NetworkError", event.errorType)
    }

    @Test
    fun testPropertiesSanitization() {
        val props = mapOf(
            "restaurant_id" to "123",
            "email" to "user@test.com",
            "password" to "secret",
            "auth_token" to "token123",
            "api_key" to "key456"
        )
        
        val listener = NavigationAnalyticsListener(
            MockAnalyticsService(),
            AppCoordinator().navigationState
        )
        
        // Call the private sanitize method through reflection
        val sanitizeMethod = NavigationAnalyticsListener::class.java.getDeclaredMethod(
            "sanitizeProperties", 
            Map::class.java
        )
        sanitizeMethod.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val sanitized = sanitizeMethod.invoke(listener, props) as Map<String, String>
        
        assertTrue(!sanitized.containsKey("email"), "Should not contain email")
        assertTrue(!sanitized.containsKey("password"), "Should not contain password")
        assertTrue(!sanitized.containsKey("auth_token"), "Should not contain auth_token")
        assertTrue(!sanitized.containsKey("api_key"), "Should not contain api_key")
        assertTrue(sanitized.containsKey("restaurant_id"), "Should contain restaurant_id")
    }
}

class MockAnalyticsService : AnalyticsService {
    val trackedEvents = mutableListOf<AnalyticsEvent>()

    override suspend fun trackEvent(event: AnalyticsEvent) {
        trackedEvents.add(event)
    }

    override suspend fun setUserProperties(properties: Map<String, String>) {}

    override suspend fun clearUserProperties() {}

    override suspend fun flush() {}
}
