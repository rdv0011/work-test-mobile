package io.umain.munchies.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.umain.munchies.localization.tr
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
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

@Composable
fun AppNavigation(coordinator: AppCoordinator) {
    val currentDestination by coordinator.currentDestination.collectAsState()
    
    when (currentDestination) {
        is Destination.RestaurantList -> {
            Text(tr("restaurant.list.title"))
        }
        is Destination.RestaurantDetail -> {
            val restaurantId = (currentDestination as Destination.RestaurantDetail).restaurantId
            Text("${tr("restaurant.detail.title")}: $restaurantId")
        }
    }
}

@Composable
fun MunchiesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

@Preview
@Composable
fun DefaultPreview() {
    MunchiesTheme {
        Text("Hello, Android!")
    }
}
