package io.umain.munchies.android.features.restaurant.presentation.restaurantdetail

import androidx.lifecycle.ViewModel
import io.umain.munchies.core.lifecycle.Closeable
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState
import io.umain.munchies.logging.logInfo
import kotlinx.coroutines.flow.StateFlow

class RestaurantDetailAndroidViewModel(
    private val shared: RestaurantDetailViewModel
) : ViewModel(), Closeable {
    init {
        logInfo("RestaurantDetailAndroidViewModel", "Created")
    }

    val uiState: StateFlow<RestaurantDetailUiState> = shared.stateFlow
    val restaurantId: String get() = shared.restaurantId

    fun load() = shared.load()
    fun submitReview(rating: Int, comment: String) = shared.submitReview(rating, comment)

    override fun close() {
        shared.close()
    }
}
