package io.umain.munchies.core.di

import org.koin.core.scope.Scope

/**
 * KMP helper to retrieve ViewModels from a Koin Scope.
 *
 * On Android: Calls scope.get<T>() directly.
 * On iOS: Provides a stable interop point for Swift to retrieve typed instances.
 *
 * Usage from Swift:
 * ```swift
 * let viewModel = try! scope.get(viewModel: RestaurantDetailViewModel.self)
 * ```
 *
 * Usage from Kotlin:
 * ```kotlin
 * val viewModel = scope.getViewModel<RestaurantDetailViewModel>()
 * ```
 */
inline fun <reified T> Scope.getViewModel(): T = get()

/**
 * KMP helper to retrieve ViewModels from a Koin Scope with parameters.
 *
 * On Android: Calls scope.get<T>(parameters = { ... }) directly.
 * On iOS: Provides a stable interop point for Swift with parameter passing.
 *
 * Usage from Kotlin:
 * ```kotlin
 * val viewModel = scope.getViewModel<RestaurantDetailViewModel> {
 *     parametersOf(restaurantId)
 * }
 * ```
 */
inline fun <reified T> Scope.getViewModel(noinline parameterBuilder: () -> org.koin.core.parameter.ParametersHolder): T =
    get(parameters = parameterBuilder)
