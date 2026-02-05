package io.umain.munchies.feature.restaurant.presentation.model

data class DetailCardData(
    val title: String,
    val tags: List<String> = emptyList(),
    val statusText: String,
    val statusColor: String,
)