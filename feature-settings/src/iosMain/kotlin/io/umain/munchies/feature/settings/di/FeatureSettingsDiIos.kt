package io.umain.munchies.feature.settings.di

import org.koin.core.context.loadKoinModules

actual fun loadSettingsKoinModules() {
    loadKoinModules(featureSettingsModuleIos)
}
