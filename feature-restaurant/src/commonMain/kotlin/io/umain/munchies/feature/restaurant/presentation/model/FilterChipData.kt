package io.umain.munchies.feature.restaurant.presentation.model

data class FilterChipData(
    val id: String,
    val label: String,
    val iconUrl: String,
    val isSelected: Boolean = false,
    val contentDescription: String = "Filter: $label"
)