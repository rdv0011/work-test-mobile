package io.umain.munchies.feature.restaurant.presentation.state

import io.umain.munchies.core.state.ViewState
import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus
import io.umain.munchies.feature.restaurant.presentation.model.DetailCardData

sealed class RestaurantDetailUiState: ViewState {
    object Loading : RestaurantDetailUiState()
    data class Success(val detailCardData: DetailCardData) : RestaurantDetailUiState()
    data class Error(val message: String) : RestaurantDetailUiState()
}
