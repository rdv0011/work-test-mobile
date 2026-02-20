package io.umain.munchies.feature.settings.di

import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.scope.Scope

fun registerFeatureSettingsModule() {
    loadKoinModules(featureSettingsModule)
}

fun Scope.getSettingsViewModel(): SettingsViewModel =
    get()