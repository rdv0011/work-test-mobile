package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.core.di.KmpScopeId

data class RestaurantDetailScope(
    val restaurantId: String
) : KmpScopeId {
    override val value = "RestaurantDetail_$restaurantId"
}