package io.umain.munchies.aggregator

import io.umain.munchies.feature.restaurant.di.createRestaurantListScopeIos
import io.umain.munchies.feature.restaurant.di.createRestaurantDetailScopeIos
import io.umain.munchies.feature.restaurant.di.getRestaurantListViewModelIos
import io.umain.munchies.feature.restaurant.di.getRestaurantDetailViewModelIos
import io.umain.munchies.feature.settings.di.createSettingsScopeIos
import io.umain.munchies.feature.settings.di.getSettingsViewModelIos
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import io.umain.munchies.feature.restaurant.presentation.model.RestaurantCardData
import io.umain.munchies.feature.restaurant.presentation.model.FilterChipData
import io.umain.munchies.feature.restaurant.presentation.model.DetailCardData
import org.koin.core.scope.Scope
import kotlin.native.ObjCName
import kotlin.experimental.ExperimentalObjCName

// Explicit type constructors to force Kotlin Native to export these data classes
@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantCardDataConstructor")
fun createRestaurantCardData(
    id: String,
    restaurantName: String,
    tags: List<String>,
    deliveryTime: Int,
    distance: Double,
    rating: String,
    imageUrl: String,
    contentDescription: String = "Restaurant: $restaurantName"
): RestaurantCardData = RestaurantCardData(
    id, restaurantName, tags, deliveryTime, distance, rating, imageUrl, contentDescription
)

@OptIn(ExperimentalObjCName::class)
@ObjCName("FilterChipDataConstructor")
fun createFilterChipData(
    id: String,
    label: String,
    iconUrl: String,
    isSelected: Boolean = false,
    contentDescription: String = "Filter: $label"
): FilterChipData = FilterChipData(id, label, iconUrl, isSelected, contentDescription)

@OptIn(ExperimentalObjCName::class)
@ObjCName("DetailCardDataConstructor")
fun createDetailCardData(
    title: String,
    imageUrl: String? = null,
    tags: List<String> = emptyList(),
    statusText: String,
    statusColor: String
): DetailCardData = DetailCardData(title, imageUrl, tags, statusText, statusColor)

@OptIn(ExperimentalObjCName::class)
@ObjCName("createSettingsScope")
fun createSettingsScope(): Scope = createSettingsScopeIos()

@OptIn(ExperimentalObjCName::class)
@ObjCName("createRestaurantListScope")
fun createRestaurantListScope(): Scope = createRestaurantListScopeIos()

@OptIn(ExperimentalObjCName::class)
@ObjCName("createRestaurantDetailScope")
fun createRestaurantDetailScope(restaurantId: String): Scope = createRestaurantDetailScopeIos(restaurantId)

@OptIn(ExperimentalObjCName::class)
@ObjCName("getSettingsViewModelFromFramework")
fun getSettingsViewModelFromFramework(): SettingsViewModel = getSettingsViewModelIos()

@OptIn(ExperimentalObjCName::class)
@ObjCName("getRestaurantListViewModelFromFramework")
fun getRestaurantListViewModelFromFramework(): RestaurantListViewModel = getRestaurantListViewModelIos()

@OptIn(ExperimentalObjCName::class)
@ObjCName("getRestaurantDetailViewModelFromFramework")
fun getRestaurantDetailViewModelFromFramework(restaurantId: String): RestaurantDetailViewModel = 
    getRestaurantDetailViewModelIos(restaurantId)
