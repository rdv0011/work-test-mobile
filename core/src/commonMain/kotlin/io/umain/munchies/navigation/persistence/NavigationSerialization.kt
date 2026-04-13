package io.umain.munchies.navigation.persistence

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.ModalRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.FilterModalRoute
import io.umain.munchies.navigation.SubmitReviewModalRoute
import io.umain.munchies.navigation.ConfirmActionModalRoute
import io.umain.munchies.navigation.DatePickerModalRoute
import io.umain.munchies.navigation.ReviewSuccessModalRoute
import io.umain.munchies.navigation.ReviewErrorAlertRoute

/**
 * Serializers module for navigation routes.
 * Registers all Route and ModalRoute subclasses for polymorphic serialization.
 */
val navigationSerializersModule = SerializersModule {
    polymorphic(Route::class) {
        subclass(RestaurantListRoute::class)
        subclass(RestaurantDetailRoute::class)
        subclass(SettingsRoute::class)
        subclass(FilterModalRoute::class)
        subclass(SubmitReviewModalRoute::class)
        subclass(ConfirmActionModalRoute::class)
        subclass(DatePickerModalRoute::class)
        subclass(ReviewSuccessModalRoute::class)
        subclass(ReviewErrorAlertRoute::class)
    }
    polymorphic(ModalRoute::class) {
        subclass(FilterModalRoute::class)
        subclass(SubmitReviewModalRoute::class)
        subclass(ConfirmActionModalRoute::class)
        subclass(DatePickerModalRoute::class)
        subclass(ReviewSuccessModalRoute::class)
        subclass(ReviewErrorAlertRoute::class)
    }
}

/**
 * Json instance configured for navigation serialization.
 * Includes serializers for all Route and ModalRoute subclasses.
 */
val navigationJson = Json {
    serializersModule = navigationSerializersModule
    ignoreUnknownKeys = true
    prettyPrint = false
}
