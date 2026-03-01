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
    
    fun showRestaurantList() {
        dispatcher.navigate(Destination.RestaurantList)
    }
    
    fun showFilterModal(preSelectedFilters: List<String> = emptyList()) {
        dispatcher.presentModal(ModalDestination.Filter(preSelectedFilters))
    }
}
