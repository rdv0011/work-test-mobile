package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.core.lifecycle.KmpViewModel
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.ModalDestination

/**
 * Restaurant feature-scoped navigation ViewModel.
 * Owns all navigation actions specific to the restaurant feature.
 */
class RestaurantNavigationViewModel(
    private val dispatcher: NavigationDispatcher
) : KmpViewModel() {
    
    fun showRestaurantDetail(restaurantId: String) {
        dispatcher.navigate(Destination.RestaurantDetail(restaurantId))
    }
    
    fun showFilterModal(preSelectedFilters: List<String> = emptyList()) {
        dispatcher.presentModal(ModalDestination.Filter(preSelectedFilters))
    }
    
    fun showSubmitReviewModal(restaurantId: String) {
        dispatcher.presentModal(ModalDestination.SubmitReviewModal(restaurantId))
    }
    
    fun showReviewSuccessModal() {
        dispatcher.presentModal(ModalDestination.ReviewSuccessModal)
    }
    
    fun showReviewErrorAlert(message: String) {
        dispatcher.presentModal(ModalDestination.ReviewErrorAlert(message))
    }
    
    fun navigateBack() {
        dispatcher.navigateBack()
    }
}
