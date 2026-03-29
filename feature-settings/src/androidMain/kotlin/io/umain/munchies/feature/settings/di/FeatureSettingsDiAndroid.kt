package io.umain.munchies.feature.settings.di

import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

actual fun loadSettingsKoinModules() {
    loadKoinModules(featureSettingsModule)
}
