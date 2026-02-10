package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.core.di.KmpScopeId

/**
 * Scope identity for the Restaurant List route.
 *
 * The list is a singleton per navigation session (unlike detail which is parameterized per restaurant).
 * This scope is created when the list route is first visited and retained
 * until the app is closed or the list route is explicitly cleaned up by RouteRegistry.
 */
object RestaurantListScope : KmpScopeId {
    override val value = "RestaurantList"
    override val qualifierName: String = "RestaurantListScope"
}
