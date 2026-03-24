package io.umain.munchies.android.features.restaurant.presentation.restaurantdetail

import androidx.lifecycle.ViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState
import kotlinx.coroutines.flow.StateFlow

class RestaurantDetailAndroidViewModel(
    private val shared: RestaurantDetailViewModel
) : ViewModel() {
    val uiState: StateFlow<RestaurantDetailUiState> = shared.stateFlow

    fun load() = shared.load()

    /**
     * Call this manually when the Koin scope is closed to ensure cleanup.
     */
    fun close() {
        shared.close()
    }

    override fun onCleared() {
        super.onCleared()
        // No-op: cleanup is now handled by close()
    }
}