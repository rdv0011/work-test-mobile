package io.umain.munchies.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import io.umain.munchies.navigation.DeepLinkConstants
import io.umain.munchies.navigation.DeepLinkProcessor
import io.umain.munchies.android.navigation.AppNavigation
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.persistence.NavigationStateRestorer
import io.umain.munchies.core.analytics.NavigationAnalyticsListener
import io.umain.munchies.logging.logError
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val coordinator: AppCoordinator by inject()
    private var analyticsListener: NavigationAnalyticsListener? = null
    private var launchDeepLinkUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeAppState(savedInstanceState)

        setContent {
            MunchiesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(coordinator, launchDeepLinkUri)
                }
            }
        }
    }

    private fun initializeAppState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                try {
                    val restorer: NavigationStateRestorer by inject()
                    val restoredState = restorer.restoreNavigationState()
                    coordinator.applyNavigationState(restoredState)
                } catch (e: Exception) {
                    logError("MainActivity", "Failed to restore navigation state: ${e.message}")
                }
            }
        }

        initAnalyticsTracking()
        launchDeepLinkUri = getColdStartDeepLinkUri(savedInstanceState)
    }

    override fun onDestroy() {
        analyticsListener?.close()
        analyticsListener = null
        super.onDestroy()
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun initAnalyticsTracking() {
        analyticsListener = try {
            val listener: NavigationAnalyticsListener by inject()
            listener.apply { startTracking() }
        } catch (e: Exception) {
            null
        }
    }

    private fun getColdStartDeepLinkUri(savedInstanceState: Bundle?): Uri? {
        return if (savedInstanceState == null) {
            extractDeepLinkUri(intent)?.also { clearConsumedDeepLinkFromIntent() }
        } else {
            null
        }
    }

    private fun clearConsumedDeepLinkFromIntent() {
        intent = intent?.apply {
            data = null
        }
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
