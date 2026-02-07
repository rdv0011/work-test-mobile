import Foundation
import shared

final class RestaurantDetailViewModelHolder: ObservableObject {

    let viewModel: RestaurantDetailViewModel
    private let scope: Scope

    init(restaurantId: String) {

        let handle = FeatureRestaurantIosKt.getRestaurantDetailViewModel(
            scopeId: RestaurantDetailScope(restaurantId: restaurantId),
            restaurantId: restaurantId
        )

        self.scope = handle.scope
        self.viewModel = handle.viewModel 
    }

    deinit {
        scope.close()
        viewModel.close()
    }
}
