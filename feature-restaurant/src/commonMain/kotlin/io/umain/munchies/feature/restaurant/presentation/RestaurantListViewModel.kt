package io.umain.munchies.feature.restaurant.presentation

import io.umain.munchies.core.lifecycle.KmpViewModel
import io.umain.munchies.core.state.ViewModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.Filter

class RestaurantListViewModel(
    private val repository: RestaurantRepository,
): KmpViewModel(), ViewModelState<RestaurantListUiState> {
    private val _stateFlow = MutableStateFlow<RestaurantListUiState>(RestaurantListUiState.Loading)
    override val stateFlow: StateFlow<RestaurantListUiState> = _stateFlow

    private val _selectedFilters = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilters: StateFlow<Set<String>> = _selectedFilters

    private var allRestaurants: List<Restaurant> = emptyList()
    private var allFilters: List<Filter> = emptyList()

    fun load() {
        scope.launch {
            try {
                allRestaurants = repository.getRestaurants()
                val filterIds = allRestaurants.flatMap { it.filterIds }.distinct()
                allFilters = filterIds.mapNotNull { repository.getFilterById(it) }
                _stateFlow.value = RestaurantListUiState.Success(restaurants = allRestaurants, filters = allFilters)
            } catch (t: Throwable) {
                _stateFlow.value = RestaurantListUiState.Error(message = t.message ?: "Unknown error")
            }
        }
    }

    fun toggleFilter(filterId: String) {
        val current = _selectedFilters.value.toMutableSet()
        if (current.contains(filterId)) current.remove(filterId) else current.add(filterId)
        _selectedFilters.value = current
        applyFilters()
    }

    private fun applyFilters() {
        scope.launch {
            _stateFlow.value = RestaurantListUiState.Loading
            try {
                val restaurants = if (_selectedFilters.value.isEmpty()) {
                    allRestaurants
                } else {
                    allRestaurants.filter { restaurant ->
                        restaurant.filterIds.any { it in _selectedFilters.value }
                    }
                }
                _stateFlow.value = RestaurantListUiState.Success(restaurants = restaurants, filters = allFilters)
            } catch (t: Throwable) {
                _stateFlow.value = RestaurantListUiState.Error(message = t.message ?: "Unknown error")
            }
        }
    }
}
