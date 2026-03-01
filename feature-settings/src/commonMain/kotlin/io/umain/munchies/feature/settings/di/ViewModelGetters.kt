package io.umain.munchies.feature.settings.di

import io.umain.munchies.feature.settings.navigation.SettingsNavigationViewModel
import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import org.koin.core.scope.Scope

/**
 * Type-safe iOS ViewModel retrievers from Scope.
 *
 * Swift cannot call generic functions directly, so we provide explicit, non-generic
 * functions for each ViewModel type. These are called from Swift code.
 *
 * Each function is stable and will be available to Swift during KMP compilation.
 */

fun Scope.getSettingsViewModel(): SettingsViewModel =
    get()

fun Scope.getSettingsNavigationViewModel(): SettingsNavigationViewModel =
    get()
