package io.umain.munchies.core.state

/**
 * Force-export state base types to Swift.
 * These functions don't perform runtime operations; they force the KMP compiler
 * to include types in the generated Swift framework.
 */

fun _exportViewStateType(state: ViewState): ViewState = state

fun <S : ViewState> _exportViewModelStateType(vm: ViewModelState<S>): ViewModelState<S> = vm
