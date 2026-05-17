import Foundation
import shared

// MARK: - ViewModel Type Aliases
typealias Feature_restaurantRestaurantDetailViewModel = Feature_restaurantFeature_restaurantRestaurantDetailViewModel
typealias RestaurantListViewModel = Feature_restaurantRestaurantListViewModel
typealias SettingsViewModel = Feature_settingsSettingsViewModel

// MARK: - Data Type Aliases
typealias RestaurantCardData = Feature_restaurantRestaurantCardData
typealias FilterChipData = Feature_restaurantFilterChipData
typealias DetailCardData = Feature_restaurantDetailCardData

// MARK: - Navigation Type Aliases
typealias AppCoordinator = CoreAppCoordinator
typealias CoreRouteHandler = SharedCoreRoute
typealias Route = SharedCoreRoute
typealias ModalRoute = SharedCoreModalRoute
typealias Destination = SharedCoreDestination
typealias ModalDestination = SharedCoreModalDestination
typealias StringResources = CoreStringResources
typealias DeepLinkConstants = SharedCoreDeepLinkConstants
typealias RouteConstants = String

// MARK: - Infrastructure Type Aliases
typealias Scope = Koin_coreScope
typealias Koin = SharedKoin_coreKoin

// MARK: - Route Subtypes
typealias CoreRestaurantListRoute = SharedCoreCoreRestaurantListRoute
typealias CoreRestaurantDetailRoute = SharedCoreCoreRestaurantDetailRoute
typealias CoreSettingsRoute = SharedCoreCoreSettingsRoute

// MARK: - Kotlin Flow Type Aliases
typealias Flow = SharedKotlinx_coroutines_coreFlow
typealias FlowCollector = SharedKotlinx_coroutines_coreFlowCollector
