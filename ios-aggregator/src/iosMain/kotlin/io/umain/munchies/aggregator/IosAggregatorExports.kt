package io.umain.munchies.aggregator

import io.umain.munchies.feature.restaurant.di.createRestaurantListScopeIos
import io.umain.munchies.feature.restaurant.di.createRestaurantDetailScopeIos
import io.umain.munchies.feature.restaurant.di.getRestaurantListViewModelIos
import io.umain.munchies.feature.restaurant.di.getRestaurantDetailViewModelIos
import io.umain.munchies.feature.restaurant.di.getRestaurantNavigationViewModelIos
import io.umain.munchies.feature.settings.di.createSettingsScopeIos
import io.umain.munchies.feature.settings.di.getSettingsViewModelIos
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import io.umain.munchies.feature.settings.presentation.SettingsUiState
import io.umain.munchies.feature.restaurant.presentation.model.RestaurantCardData
import io.umain.munchies.feature.restaurant.presentation.model.FilterChipData
import io.umain.munchies.feature.restaurant.presentation.model.DetailCardData
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState
import io.umain.munchies.core.state.ViewState
import io.umain.munchies.navigation.RouteConstants
import io.umain.munchies.navigation.DeepLinkConstants
import io.umain.munchies.navigation.DeepLinkProcessor
import io.umain.munchies.navigation.FilterModalRoute
import io.umain.munchies.navigation.ConfirmActionModalRoute
import io.umain.munchies.navigation.SubmitReviewModalRoute
import io.umain.munchies.navigation.DatePickerModalRoute
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.di.initKoinIos
import io.umain.munchies.di.getAppCoordinator
import io.umain.munchies.feature.restaurant.di.registerFeatureRestaurantModule
import io.umain.munchies.feature.settings.di.registerFeatureSettingsModule
import io.umain.munchies.core.lifecycle.subscribeToStateFlow
import io.umain.munchies.core.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import org.koin.core.scope.Scope
import kotlin.native.ObjCName
import kotlin.experimental.ExperimentalObjCName

// ============================================================================
// KOIN INITIALIZATION
// ============================================================================

@OptIn(ExperimentalObjCName::class)
@ObjCName("initKoinFramework")
fun initKoinFramework() {
    initKoinIos()
    registerFeatureRestaurantModule()
    registerFeatureSettingsModule()
    // Create AppCoordinator AFTER modules are loaded so NavigationDispatcher is available
    io.umain.munchies.di.createAppCoordinator()
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("getAppCoordinatorFromFramework")
fun getAppCoordinatorFromFramework(): io.umain.munchies.navigation.AppCoordinator =
    io.umain.munchies.di.getAppCoordinator()

// ============================================================================
// VIEW MODEL TYPE FORCING (Ensure Kotlin Native includes them in framework)
// ============================================================================

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportRestaurantViewModels")
fun forceExportRestaurantViewModels(
    listVm: RestaurantListViewModel,
    detailVm: RestaurantDetailViewModel,
    navVm: RestaurantNavigationViewModel
): Triple<RestaurantListViewModel, RestaurantDetailViewModel, RestaurantNavigationViewModel> =
    Triple(listVm, detailVm, navVm)

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportSettingsViewModels")
fun forceExportSettingsViewModels(
    settingsVm: SettingsViewModel
): SettingsViewModel = settingsVm

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportRestaurantListViewModelType")
fun forceExportRestaurantListViewModelType(): RestaurantListViewModel? = null

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportRestaurantDetailViewModelType")
fun forceExportRestaurantDetailViewModelType(): RestaurantDetailViewModel? = null

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportRestaurantNavigationViewModelType")
fun forceExportRestaurantNavigationViewModelType(): RestaurantNavigationViewModel? = null

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportSettingsViewModelType")
fun forceExportSettingsViewModelType(): SettingsViewModel? = null

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportSettingsUiStateType")
fun forceExportSettingsUiStateType(): SettingsUiState? = null

// ============================================================================
// DATA CLASS CONSTRUCTORS
// ============================================================================

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

@OptIn(ExperimentalObjCName::class)
@ObjCName("getRestaurantNavigationViewModelFromFramework")
fun getRestaurantNavigationViewModelFromFramework(): RestaurantNavigationViewModel =
    getRestaurantNavigationViewModelIos()

// ============================================================================
// UI State Type Exports (forces Kotlin Native to include these in the framework)
// ============================================================================

@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantListUiStateLoading")
fun createRestaurantListUiStateLoading(): RestaurantListUiState = RestaurantListUiState.Loading

@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantListUiStateError")
fun createRestaurantListUiStateError(message: String): RestaurantListUiState =
    RestaurantListUiState.Error(message)

@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantListUiStateSuccess")
fun createRestaurantListUiStateSuccess(
    restaurants: List<RestaurantCardData>,
    filters: List<FilterChipData>,
    selectedFilterIds: Set<String> = emptySet(),
    isFiltering: Boolean = false
): RestaurantListUiState = RestaurantListUiState.Success(restaurants, filters, selectedFilterIds, isFiltering)

@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantDetailUiStateLoading")
fun createRestaurantDetailUiStateLoading(): RestaurantDetailUiState = RestaurantDetailUiState.Loading

@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantDetailUiStateError")
fun createRestaurantDetailUiStateError(message: String): RestaurantDetailUiState =
    RestaurantDetailUiState.Error(message)

// Cast helpers for Swift type checking
@OptIn(ExperimentalObjCName::class)
@ObjCName("isRestaurantListUiStateLoading")
fun isRestaurantListUiStateLoading(state: RestaurantListUiState): Boolean = state is RestaurantListUiState.Loading

@OptIn(ExperimentalObjCName::class)
@ObjCName("isRestaurantListUiStateSuccess")
fun isRestaurantListUiStateSuccess(state: RestaurantListUiState): Boolean = state is RestaurantListUiState.Success

@OptIn(ExperimentalObjCName::class)
@ObjCName("isRestaurantListUiStateError")
fun isRestaurantListUiStateError(state: RestaurantListUiState): Boolean = state is RestaurantListUiState.Error

@OptIn(ExperimentalObjCName::class)
@ObjCName("isRestaurantDetailUiStateLoading")
fun isRestaurantDetailUiStateLoading(state: RestaurantDetailUiState): Boolean = state is RestaurantDetailUiState.Loading

@OptIn(ExperimentalObjCName::class)
@ObjCName("isRestaurantDetailUiStateError")
fun isRestaurantDetailUiStateError(state: RestaurantDetailUiState): Boolean = state is RestaurantDetailUiState.Error

@OptIn(ExperimentalObjCName::class)
@ObjCName("getRestaurantListUiStateAsSuccess")
fun getRestaurantListUiStateAsSuccess(state: RestaurantListUiState): RestaurantListUiState.Success? =
    state as? RestaurantListUiState.Success

@OptIn(ExperimentalObjCName::class)
@ObjCName("getRestaurantDetailUiStateAsSuccess")
fun getRestaurantDetailUiStateAsSuccess(state: RestaurantDetailUiState): RestaurantDetailUiState.Success? =
    state as? RestaurantDetailUiState.Success

// ============================================================================
// Navigation Type Exports
// ============================================================================

@OptIn(ExperimentalObjCName::class)
@ObjCName("_exportDeepLinkProcessorType")
fun exportDeepLinkProcessorType(processor: DeepLinkProcessor): DeepLinkProcessor = processor

@OptIn(ExperimentalObjCName::class)
@ObjCName("_exportFilterModalRouteType")
fun exportFilterModalRouteType(route: FilterModalRoute): FilterModalRoute = route

@OptIn(ExperimentalObjCName::class)
@ObjCName("_exportConfirmActionModalRouteType")
fun exportConfirmActionModalRouteType(route: ConfirmActionModalRoute): ConfirmActionModalRoute = route

@OptIn(ExperimentalObjCName::class)
@ObjCName("_exportSubmitReviewModalRouteType")
fun exportSubmitReviewModalRouteType(route: SubmitReviewModalRoute): SubmitReviewModalRoute = route

@OptIn(ExperimentalObjCName::class)
@ObjCName("_exportDatePickerModalRouteType")
fun exportDatePickerModalRouteType(route: DatePickerModalRoute): DatePickerModalRoute = route

// ============================================================================
// Constants Helper Functions
// ============================================================================

@OptIn(ExperimentalObjCName::class)
@ObjCName("getRouteConstantsValue")
fun getRouteConstantsValue(): String = RouteConstants.ROUTE_RESTAURANT_LIST

@OptIn(ExperimentalObjCName::class)
@ObjCName("getDeepLinkConstantsValue")
fun getDeepLinkConstantsValue(): String = DeepLinkConstants.HOST_RESTAURANTS

@OptIn(ExperimentalObjCName::class)
@ObjCName("getStringResourcesObject")
fun getStringResourcesObject(): Any = StringResources

@OptIn(ExperimentalObjCName::class)
@ObjCName("getStringResourcesValue")
fun getStringResourcesValue(): String = StringResources.app_title

// ============================================================================
// StateFlow Bridge (for Swift AsyncStream integration)
// ============================================================================

@OptIn(ExperimentalObjCName::class)
@ObjCName("subscribeToStateFlow")
fun <T> subscribeToStateFlowFromAggregator(
    lifecycle: LifecycleOwner,
    stateFlow: StateFlow<T>,
    onStateChanged: (T) -> Unit
): Job = subscribeToStateFlow(lifecycle, stateFlow, onStateChanged)
