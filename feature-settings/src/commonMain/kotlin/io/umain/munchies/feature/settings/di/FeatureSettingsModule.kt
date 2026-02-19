package io.umain.munchies.feature.settings.di

import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import org.koin.dsl.module
import org.koin.core.qualifier.named

val featureSettingsModule = module {

    scope(named(SettingsScope.qualifierName)) {
        scoped { SettingsViewModel() }
    }
}
