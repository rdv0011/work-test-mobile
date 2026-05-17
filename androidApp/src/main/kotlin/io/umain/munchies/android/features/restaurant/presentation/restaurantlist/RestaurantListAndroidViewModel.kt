package io.umain.munchies.android.features.restaurant.presentation.restaurantlist

import androidx.lifecycle.ViewModel
import io.umain.munchies.core.lifecycle.Closeable
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel as SharedRestaurantListViewModel
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import kotlinx.coroutines.flow.StateFlow

class RestaurantListAndroidViewModel(
    private val shared: SharedRestaurantListViewModel
) : ViewModel(), Closeable {
    val uiState: StateFlow<RestaurantListUiState> = shared.stateFlow
    val selectedFilters = shared.selectedFilters
    fun toggleFilter(filterId: String) = shared.toggleFilter(filterId)

    override fun onCleared() {
        super.onCleared()
        shared.close()
    }

    override fun close() {
        shared.close()
    }
}
