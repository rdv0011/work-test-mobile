package io.umain.munchies.feature.restaurant.presentation.state

import io.umain.munchies.core.state.ViewState
import io.umain.munchies.feature.restaurant.domain.model.Filter
import io.umain.munchies.feature.restaurant.presentation.model.FilterChipData
import io.umain.munchies.feature.restaurant.presentation.model.RestaurantCardData

sealed class RestaurantListUiState: ViewState {
    object Loading : RestaurantListUiState()
    data class Success(
        val restaurants: List<RestaurantCardData>,
        val filters: List<FilterChipData>,
        val selectedFilterIds: Set<String> = emptySet(),
        val isFiltering: Boolean = false
    ) : RestaurantListUiState()
    data class Error(val message: String) : RestaurantListUiState()
}
