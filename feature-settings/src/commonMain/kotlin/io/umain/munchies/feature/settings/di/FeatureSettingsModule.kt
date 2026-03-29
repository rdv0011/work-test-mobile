package io.umain.munchies.feature.settings.di

import io.umain.munchies.feature.settings.navigation.SettingsDeepLinkHandler
import io.umain.munchies.feature.settings.navigation.SettingsRouteHandler
import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import io.umain.munchies.navigation.DeepLinkHandler
import io.umain.munchies.navigation.RouteHandler
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.qualifier.named

val featureSettingsModule = module {

    single { SettingsRouteHandler } bind RouteHandler::class

    // Register deep link handler
    single { SettingsDeepLinkHandler() } bind DeepLinkHandler::class

    scope(named(SettingsScope.qualifierName)) {
        scoped { SettingsViewModel() }
    }
}
