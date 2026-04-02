package io.umain.munchies.navigation

import io.umain.munchies.logging.logInfo
import kotlin.random.Random

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

    //  SCREEN NAVIGATION HANDLERS

    private fun handlePush(
        state: NavigationState,
        event: NavigationEvent.Push,
        handlers: List<RouteHandler>
    ): NavigationState {
        logInfo("NavigationReducer", "[36mhandlePush: destination=${event.destination::class.simpleName}, handlers available=${handlers.size}")
        val route = resolveRoute(event.destination, handlers)

        if (route == null) {
            logInfo("NavigationReducer", "  [31mNo route created - returning same state")
            return state
        }

        logInfo("NavigationReducer", "  [32mRoute created: ${route::class.simpleName}, proceeding to handlePushInTab")
        // Always push in current tab
        return handlePushInTab(state, NavigationEvent.PushInTab(event.destination), handlers)
    }

    private fun handlePop(state: NavigationState): NavigationState {
        // If modals are showing, dismiss top modal instead
        return if (state.modalStack.isNotEmpty()) {
            handleDismissModal(state)
        } else {
            handlePopInTab(state)
        }
    }

    private fun handlePopToRoot(state: NavigationState): NavigationState {
        return state.copy(
            tabNavigation = state.tabNavigation.copy(
                stacksByTab = state.tabNavigation.stacksByTab.mapValues { entry ->
                    val stack: List<ScreenEntry> = entry.value
                    if (stack.isNotEmpty()) listOf(stack.first()) else emptyList()
                }
            )
        )
    }

    //  MODAL NAVIGATION HANDLERS

    @Suppress("UNUSED_PARAMETER")
    private fun handleShowModal(
        state: NavigationState,
        event: NavigationEvent.ShowModal,
        _handlers: List<RouteHandler>
    ): NavigationState {
        val modalRoute = destinationToModalRoute(event.destination)
        logInfo("NavigationReducer", "📥 handleShowModal: destination=${event.destination::class.simpleName} -> modalRoute=${modalRoute?.key ?: "null"}")
        
        return if (modalRoute != null) {
            state.copy(modalStack = state.modalStack + modalRoute)
        } else {
            logInfo("NavigationReducer", "  ✗ Failed to convert ModalDestination to ModalRoute")
            state
        }
    }

    private fun destinationToModalRoute(destination: ModalDestination): ModalRoute? {
        return when (destination) {
            is ModalDestination.Filter -> FilterModalRoute(destination.preSelectedFilters)
            is ModalDestination.SubmitReviewModal -> SubmitReviewModalRoute(destination.restaurantId)
            is ModalDestination.ConfirmAction -> ConfirmActionModalRoute(
                destination.message,
                destination.confirmText,
                destination.cancelText
            )
            is ModalDestination.DatePicker -> DatePickerModalRoute(destination.initialDate)
            is ModalDestination.ReviewSuccessModal -> ReviewSuccessModalRoute
            is ModalDestination.ReviewErrorAlert -> ReviewErrorAlertRoute(destination.message)
        }
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
        val lastIndex = state.modalStack.indexOfLast { modal: ModalRoute ->
            event.predicate(modal)
        }

        return if (lastIndex >= 0) {
            state.copy(modalStack = state.modalStack.take(lastIndex + 1))
        } else {
            state.copy(modalStack = emptyList())
        }
    }

    //  TAB NAVIGATION HANDLERS
    private fun handleSelectTab(
        state: NavigationState,
        event: NavigationEvent.SelectTab
    ): NavigationState {
        return state.copy(
            tabNavigation = state.tabNavigation.copy(
                activeTabId = event.tabId,
                navigationDirection = NavigationDirection.TabSwitch
            )
        )
    }

    private fun handlePushInTab(
        state: NavigationState,
        event: NavigationEvent.PushInTab,
        handlers: List<RouteHandler>
    ): NavigationState {
        val route = resolveRoute(event.destination, handlers) ?: return state

        val tabNav = state.tabNavigation
        val currentStack = tabNav.getActiveTabStack()
        val scopeId = "${route.key}-${Random.nextLong()}"
        createKoinScope(scopeId, route.scopeQualifier)
        val entry = ScreenEntry(route, scopeId)
        val newStack = currentStack + entry

        return state.copy(
            tabNavigation = tabNav.updateActiveTabStack(newStack)
                .copy(navigationDirection = NavigationDirection.Forward)
        )
    }

    private fun handlePopInTab(state: NavigationState): NavigationState {
        val tabNav = state.tabNavigation
        val currentStack = tabNav.getActiveTabStack()

        if (currentStack.size <= 1) return state

        val popped = currentStack.last()
        getKoinScopeOrNull(popped.scopeId)?.close()
        val newStack = currentStack.dropLast(1)
        return state.copy(
            tabNavigation = tabNav.updateActiveTabStack(newStack)
                .copy(navigationDirection = NavigationDirection.Back)
        )
    }

    private fun resolveRoute(
        destination: Destination,
        handlers: List<RouteHandler>
    ): Route? {
        return handlers.firstNotNullOfOrNull { handler ->
            if (handler.canHandle(destination)) handler.destinationToRoute(destination) else null
        } ?: destination.toBuiltinRoute()
    }

    private fun Destination.toBuiltinRoute(): Route? = when (this) {
        Destination.RestaurantList -> RestaurantListRoute()
        is Destination.RestaurantDetail -> RestaurantDetailRoute(restaurantId)
        Destination.Settings -> SettingsRoute()
    }
}
