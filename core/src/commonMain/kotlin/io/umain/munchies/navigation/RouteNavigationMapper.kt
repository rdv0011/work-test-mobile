package io.umain.munchies.navigation

interface RouteNavigationMapper {
    fun mapDestinationToNavRoute(destination: Destination): String?

    fun getRouteCleanupPattern(): String?
    
    fun getRouteKeyPattern(): String?
}
