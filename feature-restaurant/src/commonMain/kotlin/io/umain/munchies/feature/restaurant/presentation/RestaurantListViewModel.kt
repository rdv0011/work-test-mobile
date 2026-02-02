package io.umain.munchies.feature.restaurant.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import io.umain.munchies.feature.restaurant.domain.model.Filter

class RestaurantListViewModel(
    private val repository: RestaurantRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _uiState = MutableStateFlow<RestaurantListUiState>(RestaurantListUiState.Loading)
    val uiState: StateFlow<RestaurantListUiState> = _uiState

    private val _selectedFilters = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilters: StateFlow<Set<String>> = _selectedFilters

    fun load() {
        scope.launch {
            try {
                val restaurants = repository.getRestaurants()
                val filters = repository.getFilters()
                _uiState.value = RestaurantListUiState.Success(restaurants = restaurants, filters = filters)
            } catch (t: Throwable) {
                _uiState.value = RestaurantListUiState.Error(message = t.message ?: "Unknown error")
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
            _uiState.value = RestaurantListUiState.Loading
            try {
                val restaurants = if (_selectedFilters.value.isEmpty()) {
                    repository.getRestaurants()
                } else {
                    repository.getRestaurantsByFilter(_selectedFilters.value)
                }
                val filters = repository.getFilters()
                _uiState.value = RestaurantListUiState.Success(restaurants = restaurants, filters = filters)
            } catch (t: Throwable) {
                _uiState.value = RestaurantListUiState.Error(message = t.message ?: "Unknown error")
            }
        }
    }
}
