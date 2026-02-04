package io.umain.munchies.feature.restaurant.presentation

import io.umain.munchies.core.lifecycle.KmpViewModel
import io.umain.munchies.core.state.ViewModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState

class RestaurantDetailViewModel(
    private val repository: RestaurantRepository,
): KmpViewModel(), ViewModelState<RestaurantDetailUiState> {
    private val _stateFlow =
        MutableStateFlow<RestaurantDetailUiState>(RestaurantDetailUiState.Loading)

    override val stateFlow: StateFlow<RestaurantDetailUiState> = _stateFlow

    fun load(restaurantId: String) {
        scope.launch {
            try {
                val restaurants = repository.getRestaurants()
                val restaurant = restaurants.find { it.id == restaurantId }
                if (restaurant != null) {
                    val status = repository.getRestaurantsOpenById(restaurantId)
                    _stateFlow.value = RestaurantDetailUiState.Success(restaurant, status)
                } else {
                    _stateFlow.value = RestaurantDetailUiState.Error("Not found")
                }
            } catch (t: Throwable) {
                _stateFlow.value = RestaurantDetailUiState.Error(t.message ?: "Unknown error")
            }
        }
    }
}
