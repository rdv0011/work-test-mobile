package io.umain.munchies.feature.restaurant.presentation.state

import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.Filter

sealed class RestaurantListUiState {
    object Loading : RestaurantListUiState()
    data class Success(
        val restaurants: List<Restaurant>,
        val filters: List<Filter>,
        val selectedFilterIds: Set<String> = emptySet()
    ) : RestaurantListUiState()
    data class Error(val message: String) : RestaurantListUiState()
}
