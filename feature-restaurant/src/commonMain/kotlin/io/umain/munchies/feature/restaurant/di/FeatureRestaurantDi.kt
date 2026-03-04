package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.logging.logInfo
import org.koin.core.context.loadKoinModules

/**
 * Call this after Koin init to register feature-restaurant DI module into the running Koin instance.
 * 
 * On iOS, this loads the iOS-specific module with iOS handlers.
 * On Android, this loads the common module.
 */
fun registerFeatureRestaurantModule() {
    logInfo("FeatureRestaurantDi", "🔧 Registering feature-restaurant module")
    try {
        loadRestaurantKoinModules()
        logInfo("FeatureRestaurantDi", "✅ Feature-restaurant module registered successfully")
    } catch (e: Exception) {
        logInfo("FeatureRestaurantDi", "❌ Failed to register: ${e.message}")
        throw e
    }
}

expect fun loadRestaurantKoinModules()
