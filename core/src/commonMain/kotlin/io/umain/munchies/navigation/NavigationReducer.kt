package io.umain.munchies.navigation

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
        // Convert Destination to Route via handlers
        val route = handlers.firstNotNullOfOrNull { handler ->
            if (handler.canHandle(event.destination)) {
                handler.destinationToRoute(event.destination)
            } else null
        }

        if (route == null) {
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

    //  MODAL NAVIGATION HANDLERS

    @Suppress("UNUSED_PARAMETER")
    private fun handleShowModal(
        state: NavigationState,
        _event: NavigationEvent.ShowModal,
        _handlers: List<RouteHandler>
    ): NavigationState {
        // Modal state is managed at the UI layer (AppNavigation.kt, NavigationCoordinator.swift)
        // This method exists for completeness but modals are not tracked in NavigationState
        return state
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
                handler.destinationToRoute(event.destination)
            } else null
        } ?: return state

        val tabNav = state.tabNavigation
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
