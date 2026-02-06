import SwiftUI
import shared

struct RestaurantDetailDestinationView: View {
    let restaurantId: String
    
    @StateObject private var holder: RestaurantDetailViewModelHolder
    
    init(restaurantId: String) {
        self.restaurantId = restaurantId
        _holder = StateObject(wrappedValue: RestaurantDetailViewModelHolder(restaurantId: restaurantId))
    }
    
    var body: some View {
        RestaurantDetailView(
            restaurantId: restaurantId,
            viewModel: holder.viewModel
        )
    }
}