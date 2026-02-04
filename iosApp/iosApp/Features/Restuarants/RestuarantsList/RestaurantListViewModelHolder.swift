import Foundation
import shared

final class RestaurantListViewModelHolder: ObservableObject {
    let viewModel: RestaurantListViewModel = FeatureRestaurantIosKt.getRestaurantListViewModelIos()
    
    deinit {
        viewModel.close()
    }
}
