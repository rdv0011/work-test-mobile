package io.umain.munchies.feature.settings.navigation.ios

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.feature.settings.navigation.SettingsRouteHandler

object SettingsRouteHandlerImpl : RouteHandler {
    private val commonHandler = SettingsRouteHandler
    
    override val route: Route = commonHandler.route

    override fun toRouteString(): String = commonHandler.toRouteString()

    override fun canHandle(destination: Destination): Boolean =
        commonHandler.canHandle(destination)

    override fun destinationToRoute(destination: Destination): Route? =
        commonHandler.destinationToRoute(destination)
}
