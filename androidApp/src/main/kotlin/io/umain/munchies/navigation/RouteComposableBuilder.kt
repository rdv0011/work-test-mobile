package io.umain.munchies.navigation

import androidx.navigation.NavGraphBuilder

interface RouteComposableBuilder {
    fun buildComposable(
        navGraphBuilder: NavGraphBuilder,
        coordinator: AppCoordinator
    )
}
