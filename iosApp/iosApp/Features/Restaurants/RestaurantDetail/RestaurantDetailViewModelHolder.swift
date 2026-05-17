import Foundation
import shared

final class Feature_restaurantRestaurantDetailViewModelHolder: ObservableObject {
    let viewModel: Feature_restaurantRestaurantDetailViewModel
    let navigationViewModel: Feature_restaurantRestaurantNavigationViewModel
    let scope: Koin_coreScope
    let restaurantId: String
    
    init(restaurantId: String, scope: Koin_coreScope, viewModel: Feature_restaurantRestaurantDetailViewModel) {
        self.restaurantId = restaurantId
        self.scope = scope
        self.viewModel = viewModel
        self.navigationViewModel = FeatureRestaurantIosKt.getRestaurantNavigationViewModelIos()
    }
    
    deinit {
        viewModel.close()
    }
}
