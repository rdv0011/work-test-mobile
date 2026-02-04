package io.umain.munchies.core.state

import kotlinx.coroutines.flow.StateFlow

interface ViewState

interface ViewModelState<S : ViewState> {
    val stateFlow: StateFlow<S>
    val state: S get() = stateFlow.value
}