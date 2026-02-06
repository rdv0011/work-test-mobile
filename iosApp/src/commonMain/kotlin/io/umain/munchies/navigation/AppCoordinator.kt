package io.umain.munchies.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class NavigationEvent {
    data class Push(val destination: Destination) : NavigationEvent()
    data object Pop : NavigationEvent()
    data object PopToRoot : NavigationEvent()
}

class AppCoordinator {
    
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(
        extraBufferCapacity = 10
    )
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    fun navigateTo(destination: Destination) {
        _navigationEvents.tryEmit(NavigationEvent.Push(destination))
    }
    
    fun navigateToRestaurantDetail(restaurantId: String) {
        navigateTo(Destination.RestaurantDetail(restaurantId))
    }
    
    fun navigateBack() {
        _navigationEvents.tryEmit(NavigationEvent.Pop)
    }
    
    fun navigateToRoot() {
        _navigationEvents.tryEmit(NavigationEvent.PopToRoot)
    }
}
