package io.umain.munchies.feature.settings.di

import org.koin.core.context.loadKoinModules

fun registerFeatureSettingsModule() {
    loadKoinModules(featureSettingsModule)
}
