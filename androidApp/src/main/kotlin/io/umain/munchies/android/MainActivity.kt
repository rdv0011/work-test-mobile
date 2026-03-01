package io.umain.munchies.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.umain.munchies.navigation.DeepLinkConstants
import io.umain.munchies.navigation.DeepLinkProcessor
import io.umain.munchies.android.navigation.AppNavigation
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.navigation.AppCoordinator
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val coordinator: AppCoordinator by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val pendingDeepLinkUri = extractDeepLinkUri(intent)
        
        setContent {
            MunchiesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(coordinator, pendingDeepLinkUri)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // When app is already running, process deep link immediately
        // The event listener is already active
        handleDeepLink(intent)
    }
    
    private fun extractDeepLinkUri(intent: android.content.Intent): Uri? {
        val data = intent.data ?: return null
        return if (data.scheme == DeepLinkConstants.SCHEME) data else null
    }
    
    private fun handleDeepLink(intent: android.content.Intent) {
        val data = intent.data ?: return
        
        if (data.scheme != DeepLinkConstants.SCHEME) return
        processDeepLinkUri(data)
    }
    
    private fun processDeepLinkUri(data: Uri) {
        val host = data.host ?: return
        val pathSegments = data.pathSegments
        
        val queryParams = mutableMapOf<String, String>()
        listOf(
            DeepLinkConstants.QUERY_PARAM_FILTERS,
            DeepLinkConstants.QUERY_PARAM_MESSAGE,
            DeepLinkConstants.QUERY_PARAM_CONFIRM_TEXT,
            DeepLinkConstants.QUERY_PARAM_CANCEL_TEXT,
            DeepLinkConstants.QUERY_PARAM_INITIAL_DATE
        ).forEach { param ->
            val value = data.getQueryParameter(param)
            if (value != null) {
                queryParams[param] = value
            }
        }
        
        DeepLinkProcessor.processDeepLink(
            host = host,
            pathSegments = pathSegments,
            queryParams = queryParams,
            coordinator = coordinator
        )
    }
}

