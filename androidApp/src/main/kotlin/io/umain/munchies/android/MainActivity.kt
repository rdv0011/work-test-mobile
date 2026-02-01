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
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val coordinator: AppCoordinator by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
}
