package io.umain.munchies.feature.restaurant.presentation

import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.navigation.ModalDestination
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for RestaurantDetailViewModel navigation consequences.
 *
 * Tests the pattern where a SharedViewModel (RestaurantDetailViewModel) triggers
 * navigation through a feature-scoped NavigationViewModel when business logic completes.
 */
class RestaurantDetailViewModelTest {

    private class FakeNavigationDispatcher : NavigationDispatcher {
        var lastModalDestination: ModalDestination? = null
        var navigateBackCalled = false

        override fun navigate(destination: Any) {}
        override fun navigateBack() {
            navigateBackCalled = true
        }

        override fun presentModal(destination: ModalDestination) {
            lastModalDestination = destination
        }

        override fun dismissModal() {}
        override fun dismissAllModals() {}
        override fun selectTab(tabId: String) {}
    }

    private class FakeRestaurantRepository : RestaurantRepository {
        var submitReviewResult = true
        var shouldThrow = false
        var throwMessage = "Unknown error"

        override suspend fun getFilterById(id: String) = null
        override suspend fun getRestaurants() = emptyList()
        override suspend fun getRestaurantsOpenById(id: String) = throw UnsupportedOperationException()

        override suspend fun submitReview(restaurantId: String, rating: Int, comment: String): Boolean {
            if (shouldThrow) {
                throw RuntimeException(throwMessage)
            }
            return submitReviewResult
        }
    }

    @Test
    fun submitReview_onSuccess_showsSuccessModal() {
        // Arrange
        val fakeDispatcher = FakeNavigationDispatcher()
        val fakeRepository = FakeRestaurantRepository()
        fakeRepository.submitReviewResult = true

        val navViewModel = RestaurantNavigationViewModel(fakeDispatcher)
        val viewModel = RestaurantDetailViewModel(
            restaurantId = "restaurant-123",
            repository = fakeRepository,
            navigationViewModel = navViewModel
        )

        // Act
        viewModel.submitReview(5, "Great food!")

        // Wait a bit for coroutine to complete
        Thread.sleep(100)

        // Assert
        val lastModal = fakeDispatcher.lastModalDestination
        assertEquals(true, lastModal is ModalDestination.ReviewSuccessModal)
    }

    @Test
    fun submitReview_onFailure_showsErrorAlert() {
        // Arrange
        val fakeDispatcher = FakeNavigationDispatcher()
        val fakeRepository = FakeRestaurantRepository()
        fakeRepository.submitReviewResult = false

        val navViewModel = RestaurantNavigationViewModel(fakeDispatcher)
        val viewModel = RestaurantDetailViewModel(
            restaurantId = "restaurant-123",
            repository = fakeRepository,
            navigationViewModel = navViewModel
        )

        // Act
        viewModel.submitReview(3, "Good but pricey")

        // Wait a bit for coroutine to complete
        Thread.sleep(100)

        // Assert
        val lastModal = fakeDispatcher.lastModalDestination
        assertEquals(true, lastModal is ModalDestination.ReviewErrorAlert)
        val errorAlert = lastModal as? ModalDestination.ReviewErrorAlert
        assertEquals("Failed to submit review", errorAlert?.message)
    }

    @Test
    fun submitReview_onException_showsErrorAlertWithMessage() {
        // Arrange
        val fakeDispatcher = FakeNavigationDispatcher()
        val fakeRepository = FakeRestaurantRepository()
        fakeRepository.shouldThrow = true
        fakeRepository.throwMessage = "Network error"

        val navViewModel = RestaurantNavigationViewModel(fakeDispatcher)
        val viewModel = RestaurantDetailViewModel(
            restaurantId = "restaurant-123",
            repository = fakeRepository,
            navigationViewModel = navViewModel
        )

        // Act
        viewModel.submitReview(4, "Nice place!")

        // Wait a bit for coroutine to complete
        Thread.sleep(100)

        // Assert
        val lastModal = fakeDispatcher.lastModalDestination
        assertEquals(true, lastModal is ModalDestination.ReviewErrorAlert)
        val errorAlert = lastModal as? ModalDestination.ReviewErrorAlert
        assertEquals("Network error", errorAlert?.message)
    }

    @Test
    fun submitReview_callsNavigationViewModelMethods() {
        // This test verifies that the SharedViewModel (RestaurantDetailViewModel)
        // correctly delegates navigation to the feature-scoped NavigationViewModel.
        // This is the core pattern: business logic triggers navigation.

        // Arrange
        val fakeDispatcher = FakeNavigationDispatcher()
        val fakeRepository = FakeRestaurantRepository()

        val navViewModel = RestaurantNavigationViewModel(fakeDispatcher)
        val viewModel = RestaurantDetailViewModel(
            restaurantId = "restaurant-123",
            repository = fakeRepository,
            navigationViewModel = navViewModel
        )

        // Act - Success case
        fakeRepository.submitReviewResult = true
        viewModel.submitReview(5, "Perfect!")
        Thread.sleep(100)

        // Assert
        val successModal = fakeDispatcher.lastModalDestination
        assertEquals(true, successModal is ModalDestination.ReviewSuccessModal)

        // Act - Error case
        fakeRepository.submitReviewResult = false
        viewModel.submitReview(2, "Bad")
        Thread.sleep(100)

        // Assert
        val errorModal = fakeDispatcher.lastModalDestination
        assertEquals(true, errorModal is ModalDestination.ReviewErrorAlert)
    }
}
