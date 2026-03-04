package io.umain.munchies.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals

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
