package io.umain.munchies.feature.settings.di

import io.umain.munchies.di.getKoin
import io.umain.munchies.feature.settings.navigation.ios.SettingsRouteHandlerImpl
import io.umain.munchies.navigation.SettingsRoute
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

fun createSettingsScopeIos(): Scope {
    val koin = getKoin()
    val scopeId = SettingsScope.value
    
    return koin.getScopeOrNull(scopeId)
        ?: koin.createScope(
            scopeId = scopeId,
            qualifier = named(SettingsScope.qualifierName)
        )
}

fun createSettingsRoute(): SettingsRoute = SettingsRoute()

fun getSettingsRouteHandler() = SettingsRouteHandlerImpl

