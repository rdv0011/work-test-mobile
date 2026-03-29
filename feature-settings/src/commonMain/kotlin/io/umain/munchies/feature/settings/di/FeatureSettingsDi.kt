package io.umain.munchies.feature.settings.di

import io.umain.munchies.logging.logInfo
import org.koin.core.context.loadKoinModules

/**
 * Call this after Koin init to register feature-settings DI module into the running Koin instance.
 * 
 * On iOS, this loads the iOS-specific module with iOS handlers.
 * On Android, this loads the common module.
 */
fun registerFeatureSettingsModule() {
    try {
        loadSettingsKoinModules()
    } catch (e: Exception) {
        logInfo("FeatureSettingsDi", "❌ Failed to register: ${e.message}")
        throw e
    }
}

expect fun loadSettingsKoinModules()
