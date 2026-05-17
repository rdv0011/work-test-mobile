import Foundation
import shared

final class RestaurantListViewModelHolder: ObservableObject {
    let viewModel: Feature_restaurantRestaurantListViewModel
    let navigationViewModel: Feature_restaurantRestaurantNavigationViewModel
    let scope: Koin_coreScope
    
    init(scope: Koin_coreScope, viewModel: Feature_restaurantRestaurantListViewModel) {
        self.scope = scope
        self.viewModel = viewModel
        self.navigationViewModel = FeatureRestaurantIosKt.getRestaurantNavigationViewModelIos()
    }
    
    deinit {
        viewModel.close()
    }
}
