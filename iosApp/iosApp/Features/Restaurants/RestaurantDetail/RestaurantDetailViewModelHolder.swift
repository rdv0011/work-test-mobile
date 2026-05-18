import Foundation
import shared
import os.log

private let logger = Logger(subsystem: "com.munchies.ios", category: "RestaurantDetailViewModelHolder")

final class RestaurantDetailViewModelHolder: ObservableObject {
    let viewModel: RestaurantDetailViewModel
    let navigationViewModel: RestaurantNavigationViewModel
    let scope: Koin_coreScope
    let restaurantId: String
    
    init(restaurantId: String, scope: Koin_coreScope, viewModel: RestaurantDetailViewModel) {
        self.restaurantId = restaurantId
        self.scope = scope
        self.viewModel = viewModel
        self.navigationViewModel = viewModel.navigationViewModel
        logger.debug("[RestaurantDetailViewModelHolder] init() called for restaurantId: \(restaurantId)")
    }
    
    deinit {
        logger.debug("[RestaurantDetailViewModelHolder] deinit() called - about to close viewModel")
        viewModel.close()
        logger.debug("[RestaurantDetailViewModelHolder] deinit() completed - viewModel closed")
    }
}
