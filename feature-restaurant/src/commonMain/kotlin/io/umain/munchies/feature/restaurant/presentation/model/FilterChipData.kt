package io.umain.munchies.feature.restaurant.presentation.model

import io.umain.munchies.feature.restaurant.domain.model.Filter

data class FilterChipData(
    val id: String,
    val label: String,
    val iconUrl: String,
    val isSelected: Boolean = false,
    val contentDescription: String = "Filter: $label"
)

internal fun Filter.toFilterChipData(isSelected: Boolean = false): FilterChipData =
    FilterChipData(
        id = id,
        label = name,
        iconUrl = imageUrl,
        isSelected = isSelected
    )