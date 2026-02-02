package io.umain.munchies.feature.restaurant.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState

class RestaurantDetailViewModel(
    private val repository: RestaurantRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _uiState = MutableStateFlow<RestaurantDetailUiState>(RestaurantDetailUiState.Loading)
    val uiState: StateFlow<RestaurantDetailUiState> = _uiState

    fun load(restaurantId: String) {
        scope.launch {
            try {
                val restaurant = repository.getRestaurantById(restaurantId)
                if (restaurant != null) _uiState.value = RestaurantDetailUiState.Success(restaurant)
                else _uiState.value = RestaurantDetailUiState.Error("Not found")
            } catch (t: Throwable) {
                _uiState.value = RestaurantDetailUiState.Error(t.message ?: "Unknown error")
            }
        }
    }
}
