package io.umain.munchies.feature.restaurant.presentation.model

import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus

data class DetailCardData(
    val title: String,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val statusText: String,
    val statusColor: String,
)

internal fun Restaurant.toDetailCardData(
    status: RestaurantStatus,
    statusOpenText: String? = null,
    statusClosedText: String? = null
): DetailCardData {
    val even = (this.rating * 10).toInt() % 2 == 0

    val tags = if (even)
        listOf("Take-Out", "Fast delivery", "Eat-In")
    else
        listOf("Take-Out")

    val statusColor =
        if (status == RestaurantStatus.OPEN) {
            DesignTokens.Colors.Accent.positive
        } else {
            DesignTokens.Colors.Accent.negative
        }

    val statusText =
        if (status == RestaurantStatus.OPEN) {
            statusOpenText
        } else {
            statusClosedText
        }

    return DetailCardData(
        title = this.name,
        imageUrl = this.imageUrl,
        tags = tags,
        statusText = statusText ?: "",
        statusColor = statusColor
    )
}
