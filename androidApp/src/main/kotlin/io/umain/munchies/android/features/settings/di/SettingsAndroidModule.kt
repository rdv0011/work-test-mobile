package io.umain.munchies.android.features.settings.di

import io.umain.munchies.android.features.settings.navigation.settingsRouteHandlerAndroid
import io.umain.munchies.navigation.RouteHandler
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Registers the Android-specific settings DI module into the running Koin instance.
 *
 * Must be called after [io.umain.munchies.feature.settings.di.registerFeatureSettingsModule]
 * and before [io.umain.munchies.di.createAppCoordinator], so that SettingsRouteHandlerAndroid
 * (a ScopedRouteHandler) is present in routeHandlers when AppCoordinator.init creates scopes.
 */
fun registerAndroidSettingsModule() {
    val androidSettingsModule = module {
        // Register the Android-specific ScopedRouteHandler so AppCoordinator picks it up
        // via getAll<RouteHandler>() and calls createScope(SettingsRoute) on startup.
        single { settingsRouteHandlerAndroid() } bind RouteHandler::class
    }
    loadKoinModules(androidSettingsModule)
}
