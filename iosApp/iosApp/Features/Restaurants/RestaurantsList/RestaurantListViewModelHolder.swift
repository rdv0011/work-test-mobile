import Foundation
import shared
import os.log

private let logger = Logger(subsystem: "com.munchies.ios", category: "RestaurantListViewModelHolder")

final class RestaurantListViewModelHolder: ObservableObject {
    let viewModel: RestaurantListViewModel
    let navigationViewModel: RestaurantNavigationViewModel
    let scope: Koin_coreScope
    
    init(scope: Koin_coreScope, viewModel: RestaurantListViewModel) {
        self.scope = scope
        self.viewModel = viewModel
        self.navigationViewModel = viewModel.navigationViewModel
        logger.debug("[RestaurantListViewModelHolder] init() called")
    }
    
    deinit {
        logger.debug("[RestaurantListViewModelHolder] deinit() called - about to close viewModel")
        viewModel.close()
        logger.debug("[RestaurantListViewModelHolder] deinit() completed - viewModel closed")
    }
}
