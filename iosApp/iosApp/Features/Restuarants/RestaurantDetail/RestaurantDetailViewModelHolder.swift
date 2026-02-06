import Foundation
import shared

final class RestaurantDetailViewModelHolder: ObservableObject {

    let viewModel: RestaurantDetailViewModel
    private let scope: Scope

    init(restaurantId: String) {

        let handle = ScopedViewModelFactoryKt.scopedViewModel(
            scopeId: RestaurantDetailScope(restaurantId: restaurantId),
            params: [restaurantId]
        )

        self.scope = handle.scope
        self.viewModel = handle.viewModel as! RestaurantDetailViewModel
    }

    deinit {
        scope.close()
        viewModel.close()
    }
}
