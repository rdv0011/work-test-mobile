package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.core.di.KmpScopeId

data class RestaurantDetailScope(
    val restaurantId: String
) : KmpScopeId {
    override val value = "RestaurantDetail_$restaurantId"
    override val qualifierName: String = Companion.qualifierName

    companion object {
        const val qualifierName: String = "RestaurantDetailScope"
        fun fromScopeId(scopeId: String): String = scopeId.removePrefix("RestaurantDetail_")
    }
}