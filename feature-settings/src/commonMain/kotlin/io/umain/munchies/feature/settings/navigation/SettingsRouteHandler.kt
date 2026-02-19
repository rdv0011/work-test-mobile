package io.umain.munchies.feature.settings.navigation

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.navigation.SettingsRoute

object SettingsRouteHandler : RouteHandler {
    override val route: Route = SettingsRoute()

    override fun toRouteString(): String = "settings"

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.Settings

    override fun destinationToRoute(destination: Destination): Route? =
        if (canHandle(destination)) SettingsRoute() else null
}
