package io.umain.munchies.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppCoordinator {
    
    private val _navigationStack = MutableStateFlow<List<Destination>>(
        listOf(Destination.RestaurantList)
    )
    val navigationStack: StateFlow<List<Destination>> = _navigationStack.asStateFlow()
    
    private val _currentDestination = MutableStateFlow<Destination>(Destination.RestaurantList)
    val currentDestination: StateFlow<Destination> = _currentDestination.asStateFlow()
    
    fun navigateToRestaurantDetail(restaurantId: String) {
        val destination = Destination.RestaurantDetail(restaurantId)
        _navigationStack.value = _navigationStack.value + destination
        _currentDestination.value = destination
    }
    
    fun navigateBack(): Boolean {
        if (_navigationStack.value.size <= 1) {
            return false
        }
        
        val newStack = _navigationStack.value.dropLast(1)
        _navigationStack.value = newStack
        _currentDestination.value = newStack.last()
        return true
    }
    
    fun navigateToRoot() {
        _navigationStack.value = listOf(Destination.RestaurantList)
        _currentDestination.value = Destination.RestaurantList
    }
}
