package io.umain.munchies.android.features.restaurant.presentation.restaurantlist

import androidx.lifecycle.ViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel as SharedRestaurantListViewModel
import kotlinx.coroutines.flow.StateFlow
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState

class RestaurantListAndroidViewModel(
    private val shared: SharedRestaurantListViewModel
) : ViewModel() {
    val uiState: StateFlow<RestaurantListUiState> = shared.uiState
    val selectedFilters = shared.selectedFilters

    fun load() = shared.load()
    fun toggleFilter(filterId: String) = shared.toggleFilter(filterId)
}
