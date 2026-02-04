package io.umain.munchies.feature.restaurant.presentation.model

data class DetailCardData(
    val title: String,
    val statusText: String,
    val statusColor: String,
    val contentDescription: String = "Restaurant status: $statusText"
)