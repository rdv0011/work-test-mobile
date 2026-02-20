package io.umain.munchies.feature.settings.di

import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

val featureSettingsModule = module {

    scope(named(SettingsScope.qualifierName)) {
        scoped { SettingsViewModel() }
    }
}
