package io.umain.munchies.core.viewmodel

import org.koin.core.scope.Scope

class ScopedViewModelHandle<VM : ScopedViewModel>(
    val scope: Scope,
    val viewModel: VM
)