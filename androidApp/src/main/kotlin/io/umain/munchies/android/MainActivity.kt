package io.umain.munchies.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.umain.munchies.android.deeplinks.DeepLinkConstants
import io.umain.munchies.android.di.registerAndroidNavigationModule
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
        
        registerAndroidNavigationModule()
        
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
        
        handleDeepLink(intent)
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: android.content.Intent) {
        val data = intent.data ?: return
        
        if (data.scheme != DeepLinkConstants.SCHEME) return
        
        when (data.host) {
            DeepLinkConstants.HOST_RESTAURANTS -> handleRestaurantDeepLink(data)
            DeepLinkConstants.HOST_SETTINGS -> coordinator.selectTab(DeepLinkConstants.TAB_ID_SETTINGS)
            DeepLinkConstants.HOST_MODAL -> handleModalDeepLink(data)
        }
    }
    
    private fun handleRestaurantDeepLink(data: android.net.Uri) {
        when {
            data.pathSegments.isEmpty() -> {
                coordinator.navigateToScreen(Destination.RestaurantList)
            }
            data.pathSegments.size == DeepLinkConstants.SINGLE_SEGMENT_PATH -> {
                val restaurantId = data.pathSegments[DeepLinkConstants.RESTAURANT_ID_INDEX]
                coordinator.navigateToScreen(Destination.RestaurantDetail(restaurantId))
            }
        }
    }
    
    private fun handleModalDeepLink(data: android.net.Uri) {
        if (data.pathSegments.isEmpty()) return
        
        val modalType = data.pathSegments[DeepLinkConstants.MODAL_TYPE_INDEX]
        
        when (modalType) {
            DeepLinkConstants.PATH_FILTER.trimStart('/') -> {
                val filtersParam = data.getQueryParameter(DeepLinkConstants.QUERY_PARAM_FILTERS) ?: ""
                val preSelectedFilters = if (filtersParam.isNotEmpty()) {
                    filtersParam.split(",").map { it.trim() }
                } else {
                    emptyList()
                }
                coordinator.showFilterModal(preSelectedFilters)
            }
            
            DeepLinkConstants.PATH_SUBMIT_REVIEW.trimStart('/') -> {
                if (data.pathSegments.size == DeepLinkConstants.TWO_SEGMENT_PATH) {
                    val restaurantId = data.pathSegments[DeepLinkConstants.SUBMIT_REVIEW_RESTAURANT_ID_INDEX]
                    coordinator.submitReview(restaurantId)
                }
            }
            
            DeepLinkConstants.PATH_CONFIRM.trimStart('/') -> {
                val message = data.getQueryParameter(DeepLinkConstants.QUERY_PARAM_MESSAGE) 
                    ?: DeepLinkConstants.DEFAULT_CONFIRM_MESSAGE
                val confirmText = data.getQueryParameter(DeepLinkConstants.QUERY_PARAM_CONFIRM_TEXT) 
                    ?: DeepLinkConstants.DEFAULT_CONFIRM_TEXT
                val cancelText = data.getQueryParameter(DeepLinkConstants.QUERY_PARAM_CANCEL_TEXT) 
                    ?: DeepLinkConstants.DEFAULT_CANCEL_TEXT
                coordinator.showConfirmation(message, confirmText, cancelText)
            }
            
            DeepLinkConstants.PATH_DATE_PICKER.trimStart('/') -> {
                val initialDate = data.getQueryParameter(DeepLinkConstants.QUERY_PARAM_INITIAL_DATE)
                coordinator.showModal(ModalDestination.DatePicker(initialDate))
            }
        }
    }
}

