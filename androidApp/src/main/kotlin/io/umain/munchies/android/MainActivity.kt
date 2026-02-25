package io.umain.munchies.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.umain.munchies.android.navigation.AppNavigation
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.ModalDestination
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val coordinator: AppCoordinator by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep links from intent
        handleDeepLink(intent)
        
        setContent {
            MunchiesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(coordinator)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        // Handle deep links when activity is already running
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: android.content.Intent) {
        val data = intent.data
        if (data != null) {
            when {
                // munchies://restaurants -> Restaurant List
                data.scheme == "munchies" && data.host == "restaurants" && data.pathSegments.isEmpty() -> {
                    coordinator.navigateToScreen(Destination.RestaurantList)
                }
                
                // munchies://restaurants/{restaurantId} -> Restaurant Detail
                data.scheme == "munchies" && data.host == "restaurants" && data.pathSegments.size == 1 -> {
                    val restaurantId = data.pathSegments[0]
                    coordinator.navigateToScreen(Destination.RestaurantDetail(restaurantId))
                }
                
                // munchies://settings -> Settings Tab
                data.scheme == "munchies" && data.host == "settings" -> {
                    coordinator.selectTab("settings")
                }
                
                // munchies://modal/filter?filters=tag1,tag2 -> Filter Modal
                data.scheme == "munchies" && data.host == "modal" && data.path == "/filter" -> {
                    val filtersParam = data.getQueryParameter("filters") ?: ""
                    val preSelectedFilters = if (filtersParam.isNotEmpty()) {
                        filtersParam.split(",").map { it.trim() }
                    } else {
                        emptyList()
                    }
                    coordinator.showFilterModal(preSelectedFilters)
                }
                
                // munchies://modal/submit_review/{restaurantId} -> Submit Review Modal
                data.scheme == "munchies" && data.host == "modal" && data.pathSegments.size == 2 
                        && data.pathSegments[0] == "submit_review" -> {
                    val restaurantId = data.pathSegments[1]
                    coordinator.submitReview(restaurantId)
                }
                
                // munchies://modal/confirm?message=...&confirmText=...&cancelText=... -> Confirm Dialog
                data.scheme == "munchies" && data.host == "modal" && data.path == "/confirm" -> {
                    val message = data.getQueryParameter("message") ?: "Are you sure?"
                    val confirmText = data.getQueryParameter("confirmText") ?: "OK"
                    val cancelText = data.getQueryParameter("cancelText") ?: "Cancel"
                    coordinator.showConfirmation(message, confirmText, cancelText)
                }
                
                // munchies://modal/date_picker?initialDate=2026-02-25 -> Date Picker Modal
                data.scheme == "munchies" && data.host == "modal" && data.path == "/date_picker" -> {
                    val initialDate = data.getQueryParameter("initialDate")
                    coordinator.showModal(ModalDestination.DatePicker(initialDate))
                }
            }
        }
    }
}
