import Foundation
import shared

@objcMembers
final class RestaurantListViewModelHolder: ObservableObject {
    let viewModel: RestaurantListViewModel

    init(viewModel: RestaurantListViewModel) {
        self.viewModel = viewModel
    }
}
