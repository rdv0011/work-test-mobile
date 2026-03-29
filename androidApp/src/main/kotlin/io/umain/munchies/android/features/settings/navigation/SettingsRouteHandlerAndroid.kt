package io.umain.munchies.android.features.settings.navigation

import io.umain.munchies.feature.settings.di.SettingsScope
import io.umain.munchies.feature.settings.navigation.SettingsRouteHandler
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteNavigationMapper
import io.umain.munchies.navigation.ScopedRouteHandler
import io.umain.munchies.navigation.SettingsRoute
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

class SettingsRouteHandlerAndroid(
    private val commonHandler: SettingsRouteHandler = SettingsRouteHandler
) : ScopedRouteHandler, RouteNavigationMapper {

    override val route: Route = commonHandler.route
    
    override fun toRouteString(): String = commonHandler.toRouteString()
    
    override fun canHandle(destination: Destination): Boolean =
        commonHandler.canHandle(destination)
    
    override fun destinationToRoute(destination: Destination): Route? =
        commonHandler.destinationToRoute(destination)
    
    override fun createScope(route: Route): Scope {
        require(route is SettingsRoute) { "Expected SettingsRoute, got $route" }
        val koin = GlobalContext.get()
        val scopeId = route.key
        return koin.getScopeOrNull(scopeId)
            ?: koin.createScope(
                scopeId = scopeId,
                qualifier = named(SettingsScope.qualifierName)
            )
    }

    override fun mapDestinationToNavRoute(destination: Destination): String? =
        if (canHandle(destination)) toRouteString() else null

    override fun getRouteCleanupPattern(): String? = null
    
    override fun getRouteKeyPattern(): String? = null
}

fun settingsRouteHandlerAndroid(): SettingsRouteHandlerAndroid =
    SettingsRouteHandlerAndroid()
