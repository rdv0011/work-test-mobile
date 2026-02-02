package io.umain.munchies.android.features.restaurantdetail

import androidx.lifecycle.ViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel as SharedRestaurantDetailViewModel
import kotlinx.coroutines.flow.StateFlow
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState

class RestaurantDetailAndroidViewModel(
    private val shared: SharedRestaurantDetailViewModel
) : ViewModel() {
    val uiState: StateFlow<RestaurantDetailUiState> = shared.uiState

    fun load(restaurantId: String) = shared.load(restaurantId)
}
