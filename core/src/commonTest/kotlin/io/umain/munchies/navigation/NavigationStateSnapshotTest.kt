package io.umain.munchies.navigation

import io.umain.munchies.core.ui.IconId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class NavigationStateSnapshotTest {

    @Test
    fun testRestaurantListRouteSerialization() {
        val route = RestaurantListRoute()
        val json = Json.encodeToString(RestaurantListRoute.serializer(), route)
        val decoded = Json.decodeFromString(RestaurantListRoute.serializer(), json)
        assertEquals(route.key, decoded.key)
    }

    @Test
    fun testRestaurantDetailRouteSerialization() {
        val route = RestaurantDetailRoute("restaurant-123")
        val json = Json.encodeToString(RestaurantDetailRoute.serializer(), route)
        val decoded = Json.decodeFromString(RestaurantDetailRoute.serializer(), json)
        assertEquals(route.restaurantId, decoded.restaurantId)
        assertEquals(route.key, decoded.key)
    }

    @Test
    fun testSettingsRouteSerialization() {
        val route = SettingsRoute()
        val json = Json.encodeToString(SettingsRoute.serializer(), route)
        val decoded = Json.decodeFromString(SettingsRoute.serializer(), json)
        assertEquals(route.key, decoded.key)
    }

    @Test
    fun testFilterModalRouteSerialization() {
        val route = FilterModalRoute(listOf("filter1", "filter2"))
        val json = Json.encodeToString(FilterModalRoute.serializer(), route)
        val decoded = Json.decodeFromString(FilterModalRoute.serializer(), json)
        assertEquals(route.preSelectedFilters, decoded.preSelectedFilters)
        assertEquals(route.key, decoded.key)
    }

    @Test
    fun testSubmitReviewModalRouteSerialization() {
        val route = SubmitReviewModalRoute("restaurant-456")
        val json = Json.encodeToString(SubmitReviewModalRoute.serializer(), route)
        val decoded = Json.decodeFromString(SubmitReviewModalRoute.serializer(), json)
        assertEquals(route.restaurantId, decoded.restaurantId)
        assertEquals(route.key, decoded.key)
    }

    @Test
    fun testConfirmActionModalRouteSerialization() {
        val route = ConfirmActionModalRoute("Are you sure?", "Yes", "No")
        val json = Json.encodeToString(ConfirmActionModalRoute.serializer(), route)
        val decoded = Json.decodeFromString(ConfirmActionModalRoute.serializer(), json)
        assertEquals(route.message, decoded.message)
        assertEquals(route.confirmText, decoded.confirmText)
        assertEquals(route.cancelText, decoded.cancelText)
    }

    @Test
    fun testDatePickerModalRouteSerialization() {
        val route = DatePickerModalRoute("2024-04-12")
        val json = Json.encodeToString(DatePickerModalRoute.serializer(), route)
        val decoded = Json.decodeFromString(DatePickerModalRoute.serializer(), json)
        assertEquals(route.initialDate, decoded.initialDate)
    }

    @Test
    fun testReviewSuccessModalRouteSerialization() {
        val route = ReviewSuccessModalRoute
        val json = Json.encodeToString(ReviewSuccessModalRoute.serializer(), route)
        val decoded = Json.decodeFromString(ReviewSuccessModalRoute.serializer(), json)
        assertEquals(route.key, decoded.key)
    }

    @Test
    fun testReviewErrorAlertRouteSerialization() {
        val route = ReviewErrorAlertRoute("Something went wrong")
        val json = Json.encodeToString(ReviewErrorAlertRoute.serializer(), route)
        val decoded = Json.decodeFromString(ReviewErrorAlertRoute.serializer(), json)
        assertEquals(route.message, decoded.message)
    }

    @Test
    fun testNavigationStateSnapshotRoundtrip() {
        val snapshot = NavigationStateSnapshot(
            tabNavigation = TabNavigationStateSnapshot(
                tabDefinitions = listOf(
                    TabDefinitionSnapshot("tab1", "Tab 1", IconId.Restaurant, RestaurantListRoute()),
                    TabDefinitionSnapshot("tab2", "Tab 2", IconId.Settings, SettingsRoute())
                ),
                activeTabId = "tab1",
                stacksByTab = mapOf(
                    "tab1" to listOf(RestaurantListRoute(), RestaurantDetailRoute("123")),
                    "tab2" to listOf(SettingsRoute())
                ),
                navigationDirection = NavigationDirection.Forward
            ),
            modalStack = listOf(FilterModalRoute(listOf("tag1"))),
            originDeepLink = "munchies://restaurants/123",
            restoredFromCrash = false,
            restorationTimestamp = 1234567890L
        )

        val json = Json.encodeToString(NavigationStateSnapshot.serializer(), snapshot)
        val decoded = Json.decodeFromString(NavigationStateSnapshot.serializer(), json)

        assertEquals(snapshot.tabNavigation.activeTabId, decoded.tabNavigation.activeTabId)
        assertEquals(snapshot.tabNavigation.navigationDirection, decoded.tabNavigation.navigationDirection)
        assertEquals(snapshot.modalStack.size, decoded.modalStack.size)
        assertEquals(snapshot.originDeepLink, decoded.originDeepLink)
        assertEquals(snapshot.restoredFromCrash, decoded.restoredFromCrash)
        assertEquals(snapshot.restorationTimestamp, decoded.restorationTimestamp)
    }

    @Test
    fun testNavigationStateToSnapshotRoundtrip() {
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(
                TabDefinition("restaurants", "Restaurants", IconId.Restaurant, RestaurantListRoute()),
                TabDefinition("settings", "Settings", IconId.Settings, SettingsRoute())
            ),
            activeTabId = "restaurants",
            stacksByTab = mapOf(
                "restaurants" to listOf(RestaurantListRoute(), RestaurantDetailRoute("456")),
                "settings" to listOf(SettingsRoute())
            )
        )
        
        val navState = NavigationState(
            tabNavigation = tabNav,
            modalStack = listOf(ConfirmActionModalRoute("Confirm?")),
            originDeepLink = "munchies://settings"
        )
        
        val snapshot = navState.toSnapshot(restoredFromCrash = false)
        
        assertEquals(navState.tabNavigation.activeTabId, snapshot.tabNavigation.activeTabId)
        assertEquals(navState.modalStack.size, snapshot.modalStack.size)
        assertEquals(navState.originDeepLink, snapshot.originDeepLink)
        assertEquals(false, snapshot.restoredFromCrash)

        val restoredState = snapshot.toNavigationState()
        
        assertEquals(navState.tabNavigation.activeTabId, restoredState.tabNavigation.activeTabId)
        assertEquals(navState.modalStack.size, restoredState.modalStack.size)
        assertEquals(navState.originDeepLink, restoredState.originDeepLink)
    }
}
