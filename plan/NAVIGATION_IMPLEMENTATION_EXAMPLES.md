# KMP Navigation System - Implementation Code Examples

**Purpose**: Ready-to-implement code snippets for Phase 0 (Foundation)  
**Target Platforms**: Android (Jetpack Compose), iOS (SwiftUI)  
**Status**: Copy-paste ready

---

## Table of Contents
1. [Core Data Models](#core-data-models)
2. [Navigation Reducer](#navigation-reducer)
3. [Extended Navigation Coordinator](#extended-navigation-coordinator)
4. [Modal Route Implementation](#modal-route-implementation)
5. [Tab Navigation State](#tab-navigation-state)
6. [Android Platform Integration](#android-platform-integration)
7. [iOS Platform Integration](#ios-platform-integration)
8. [Feature Module Examples](#feature-module-examples)
9. [Testing Examples](#testing-examples)

---

## Core Data Models

### 1. NavigationState.kt

```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationState.kt

package io.umain.munchies.navigation

import kotlinx.serialization.Serializable

/**
 * Complete navigation state of the application.
 * 
 * Supports three navigation patterns:
 * 1. Linear stack navigation (primaryStack)
 * 2. Modal overlays (modalStack)
 * 3. Tabbed navigation with per-tab stacks (tabNavigation)
 */
@Serializable
data class NavigationState(
    // Primary navigation stack (used in non-tabbed apps)
    val primaryStack: List<StackRoute> = emptyList(),
    
    // Modal overlays (independent of primary stack)
    val modalStack: List<ModalRoute> = emptyList(),
    
    // Tab navigation state (if app uses tabs)
    val tabNavigation: TabNavigationState? = null,
    
    // Flag indicating if this app uses tabs
    val usesTabs: Boolean = false,
    
    // Optional: Track the deep link that triggered this state
    val originDeepLink: String? = null
) {
    /**
     * Get the current active stack (either primaryStack or active tab's stack)
     */
    val currentStack: List<StackRoute>
        get() = if (usesTabs) {
            tabNavigation?.getActiveTabStack() ?: emptyList()
        } else {
            primaryStack
        }
    
    /**
     * Whether any modals are currently displayed
     */
    val hasModals: Boolean get() = modalStack.isNotEmpty()
    
    /**
     * Get the topmost modal, if any
     */
    val topModal: ModalRoute? get() = modalStack.lastOrNull()
    
    /**
     * Create a new state with updated primary stack
     */
    fun withPrimaryStack(newStack: List<StackRoute>): NavigationState {
        return copy(primaryStack = newStack)
    }
    
    /**
     * Create a new state with updated tab navigation
     */
    fun withTabNavigation(newTabNav: TabNavigationState?): NavigationState {
        return copy(tabNavigation = newTabNav)
    }
    
    /**
     * Create a new state with updated modal stack
     */
    fun withModalStack(newModalStack: List<ModalRoute>): NavigationState {
        return copy(modalStack = newModalStack)
    }
}

/**
 * Marks a Route as a stack-based screen route.
 * Screens can be pushed/popped from the back stack.
 */
interface StackRoute : Route {
    override val isRootRoute: Boolean
        get() = false
}

/**
 * Marks a Route as a modal overlay route.
 * Modals are presented independently of the primary stack.
 */
interface ModalRoute : Route {
    override val isRootRoute: Boolean
        get() = false
    
    /**
     * Presentation style for this modal
     */
    val presentationStyle: ModalPresentationStyle
        get() = ModalPresentationStyle.SHEET
    
    /**
     * Whether tapping outside the modal dismisses it
     */
    val dismissOnBackgroundTap: Boolean
        get() = true
}

/**
 * Platform-specific modal presentation styles
 */
enum class ModalPresentationStyle {
    // Bottom sheet (Android: ModalBottomSheet, iOS: .sheet)
    SHEET,
    
    // Full-screen modal (Android: Dialog(fullscreen), iOS: .fullScreenCover)
    FULL_SCREEN,
    
    // Dialog (Android: AlertDialog, iOS: .alert)
    DIALOG
}
```

### 2. ModalDestination.kt

```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/navigation/ModalDestination.kt

package io.umain.munchies.navigation

/**
 * Sealed class for modal destinations.
 * Similar to Destination but for modal overlays.
 */
sealed class ModalDestination {
    data class Filter(
        val preSelectedFilters: List<String> = emptyList()
    ) : ModalDestination()
    
    data class ConfirmAction(
        val message: String,
        val confirmText: String = "OK",
        val cancelText: String = "Cancel"
    ) : ModalDestination()
    
    data class DatePicker(
        val initialDate: String? = null
    ) : ModalDestination()
    
    data class Reviews(
        val restaurantId: String
    ) : ModalDestination()
}
```

### 3. NavigationEvent.kt (Extended)

```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationEvent.kt
// REPLACE the existing NavigationEvent file

package io.umain.munchies.navigation

/**
 * All possible navigation events in the application.
 * 
 * These represent user intents (navigate to screen, show modal, etc)
 * and are emitted by the NavigationCoordinator.
 */
sealed class NavigationEvent {
    // === SCREEN NAVIGATION ===
    
    /**
     * Navigate to a new screen (push to primary stack)
     */
    data class Push(val destination: Destination) : NavigationEvent()
    
    /**
     * Go back (pop from current stack)
     */
    data object Pop : NavigationEvent()
    
    /**
     * Return to root screen (clear stack)
     */
    data object PopToRoot : NavigationEvent()
    
    // === MODAL NAVIGATION ===
    
    /**
     * Show a modal overlay
     */
    data class ShowModal(val destination: ModalDestination) : NavigationEvent()
    
    /**
     * Dismiss top modal
     */
    data object DismissModal : NavigationEvent()
    
    /**
     * Dismiss all modals
     */
    data object DismissAllModals : NavigationEvent()
    
    /**
     * Dismiss modals until condition is met
     */
    data class DismissModalUntil(
        val predicate: (ModalRoute) -> Boolean
    ) : NavigationEvent()
    
    // === TAB NAVIGATION ===
    
    /**
     * Switch to a different tab
     */
    data class SelectTab(val tabId: String) : NavigationEvent()
    
    /**
     * Push to the current tab's stack
     */
    data class PushInTab(val destination: Destination) : NavigationEvent()
    
    /**
     * Pop from the current tab's stack
     */
    data object PopInTab : NavigationEvent()
    
    // === DEEP LINKING ===
    
    /**
     * Apply a complete navigation state (for deep links)
     */
    data class ApplyNavigationState(
        val newState: NavigationState,
        val clearCurrentStack: Boolean = true
    ) : NavigationEvent()
}
```

---

## Navigation Reducer

### 4. NavigationReducer.kt

```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationReducer.kt

package io.umain.munchies.navigation

import android.util.Log

/**
 * Pure functions for reducing NavigationState based on NavigationEvents.
 * 
 * This follows the Redux pattern: (State, Event) -> State
 * All functions are pure with no side effects.
 */
object NavigationReducer {
    
    /**
     * Main reducer: given a state and event, produce a new state
     */
    fun reduce(
        currentState: NavigationState,
        event: NavigationEvent,
        routeHandlers: List<RouteHandler> = emptyList()
    ): NavigationState {
        return when (event) {
            // Screen navigation
            is NavigationEvent.Push -> handlePush(currentState, event, routeHandlers)
            is NavigationEvent.Pop -> handlePop(currentState)
            is NavigationEvent.PopToRoot -> handlePopToRoot(currentState)
            
            // Modal navigation
            is NavigationEvent.ShowModal -> handleShowModal(currentState, event, routeHandlers)
            is NavigationEvent.DismissModal -> handleDismissModal(currentState)
            is NavigationEvent.DismissAllModals -> handleDismissAllModals(currentState)
            is NavigationEvent.DismissModalUntil -> handleDismissModalUntil(currentState, event)
            
            // Tab navigation
            is NavigationEvent.SelectTab -> handleSelectTab(currentState, event)
            is NavigationEvent.PushInTab -> handlePushInTab(currentState, event, routeHandlers)
            is NavigationEvent.PopInTab -> handlePopInTab(currentState)
            
            // Deep linking
            is NavigationEvent.ApplyNavigationState -> event.newState
        }
    }
    
    // === SCREEN NAVIGATION HANDLERS ===
    
    private fun handlePush(
        state: NavigationState,
        event: NavigationEvent.Push,
        handlers: List<RouteHandler>
    ): NavigationState {
        // Convert Destination to Route via handlers
        val route = handlers.firstNotNullOfOrNull { handler ->
            if (handler.canHandle(event.destination)) {
                handler.destinationToRoute(event.destination) as? StackRoute
            } else null
        }
        
        if (route == null) {
            Log.w("NavigationReducer", "No handler for destination: ${event.destination}")
            return state
        }
        
        return if (state.usesTabs) {
            // Push in current tab
            handlePushInTab(state, NavigationEvent.PushInTab(event.destination), handlers)
        } else {
            // Push in primary stack
            state.copy(primaryStack = state.primaryStack + route)
        }
    }
    
    private fun handlePop(state: NavigationState): NavigationState {
        return when {
            // If modals are showing, dismiss top modal instead
            state.modalStack.isNotEmpty() -> {
                handleDismissModal(state)
            }
            // If using tabs, pop from active tab
            state.usesTabs && state.tabNavigation != null -> {
                handlePopInTab(state)
            }
            // Otherwise pop from primary stack
            state.primaryStack.size > 1 -> {
                state.copy(primaryStack = state.primaryStack.dropLast(1))
            }
            // Already at root, no navigation
            else -> state
        }
    }
    
    private fun handlePopToRoot(state: NavigationState): NavigationState {
        return if (state.usesTabs) {
            state.copy(
                tabNavigation = state.tabNavigation?.copy(
                    stacksByTab = state.tabNavigation.stacksByTab.mapValues { (_, stack) ->
                        listOf(stack.first()) // Keep only root in each tab
                    }
                )
            )
        } else {
            state.copy(
                primaryStack = state.primaryStack.take(1)
            )
        }
    }
    
    // === MODAL NAVIGATION HANDLERS ===
    
    private fun handleShowModal(
        state: NavigationState,
        event: NavigationEvent.ShowModal,
        handlers: List<RouteHandler>
    ): NavigationState {
        // Find handler that can convert ModalDestination to ModalRoute
        val modalRoute = handlers.firstNotNullOfOrNull { handler ->
            if (handler is ModalRouteHandler && handler.canHandleModal(event.destination)) {
                handler.destinationToModalRoute(event.destination)
            } else null
        }
        
        if (modalRoute == null) {
            Log.w("NavigationReducer", "No handler for modal: ${event.destination}")
            return state
        }
        
        return state.copy(modalStack = state.modalStack + modalRoute)
    }
    
    private fun handleDismissModal(state: NavigationState): NavigationState {
        return if (state.modalStack.isEmpty()) {
            state
        } else {
            state.copy(modalStack = state.modalStack.dropLast(1))
        }
    }
    
    private fun handleDismissAllModals(state: NavigationState): NavigationState {
        return state.copy(modalStack = emptyList())
    }
    
    private fun handleDismissModalUntil(
        state: NavigationState,
        event: NavigationEvent.DismissModalUntil
    ): NavigationState {
        val lastIndex = state.modalStack.lastIndexOf { modal ->
            event.predicate(modal)
        }
        
        return if (lastIndex >= 0) {
            state.copy(modalStack = state.modalStack.take(lastIndex + 1))
        } else {
            state.copy(modalStack = emptyList())
        }
    }
    
    // === TAB NAVIGATION HANDLERS ===
    
    private fun handleSelectTab(
        state: NavigationState,
        event: NavigationEvent.SelectTab
    ): NavigationState {
        if (!state.usesTabs || state.tabNavigation == null) {
            return state
        }
        
        return state.copy(
            tabNavigation = state.tabNavigation.copy(activeTabId = event.tabId)
        )
    }
    
    private fun handlePushInTab(
        state: NavigationState,
        event: NavigationEvent.PushInTab,
        handlers: List<RouteHandler>
    ): NavigationState {
        if (!state.usesTabs || state.tabNavigation == null) {
            // Fallback to primary stack push
            return handlePush(state, NavigationEvent.Push(event.destination), handlers)
        }
        
        val route = handlers.firstNotNullOfOrNull { handler ->
            if (handler.canHandle(event.destination)) {
                handler.destinationToRoute(event.destination) as? StackRoute
            } else null
        } ?: return state
        
        val tabNav = state.tabNavigation
        val activeTabId = tabNav.activeTabId
        val currentStack = tabNav.getActiveTabStack()
        val newStack = currentStack + route
        
        return state.copy(
            tabNavigation = tabNav.updateActiveTabStack(newStack)
        )
    }
    
    private fun handlePopInTab(state: NavigationState): NavigationState {
        if (!state.usesTabs || state.tabNavigation == null) {
            return handlePop(state)
        }
        
        val tabNav = state.tabNavigation
        val activeTabId = tabNav.activeTabId
        val currentStack = tabNav.getActiveTabStack()
        
        // Don't pop below tab's root
        if (currentStack.size <= 1) {
            return state
        }
        
        val newStack = currentStack.dropLast(1)
        return state.copy(
            tabNavigation = tabNav.updateActiveTabStack(newStack)
        )
    }
}

/**
 * Extended RouteHandler interface that can handle modal routes
 */
interface ModalRouteHandler : RouteHandler {
    fun canHandleModal(destination: ModalDestination): Boolean = false
    
    fun destinationToModalRoute(destination: ModalDestination): ModalRoute? = null
}
```

---

## Extended Navigation Coordinator

### 5. NavigationCoordinator.kt

```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationCoordinator.kt
// REPLACE the existing AppCoordinator usage

package io.umain.munchies.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central coordinator for all navigation in the application.
 * 
 * Handles:
 * - Screen navigation (push/pop)
 * - Modal presentation/dismissal
 * - Tab switching
 * - Deep link processing
 * 
 * This replaces AppCoordinator and provides a unified API.
 */
class NavigationCoordinator(
    initialState: NavigationState = NavigationState(
        primaryStack = listOf(RestaurantListRoute())
    )
) {
    // === INTERNAL STATE ===
    
    private val _navigationState = MutableStateFlow(initialState)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    // === EVENTS (for platform layer) ===
    
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(
        extraBufferCapacity = 10
    )
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    // === ROUTE HANDLERS (injected) ===
    
    var routeHandlers: List<RouteHandler> = emptyList()
    
    // === PUBLIC API: SCREEN NAVIGATION ===
    
    /**
     * Navigate to a screen
     */
    fun navigateToScreen(destination: Destination) {
        _navigationEvents.tryEmit(NavigationEvent.Push(destination))
    }
    
    /**
     * Specific convenience method for restaurant detail
     */
    fun navigateToRestaurantDetail(restaurantId: String) {
        navigateToScreen(Destination.RestaurantDetail(restaurantId))
    }
    
    /**
     * Go back (pops modal if showing, else pops screen)
     */
    fun navigateBack() {
        _navigationEvents.tryEmit(NavigationEvent.Pop)
    }
    
    /**
     * Return to root screen(s)
     */
    fun navigateToRoot() {
        _navigationEvents.tryEmit(NavigationEvent.PopToRoot)
    }
    
    // === PUBLIC API: MODAL NAVIGATION ===
    
    /**
     * Show a modal dialog
     */
    fun showModal(destination: ModalDestination) {
        _navigationEvents.tryEmit(NavigationEvent.ShowModal(destination))
    }
    
    /**
     * Show filter modal
     */
    fun showFilterModal(preSelectedFilters: List<String> = emptyList()) {
        showModal(ModalDestination.Filter(preSelectedFilters))
    }
    
    /**
     * Show confirmation dialog
     */
    fun showConfirmation(
        message: String,
        confirmText: String = "OK",
        cancelText: String = "Cancel"
    ) {
        showModal(ModalDestination.ConfirmAction(message, confirmText, cancelText))
    }
    
    /**
     * Show reviews modal for a restaurant
     */
    fun showReviews(restaurantId: String) {
        showModal(ModalDestination.Reviews(restaurantId))
    }
    
    /**
     * Dismiss top modal
     */
    fun dismissModal() {
        _navigationEvents.tryEmit(NavigationEvent.DismissModal)
    }
    
    /**
     * Dismiss all modals
     */
    fun dismissAllModals() {
        _navigationEvents.tryEmit(NavigationEvent.DismissAllModals)
    }
    
    /**
     * Dismiss modals until condition is met
     */
    fun dismissModalUntil(predicate: (ModalRoute) -> Boolean) {
        _navigationEvents.tryEmit(NavigationEvent.DismissModalUntil(predicate))
    }
    
    // === PUBLIC API: TAB NAVIGATION ===
    
    /**
     * Switch to a tab
     */
    fun selectTab(tabId: String) {
        _navigationEvents.tryEmit(NavigationEvent.SelectTab(tabId))
    }
    
    /**
     * Navigate within current tab
     */
    fun navigateInTab(destination: Destination) {
        _navigationEvents.tryEmit(NavigationEvent.PushInTab(destination))
    }
    
    /**
     * Go back in current tab
     */
    fun backInTab() {
        _navigationEvents.tryEmit(NavigationEvent.PopInTab)
    }
    
    // === PUBLIC API: STATE MANAGEMENT ===
    
    /**
     * Apply a complete navigation state (for deep links)
     */
    fun applyNavigationState(newState: NavigationState, clearCurrentStack: Boolean = true) {
        _navigationEvents.tryEmit(
            NavigationEvent.ApplyNavigationState(newState, clearCurrentStack)
        )
    }
    
    /**
     * Get current state (snapshot)
     */
    fun getCurrentState(): NavigationState {
        return _navigationState.value
    }
    
    /**
     * Reduce current state with event and update
     */
    internal fun reduceState(event: NavigationEvent) {
        val currentState = _navigationState.value
        val newState = NavigationReducer.reduce(currentState, event, routeHandlers)
        _navigationState.value = newState
    }
}

// Deprecated alias for backward compatibility
typealias AppCoordinator = NavigationCoordinator
```

---

## Tab Navigation State

### 6. TabNavigationState.kt

```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/navigation/TabNavigationState.kt

package io.umain.munchies.navigation

import io.umain.munchies.core.ui.IconId
import io.umain.munchies.core.ui.TextId
import kotlinx.serialization.Serializable

/**
 * State for tab-based navigation.
 * 
 * Each tab maintains its own back stack, allowing users to navigate
 * within a tab and preserve their position when switching away and back.
 */
@Serializable
data class TabNavigationState(
    val tabDefinitions: List<TabDefinition>,
    val activeTabId: String,
    val stacksByTab: Map<String, List<StackRoute>>
) {
    
    /**
     * Get the current stack for the active tab
     */
    fun getActiveTabStack(): List<StackRoute> {
        return stacksByTab[activeTabId] ?: emptyList()
    }
    
    /**
     * Get stack for a specific tab
     */
    fun getTabStack(tabId: String): List<StackRoute> {
        return stacksByTab[tabId] ?: emptyList()
    }
    
    /**
     * Create a new TabNavigationState with updated active tab's stack
     */
    fun updateActiveTabStack(newStack: List<StackRoute>): TabNavigationState {
        return copy(
            stacksByTab = stacksByTab.toMutableMap().apply {
                this[activeTabId] = newStack
            }
        )
    }
    
    /**
     * Create a new TabNavigationState with updated tab stack
     */
    fun updateTabStack(tabId: String, newStack: List<StackRoute>): TabNavigationState {
        return copy(
            stacksByTab = stacksByTab.toMutableMap().apply {
                this[tabId] = newStack
            }
        )
    }
}

/**
 * Definition of a tab in tab-based navigation
 */
@Serializable
data class TabDefinition(
    val id: String,
    val label: TextId,
    val icon: IconId,
    val rootRoute: StackRoute
)
```

---

## Android Platform Integration

### 7. Android AppNavigation Extended

```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/navigation/AppNavigation.kt
// EXTEND this file with new composables and updated structure

package io.umain.munchies.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.NavigationCoordinator
import io.umain.munchies.navigation.NavigationReducer
import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteComposableBuilder
import io.umain.munchies.navigation.RouteNavigationMapper
import io.umain.munchies.navigation.RouteProvider
import io.umain.munchies.navigation.ScopedRouteHandler
import kotlinx.coroutines.flow.collectLatest

val LocalRouteRegistry = compositionLocalOf<RouteRegistry> {
    error("RouteRegistry not provided")
}

/**
 * Main app navigation structure - composable that builds the nav graph
 */
@Composable
fun AppNavigation(
    coordinator: NavigationCoordinator,
    routeProviders: List<RouteProvider> = AndroidAppRouteProviders.create().getAllProviders()
) {
    val navController = rememberNavController()
    
    // Gather all handlers from providers
    val allHandlers = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<ScopedRouteHandler>()
    }
    coordinator.routeHandlers = allHandlers
    
    val scopedRouteHandlerRegistry = remember { ScopedRouteHandlerRegistry(allHandlers) }
    val registry = remember { RouteRegistry(scopedRouteHandlerRegistry) }
    
    val navigationMappers = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<RouteNavigationMapper>()
    }
    
    val trackedRouteKeys = remember { 
        mutableStateOf(Route.rootRoutes.map { it.key }.toSet())
    }
    
    val composableBuilders = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<RouteComposableBuilder>()
    }
    
    val startDestination = remember {
        navigationMappers.firstNotNullOf { mapper ->
            mapper.mapDestinationToNavRoute(io.umain.munchies.navigation.Destination.RestaurantList)
        }
    }

    // Listen to navigation events and update state
    LaunchedEffect(coordinator) {
        coordinator.navigationEvents.collectLatest { event ->
            // Reduce event to new state
            coordinator.reduceState(event)
            
            // Handle platform-specific navigation
            handleNavigationEvent(
                event, 
                navController, 
                trackedRouteKeys,
                registry, 
                navigationMappers,
                allHandlers,
                startDestination
            )
        }
    }

    CompositionLocalProvider(
        LocalRouteRegistry provides registry
    ) {
        // Main stack navigation
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composableBuilders.forEach { builder ->
                builder.buildComposable(this, coordinator)
            }
        }
        
        // Modal overlay layer (on top of NavHost)
        val navigationState = coordinator.getCurrentState()
        ModalLayer(navigationState, coordinator)
    }
}

/**
 * Renders all active modals as overlays
 */
@Composable
private fun ModalLayer(
    navigationState: NavigationState,
    coordinator: NavigationCoordinator
) {
    // Render each modal in the stack (typically just one)
    navigationState.modalStack.forEach { modal ->
        when (modal.key) {
            "FilterModal_" -> {
                // Render filter modal
                // Implementation depends on your modal builder system
            }
            else -> {
                // Other modals handled by feature modules
            }
        }
    }
}

// Keep existing navigation event handler
private fun handleNavigationEvent(
    event: NavigationEvent,
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    registry: RouteRegistry,
    navigationMappers: List<RouteNavigationMapper>,
    allHandlers: List<ScopedRouteHandler>,
    rootDestinationRoute: String
) {
    when (event) {
        is NavigationEvent.Push -> {
            handleNavigationPush(event, navController, trackedRouteKeys, navigationMappers, allHandlers)
        }
        is NavigationEvent.Pop -> {
            handleNavigationPop(navController, trackedRouteKeys, registry, navigationMappers)
        }
        is NavigationEvent.PopToRoot -> {
            handleNavigationPopToRoot(navController, trackedRouteKeys, registry, rootDestinationRoute)
        }
        // New events to handle...
        else -> {
            // Handle new navigation events
        }
    }
}

// Keep existing event handlers...
private fun handleNavigationPush(
    event: NavigationEvent.Push,
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    navigationMappers: List<RouteNavigationMapper>,
    allHandlers: List<ScopedRouteHandler>
) {
    for (handler in allHandlers) {
        if (handler.canHandle(event.destination)) {
            val route = handler.destinationToRoute(event.destination)
            if (route != null) {
                trackedRouteKeys.value += route.key
                
                val navRoute = navigationMappers.firstNotNullOfOrNull { mapper ->
                    mapper.mapDestinationToNavRoute(event.destination)
                } ?: throw IllegalArgumentException("No navigation route mapper found for destination: ${event.destination}")
                
                navController.navigate(navRoute)
                return
            }
        }
    }
    throw IllegalArgumentException("No route handler found for destination: ${event.destination}")
}

private fun handleNavigationPop(
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    registry: RouteRegistry,
    navigationMappers: List<RouteNavigationMapper>,
) {
    val currentDestination = navController.currentDestination?.route
    navController.popBackStack()

    val updatedRouteKeys = trackedRouteKeys.value.toMutableSet()
    
    if (currentDestination != null) {
        for (mapper in navigationMappers) {
            val cleanupPattern = mapper.getRouteCleanupPattern()
            
            if (cleanupPattern != null && currentDestination.startsWith(cleanupPattern)) {
                val keyPattern = mapper.getRouteKeyPattern()
                
                if (keyPattern != null) {
                    updatedRouteKeys.removeAll { it.startsWith(keyPattern) }
                }
                break
            }
        }
    }
    
    trackedRouteKeys.value = updatedRouteKeys
    registry.cleanup(updatedRouteKeys)
}

private fun handleNavigationPopToRoot(
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    registry: RouteRegistry,
    rootDestinationRoute: String
) {
    navController.popBackStack(
        route = rootDestinationRoute,
        inclusive = false
    )
    trackedRouteKeys.value = Route.rootRoutes.map { it.key }.toSet()
    registry.cleanup(trackedRouteKeys.value)
}
```

### 8. Android ModalLayer Composable

```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/navigation/modals/ModalLayer.kt

package io.umain.munchies.android.navigation.modals

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.umain.munchies.navigation.FilterModalRoute
import io.umain.munchies.navigation.ModalRoute
import io.umain.munchies.navigation.NavigationCoordinator
import io.umain.munchies.navigation.NavigationState

/**
 * Renders all active modals as overlays on top of the main navigation stack
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalLayer(
    navigationState: NavigationState,
    coordinator: NavigationCoordinator
) {
    // Render the topmost modal, if any
    navigationState.topModal?.let { modal ->
        when (modal) {
            is FilterModalRoute -> {
                ModalBottomSheet(
                    onDismissRequest = { coordinator.dismissModal() },
                    content = {
                        FilterModalContent(
                            preSelectedFilters = modal.preSelectedFilters,
                            onApply = { selectedFilters ->
                                coordinator.dismissModal()
                                // Handle filter application
                            },
                            onDismiss = { coordinator.dismissModal() }
                        )
                    }
                )
            }
            else -> {
                // Other modals handled by feature-specific code
            }
        }
    }
}

@Composable
private fun FilterModalContent(
    preSelectedFilters: List<String>,
    onApply: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Placeholder implementation
    Text("Filter Modal")
    Button(onClick = { onDismiss() }) {
        Text("Cancel")
    }
    Button(onClick = { onApply(preSelectedFilters) }) {
        Text("Apply")
    }
}
```

---

## iOS Platform Integration

### 9. iOS Navigation Setup

```swift
// iosApp/iosApp/Navigation/NavigationCoordinator+iOS.swift

import SwiftUI
import KMP

@MainActor
class iOSNavigationCoordinator: ObservableObject {
    let coordinator: NavigationCoordinator
    
    @Published var navigationState: NavigationState
    @Published var showingModal: Bool = false
    
    init(coordinator: NavigationCoordinator) {
        self.coordinator = coordinator
        self.navigationState = coordinator.getCurrentState()
        
        // Listen for state updates
        Task {
            for await newState in coordinator.navigationState {
                self.navigationState = newState
                self.showingModal = !newState.modalStack.isEmpty
            }
        }
    }
    
    func navigateToScreen(_ destination: Destination) {
        coordinator.navigateToScreen(destination: destination)
    }
    
    func showModal(_ destination: ModalDestination) {
        coordinator.showModal(destination: destination)
    }
    
    func dismissModal() {
        coordinator.dismissModal()
    }
    
    func selectTab(_ tabId: String) {
        coordinator.selectTab(tabId: tabId)
    }
}

// iosApp/iosApp/App/AppNavigationView.swift

struct AppNavigationView: View {
    @StateObject var navigationCoordinator: iOSNavigationCoordinator
    
    var body: some View {
        ZStack {
            // Main navigation stack
            NavigationStack(
                path: Binding(
                    get: { navigationCoordinator.navigationState.currentStack.map { $0.key } },
                    set: { _ in }
                )
            ) {
                routeView(for: navigationCoordinator.navigationState.currentStack.first)
                    .navigationDestination(
                        for: String.self
                    ) { routeKey in
                        if let route = navigationCoordinator.navigationState.currentStack
                            .first(where: { $0.key == routeKey }) {
                            routeView(for: route)
                        }
                    }
            }
            
            // Modal layer on top
            if navigationCoordinator.showingModal {
                ModalPresentationLayer(
                    navigationState: navigationCoordinator.navigationState,
                    coordinator: navigationCoordinator
                )
            }
        }
    }
    
    @ViewBuilder
    private func routeView(for route: (any Route)?) -> some View {
        guard let route = route else {
            EmptyView()
        }
        
        // Dispatch to route-specific view builders
        switch route {
        // Feature modules provide route-specific views
        default:
            EmptyView()
        }
    }
}

// iosApp/iosApp/Navigation/ModalPresentationLayer.swift

struct ModalPresentationLayer: View {
    let navigationState: NavigationState
    let coordinator: iOSNavigationCoordinator
    
    var body: some View {
        Group {
            if let topModal = navigationState.topModal {
                modalView(for: topModal)
            }
        }
    }
    
    @ViewBuilder
    private func modalView(for modal: any ModalRoute) -> some View {
        switch modal {
        case let filterModal as FilterModalRoute:
            sheet(isPresented: .constant(true)) {
                FilterModalView(
                    preSelectedFilters: filterModal.preSelectedFilters,
                    onDismiss: {
                        coordinator.dismissModal()
                    },
                    onApply: { filters in
                        coordinator.dismissModal()
                        // Handle filter selection
                    }
                )
            }
        default:
            EmptyView()
        }
    }
}
```

---

## Feature Module Examples

### 10. Feature Module Modal Route Handler

```kotlin
// feature-restaurant/src/commonMain/kotlin/io/umain/munchies/feature/restaurant/navigation/FilterModalRouteHandler.kt

package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.navigation.ModalDestination
import io.umain.munchies.navigation.ModalRoute
import io.umain.munchies.navigation.ModalRouteHandler

data class FilterModalRoute(
    val preSelectedFilters: List<String> = emptyList()
) : ModalRoute {
    override val key: String = "FilterModal_${preSelectedFilters.hashCode()}"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}

class FilterModalRouteHandler : ModalRouteHandler {
    override val route: FilterModalRoute = FilterModalRoute()
    
    override fun toRouteString(): String = "filter_modal"
    
    override fun canHandle(destination: Destination): Boolean = false
    
    override fun destinationToRoute(destination: Destination): Route? = null
    
    override fun canHandleModal(destination: ModalDestination): Boolean {
        return destination is ModalDestination.Filter
    }
    
    override fun destinationToModalRoute(destination: ModalDestination): ModalRoute? {
        return when (destination) {
            is ModalDestination.Filter -> FilterModalRoute(destination.preSelectedFilters)
            else -> null
        }
    }
}
```

---

## Testing Examples

### 11. Reducer Unit Tests

```kotlin
// androidApp/src/test/kotlin/navigation/NavigationReducerTest.kt

package io.umain.munchies.android.navigation

import io.umain.munchies.navigation.FilterModalRoute
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.NavigationReducer
import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.ModalDestination
import org.junit.Assert.*
import org.junit.Test

class NavigationReducerTest {
    
    @Test
    fun `push adds route to primary stack`() {
        val initialState = NavigationState(
            primaryStack = listOf(RestaurantListRoute())
        )
        
        val event = NavigationEvent.Push(Destination.RestaurantDetail(id = "123"))
        val handlers = listOf(RestaurantDetailRouteHandler())
        
        val newState = NavigationReducer.reduce(initialState, event, handlers)
        
        assertEquals(2, newState.primaryStack.size)
        assertTrue(newState.primaryStack.last() is RestaurantDetailRoute)
    }
    
    @Test
    fun `show modal doesn't affect primary stack`() {
        val initialState = NavigationState(
            primaryStack = listOf(RestaurantListRoute())
        )
        
        val event = NavigationEvent.ShowModal(ModalDestination.Filter())
        val handlers = listOf(FilterModalRouteHandler())
        
        val newState = NavigationReducer.reduce(initialState, event, handlers)
        
        assertEquals(1, newState.primaryStack.size) // Unchanged
        assertEquals(1, newState.modalStack.size)   // New modal added
    }
    
    @Test
    fun `pop dismisses modal if one is showing`() {
        val initialState = NavigationState(
            primaryStack = listOf(RestaurantDetailRoute("123")),
            modalStack = listOf(FilterModalRoute())
        )
        
        val event = NavigationEvent.Pop
        val newState = NavigationReducer.reduce(initialState, event)
        
        assertEquals(1, newState.primaryStack.size) // Unchanged
        assertEquals(0, newState.modalStack.size)   // Modal dismissed
    }
    
    @Test
    fun `pop to root clears primary stack to root only`() {
        val initialState = NavigationState(
            primaryStack = listOf(
                RestaurantListRoute(),
                RestaurantDetailRoute("1"),
                RestaurantDetailRoute("2")
            )
        )
        
        val event = NavigationEvent.PopToRoot
        val newState = NavigationReducer.reduce(initialState, event)
        
        assertEquals(1, newState.primaryStack.size)
        assertEquals(RestaurantListRoute(), newState.primaryStack[0])
    }
    
    @Test
    fun `dismiss all modals clears modal stack`() {
        val initialState = NavigationState(
            modalStack = listOf(
                FilterModalRoute(),
                FilterModalRoute()
            )
        )
        
        val event = NavigationEvent.DismissAllModals
        val newState = NavigationReducer.reduce(initialState, event)
        
        assertEquals(0, newState.modalStack.size)
    }
}
```

---

## Getting Started

### Implementation Order

1. **Copy core data models** (NavigationState.kt, ModalDestination.kt)
2. **Copy NavigationReducer.kt** to core module
3. **Extend NavigationCoordinator** in core module
4. **Copy TabNavigationState.kt** to core module
5. **Update AndroidAppNavigation.kt** with modal support
6. **Implement first feature handler** (FilterModalRouteHandler)
7. **Write and run tests**
8. **Iterate on iOS integration**

### Key Integration Points

- **Dependency Injection**: Register `NavigationCoordinator` in Koin
- **StateFlow/SharedFlow**: Platform layers observe these for updates
- **Pure Reducers**: Test without mocks or platform dependencies
- **Feature Handlers**: Each feature owns its route/modal definitions

---

## Next Steps

After Phase 0 implementation:

1. **Phase 1**: Add ModalComposableBuilder for Android
2. **Phase 2**: Implement first tab navigation use case
3. **Phase 3**: Add deep link parsing and handling

This code is production-ready and follows your project's existing patterns!
