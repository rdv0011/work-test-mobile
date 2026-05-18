import shared

// MARK: - Restaurant Feature ViewModels
typealias RestaurantNavigationViewModel = Feature_restaurantRestaurantNavigationViewModel
typealias RestaurantListViewModel = Feature_restaurantRestaurantListViewModel
typealias RestaurantDetailViewModel = Feature_restaurantRestaurantDetailViewModel

// MARK: - Settings Feature ViewModels
typealias SettingsViewModel = Feature_settingsSettingsViewModel
typealias SettingsNavigationViewModel = Feature_settingsSettingsNavigationViewModel

// MARK: - ViewModelHolders
typealias RestaurantListViewModelHolder = RestaurantListViewModelHolder
typealias RestaurantDetailViewModelHolder = RestaurantDetailViewModelHolder
typealias SettingsViewModelHolder = SettingsViewModelHolder

// MARK: - UI State Types
typealias RestaurantListUiState = Feature_restaurantRestaurantListUiState
typealias RestaurantListUiStateSuccess = Feature_restaurantRestaurantListUiStateSuccess
typealias RestaurantDetailUiState = Feature_restaurantRestaurantDetailUiState
typealias SettingsUiState = Feature_settingsSettingsUiState

// MARK: - Navigation Types
typealias CoreRoute = CoreRoute
typealias CoreDestination = CoreDestination
typealias CoreModalRoute = CoreModalRoute

// MARK: - DI & Scope Types
typealias Scope = Koin_coreScope
typealias Koin = Koin_coreKoin
