package io.umain.munchies.android.features.restaurant.presentation.restaurantdetail

import androidx.lifecycle.ViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel as SharedRestaurantDetailViewModel
import kotlinx.coroutines.flow.StateFlow
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState

class RestaurantDetailAndroidViewModel(
    private val shared: SharedRestaurantDetailViewModel
) : ViewModel() {
    val uiState: StateFlow<RestaurantDetailUiState> = shared.stateFlow

    override fun onCleared() {
        super.onCleared()
        shared.close()
    }

    fun load(restaurantId: String) = shared.load(restaurantId)
}
