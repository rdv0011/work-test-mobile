package io.umain.munchies.android.features.restaurantlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.localization.tr
import io.umain.munchies.navigation.AppCoordinator

@Composable
fun RestaurantListScreen(
    coordinator: AppCoordinator,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = tr("app.title"),
                style = MaterialTheme.typography.headlineLarge
            )
            
            Text(
                text = tr("restaurant.list.title"),
                style = MaterialTheme.typography.titleMedium
            )
            
            Button(
                onClick = {
                    coordinator.navigateToRestaurantDetail("test-123")
                }
            ) {
                Text("View Restaurant Detail")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestaurantListScreenPreview() {
    MunchiesTheme {
        RestaurantListScreen(
            coordinator = AppCoordinator()
        )
    }
}
