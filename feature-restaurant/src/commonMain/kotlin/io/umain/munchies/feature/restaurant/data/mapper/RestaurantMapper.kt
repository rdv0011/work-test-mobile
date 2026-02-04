package io.umain.munchies.feature.restaurant.data.mapper

import io.umain.munchies.feature.restaurant.data.remote.RestaurantDto
import io.umain.munchies.feature.restaurant.data.remote.FilterDto
import io.umain.munchies.feature.restaurant.data.remote.RestaurantOpenDto
import io.umain.munchies.feature.restaurant.domain.model.Filter
import io.umain.munchies.feature.restaurant.domain.model.Restaurant
import io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus

internal fun RestaurantDto.toDomain(): Restaurant = Restaurant(
    id = id,
    name = name,
    imageUrl = imageUrl,
    rating = rating,
    deliveryTimeMinutes = deliveryTimeMinutes,
    filterIds = filterIds
)

internal fun FilterDto.toDomain(): Filter = Filter(
    id = id,
    name = name,
    imageUrl = imageUrl
)

internal fun RestaurantOpenDto.toDomain(): RestaurantStatus =
    if (open) RestaurantStatus.OPEN else RestaurantStatus.CLOSED

