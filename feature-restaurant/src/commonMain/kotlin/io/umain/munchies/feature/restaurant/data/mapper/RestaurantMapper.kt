package io.umain.munchies.feature.restaurant.data.mapper

import io.umain.munchies.feature.restaurant.data.remote.RestaurantDto
import io.umain.munchies.feature.restaurant.data.remote.FilterDto
import io.umain.munchies.feature.restaurant.domain.model.Filter
import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus

internal fun RestaurantDto.toDomain(): Restaurant = Restaurant(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    rating = rating,
    reviewCount = reviewCount,
    status = when (status.lowercase()) {
        "open" -> RestaurantStatus.OPEN
        "closed" -> RestaurantStatus.CLOSED
        else -> RestaurantStatus.CLOSED
    },
    filterIds = filterIds
)

internal fun FilterDto.toDomain(): Filter = Filter(
    id = id,
    name = name,
    iconUrl = iconUrl
)
