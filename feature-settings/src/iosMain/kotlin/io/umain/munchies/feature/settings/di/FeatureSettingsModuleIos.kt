package io.umain.munchies.feature.settings.di

import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.feature.settings.navigation.SettingsNavigationViewModel
import io.umain.munchies.feature.settings.navigation.ios.SettingsRouteHandlerImpl
import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.logging.logInfo
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.qualifier.named

val featureSettingsModuleIos = module {
    logInfo("FeatureSettingsModuleIos", "🔧 Building iOS-specific settings module")
    
    single { SettingsNavigationViewModel(get<NavigationDispatcher>()) }

    single {
        logInfo("FeatureSettingsModuleIos", "📝 Registering SettingsRouteHandlerImpl")
        SettingsRouteHandlerImpl
    } bind RouteHandler::class

    scope(named(SettingsScope.qualifierName)) {
        scoped { SettingsViewModel() }
    }
    
    logInfo("FeatureSettingsModuleIos", "✅ iOS-specific settings module built")
}

