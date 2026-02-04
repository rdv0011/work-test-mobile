import Foundation
import shared

final class RestaurantDetailViewModelHolder: ObservableObject {

    let viewModel: RestaurantDetailViewModel = FeatureRestaurantIosKt.getRestaurantDetailViewModelIos()

    deinit {
        viewModel.close()
    }
}
