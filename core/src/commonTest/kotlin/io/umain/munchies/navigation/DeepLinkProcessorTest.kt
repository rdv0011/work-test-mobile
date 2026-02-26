package io.umain.munchies.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for DeepLinkProcessor.
 *
 * Tests the pure routing logic for all supported deep link patterns:
 * - Restaurant navigation (list, detail)
 * - Modal navigation (filter, submit review, confirm, date picker)
 * - Settings tab navigation
 * - Edge cases (empty paths, missing parameters)
 */
class DeepLinkProcessorTest {
    
    private val mockCoordinator = MockAppCoordinator()
    
    // === RESTAURANT DEEP LINK TESTS ===
    
    @Test
    fun testRestaurantListDeepLink() {
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_RESTAURANTS,
            pathSegments = emptyList(),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.navigateToScreenCalled)
        assertEquals(Destination.RestaurantList, mockCoordinator.lastDestination)
    }
    
    @Test
    fun testRestaurantDetailDeepLink() {
        val restaurantId = "12345"
        
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_RESTAURANTS,
            pathSegments = listOf(restaurantId),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.navigateToScreenCalled)
        assertEquals(Destination.RestaurantDetail(restaurantId), mockCoordinator.lastDestination)
    }
    
    @Test
    fun testRestaurantDetailWithExtraSegmentsIgnoresExtra() {
        val restaurantId = "12345"
        
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_RESTAURANTS,
            pathSegments = listOf(restaurantId, "extra", "segments"),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.navigateToScreenCalled)
        assertEquals(Destination.RestaurantDetail(restaurantId), mockCoordinator.lastDestination)
    }
    
    // === MODAL DEEP LINK TESTS ===
    
    @Test
    fun testFilterModalDeepLink() {
        val filters = listOf("tag1", "tag2", "tag3")
        
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = listOf(DeepLinkConstants.PATH_FILTER),
            queryParams = mapOf(DeepLinkConstants.QUERY_PARAM_FILTERS to "tag1,tag2,tag3"),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.showFilterModalCalled)
        assertEquals(filters, mockCoordinator.lastFilterSelection)
    }
    
    @Test
    fun testFilterModalWithNoFilters() {
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = listOf(DeepLinkConstants.PATH_FILTER),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.showFilterModalCalled)
        assertEquals(emptyList(), mockCoordinator.lastFilterSelection)
    }
    
    @Test
    fun testSubmitReviewModalDeepLink() {
        val restaurantId = "67890"
        
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = listOf(DeepLinkConstants.PATH_SUBMIT_REVIEW, restaurantId),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.submitReviewCalled)
        assertEquals(restaurantId, mockCoordinator.lastReviewRestaurantId)
    }
    
    @Test
    fun testConfirmDialogDeepLink() {
        val message = "Are you sure?"
        val confirmText = "Confirm"
        val cancelText = "Cancel"
        
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = listOf(DeepLinkConstants.PATH_CONFIRM),
            queryParams = mapOf(
                DeepLinkConstants.QUERY_PARAM_MESSAGE to message,
                DeepLinkConstants.QUERY_PARAM_CONFIRM_TEXT to confirmText,
                DeepLinkConstants.QUERY_PARAM_CANCEL_TEXT to cancelText
            ),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.showConfirmationCalled)
        assertEquals(message, mockCoordinator.lastConfirmMessage)
        assertEquals(confirmText, mockCoordinator.lastConfirmText)
        assertEquals(cancelText, mockCoordinator.lastCancelText)
    }
    
    @Test
    fun testConfirmDialogWithDefaults() {
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = listOf(DeepLinkConstants.PATH_CONFIRM),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.showConfirmationCalled)
        assertEquals(DeepLinkConstants.DEFAULT_CONFIRM_MESSAGE, mockCoordinator.lastConfirmMessage)
        assertEquals(DeepLinkConstants.DEFAULT_CONFIRM_TEXT, mockCoordinator.lastConfirmText)
        assertEquals(DeepLinkConstants.DEFAULT_CANCEL_TEXT, mockCoordinator.lastCancelText)
    }
    
    @Test
    fun testDatePickerModalDeepLink() {
        val initialDate = "2026-02-25"
        
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = listOf(DeepLinkConstants.PATH_DATE_PICKER),
            queryParams = mapOf(DeepLinkConstants.QUERY_PARAM_INITIAL_DATE to initialDate),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.showModalCalled)
        assertEquals(initialDate, mockCoordinator.lastDatePickerInitialDate)
    }
    
    // === SETTINGS DEEP LINK TESTS ===
    
    @Test
    fun testSettingsTabDeepLink() {
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_SETTINGS,
            pathSegments = emptyList(),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.selectTabCalled)
        assertEquals(DeepLinkConstants.TAB_ID_SETTINGS, mockCoordinator.lastTabId)
    }
    
    // === EDGE CASES ===
    
    @Test
    fun testInvalidHostIsIgnored() {
        DeepLinkProcessor.processDeepLink(
            host = "invalid_host",
            pathSegments = emptyList(),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertEquals(0, mockCoordinator.callCount)
    }
    
    @Test
    fun testEmptyModalPathSegmentsIsIgnored() {
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = emptyList(),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertEquals(0, mockCoordinator.callCount)
    }
    
    @Test
    fun testSubmitReviewWithoutRestaurantIdIsIgnored() {
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = listOf(DeepLinkConstants.PATH_SUBMIT_REVIEW),
            queryParams = emptyMap(),
            coordinator = mockCoordinator
        )
        
        assertEquals(0, mockCoordinator.callCount)
    }
    
    @Test
    fun testFilterParsingTrimsWhitespace() {
        DeepLinkProcessor.processDeepLink(
            host = DeepLinkConstants.HOST_MODAL,
            pathSegments = listOf(DeepLinkConstants.PATH_FILTER),
            queryParams = mapOf(DeepLinkConstants.QUERY_PARAM_FILTERS to " tag1 , tag2 , tag3 "),
            coordinator = mockCoordinator
        )
        
        assertTrue(mockCoordinator.showFilterModalCalled)
        assertEquals(listOf("tag1", "tag2", "tag3"), mockCoordinator.lastFilterSelection)
    }
}

// Mock AppCoordinator for testing
class MockAppCoordinator : AppCoordinator() {
    
    var callCount = 0
        private set
    
    // Navigation tracking
    var navigateToScreenCalled = false
        private set
    var lastDestination: Destination? = null
        private set
    
    var showFilterModalCalled = false
        private set
    var lastFilterSelection: List<String>? = null
        private set
    
    var submitReviewCalled = false
        private set
    var lastReviewRestaurantId: String? = null
        private set
    
    var showConfirmationCalled = false
        private set
    var lastConfirmMessage: String? = null
        private set
    var lastConfirmText: String? = null
        private set
    var lastCancelText: String? = null
        private set
    
    var showModalCalled = false
        private set
    var lastDatePickerInitialDate: String? = null
        private set
    
    var selectTabCalled = false
        private set
    var lastTabId: String? = null
        private set
    
    override fun navigateToScreen(destination: Destination) {
        navigateToScreenCalled = true
        lastDestination = destination
        callCount++
    }
    
    override fun showFilterModal(preSelectedFilters: List<String>) {
        showFilterModalCalled = true
        lastFilterSelection = preSelectedFilters
        callCount++
    }
    
    override fun submitReview(restaurantId: String) {
        submitReviewCalled = true
        lastReviewRestaurantId = restaurantId
        callCount++
    }
    
    override fun showConfirmation(message: String, confirmText: String, cancelText: String) {
        showConfirmationCalled = true
        lastConfirmMessage = message
        lastConfirmText = confirmText
        lastCancelText = cancelText
        callCount++
    }
    
    override fun showModal(destination: ModalDestination) {
        if (destination is ModalDestination.DatePicker) {
            showModalCalled = true
            lastDatePickerInitialDate = destination.initialDate
            callCount++
        }
    }
    
    override fun selectTab(tabId: String) {
        selectTabCalled = true
        lastTabId = tabId
        callCount++
    }
}
