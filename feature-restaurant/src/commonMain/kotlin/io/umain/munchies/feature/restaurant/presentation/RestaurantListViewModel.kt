package io.umain.munchies.feature.restaurant.presentation

import io.umain.munchies.core.lifecycle.KmpViewModel
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.core.localization.stringResource
import io.umain.munchies.core.state.ViewModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.Filter
import io.umain.munchies.feature.restaurant.presentation.model.toCardData
import io.umain.munchies.feature.restaurant.presentation.model.toFilterChipData
import kotlinx.coroutines.delay

class RestaurantListViewModel(
    private val repository: RestaurantRepository,
): KmpViewModel(), ViewModelState<RestaurantListUiState> {
    private val _stateFlow = MutableStateFlow<RestaurantListUiState>(RestaurantListUiState.Loading)
    override val stateFlow: StateFlow<RestaurantListUiState> = _stateFlow
    private var state: RestaurantListUiState
        get() = _stateFlow.value
        set(value) { _stateFlow.value = value }

    private val _selectedFilters = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilters: StateFlow<Set<String>> = _selectedFilters
    private var selectedFiltersState: Set<String>
        get() = _selectedFilters.value
        set(value) { _selectedFilters.value = value }

    private var allRestaurants: List<Restaurant> = emptyList()
    private var allFilters: List<Filter> = emptyList()

    fun load() {
        scope.launch {
            try {
                allRestaurants = repository.getRestaurants()
                val filterIds = allRestaurants.flatMap { it.filterIds }.distinct()
                allFilters = filterIds.mapNotNull { repository.getFilterById(it) }
                state = RestaurantListUiState.Success(
                    restaurants = allRestaurants.map { it.toCardData(stringResource(StringResources.rating_format, it.rating.toDouble())) },
                    filters = allFilters.map { it.toFilterChipData() }
                )
            } catch (t: Throwable) {
                state = RestaurantListUiState.Error(message = t.message ?: "Unknown error")
            }
        }
    }

    fun toggleFilter(filterId: String) {
        val current = selectedFiltersState.toMutableSet()
        if (current.contains(filterId)) current.remove(filterId) else current.add(filterId)
        selectedFiltersState = current
        applyFilters()
    }

    private fun applyFilters() {
        state = state.copyIsFiltering(true)
        scope.launch {
            delay(200)
            try {
                val restaurants = if (selectedFiltersState.isEmpty()) {
                    allRestaurants
                } else {
                    allRestaurants.filter { restaurant ->
                        restaurant.filterIds.any { it in selectedFiltersState }
                    }
                }
                state = RestaurantListUiState.Success(
                    restaurants = restaurants.map {
                        it.toCardData(
                            stringResource(
                                StringResources.rating_format,
                                it.rating.toDouble()
                            )
                        )
                    },
                    filters = allFilters.map {
                        it.toFilterChipData(
                            selectedFiltersState.contains(
                                it.id
                            )
                        )
                    }
                )
            } catch (t: Throwable) {
                state =
                    RestaurantListUiState.Error(message = t.message ?: "Unknown error")
            }
            state = state.copyIsFiltering(false)
        }
    }

    private fun RestaurantListUiState.copyIsFiltering(isFiltering: Boolean): RestaurantListUiState {
        return when(val current = this) {
            is RestaurantListUiState.Success -> current.copy(isFiltering = isFiltering)
            else -> current
        }
    }
}
