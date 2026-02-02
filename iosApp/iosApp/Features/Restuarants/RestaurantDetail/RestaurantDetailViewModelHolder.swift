import Foundation
import shared

@objcMembers
final class RestaurantDetailViewModelHolder: ObservableObject {
    let viewModel: RestaurantDetailViewModel

    init(viewModel: RestaurantDetailViewModel) {
        self.viewModel = viewModel
    }
}
