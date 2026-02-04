package io.umain.munchies.feature.restaurant.presentation.state

import io.umain.munchies.core.state.ViewState
import io.umain.munchies.feature.restaurant.domain.model.Restaurant

sealed class RestaurantDetailUiState: ViewState {
    object Loading : RestaurantDetailUiState()
    data class Success(val restaurant: Restaurant) : RestaurantDetailUiState()
    data class Error(val message: String) : RestaurantDetailUiState()
}
