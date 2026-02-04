package io.umain.munchies.android.features.restaurant.presentation.restaurantdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.umain.munchies.android.ui.components.DetailCardCompose
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.core.ui.TextId
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.presentation.model.DetailCardData
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState
import io.umain.munchies.localization.tr
import io.umain.munchies.navigation.AppCoordinator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    coordinator: AppCoordinator,
    modifier: Modifier = Modifier
) {
    // Use Android lifecycle-aware wrapper ViewModel (delegates to shared ViewModel)
    val viewModel: RestaurantDetailAndroidViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load data on first composition
    androidx.compose.runtime.LaunchedEffect(restaurantId) {
        viewModel.load(restaurantId)
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tr(TextId.RestaurantDetailTitle)) },
                navigationIcon = {
                    IconButton(onClick = { coordinator.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = tr(TextId.AccessibilityBackButton)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is RestaurantDetailUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            is RestaurantDetailUiState.Success -> {
                val successState = uiState as RestaurantDetailUiState.Success
                val restaurant = successState.restaurant
                val status = successState.status
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = restaurant.imageUrl,
                        contentDescription = restaurant.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.lg.dp),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md.dp)
                    ) {
                        val statusColor = if (status == io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus.OPEN) {
                            DesignTokens.Colors.Accent.positive
                        } else {
                            DesignTokens.Colors.Accent.negative
                        }
                        
                        val statusText = if (status == io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus.OPEN) {
                            tr(TextId.RestaurantStatusOpen)
                        } else {
                            tr(TextId.RestaurantStatusClosed)
                        }
                        
                        DetailCardCompose(
                            data = DetailCardData(
                                title = restaurant.name,
                                subtitle = restaurant.name, // No description, using name
                                statusText = statusText,
                                statusColor = statusColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(DesignTokens.Sizes.Card.Detail.height.dp)
                        )
                        
                        Column(
                            modifier = Modifier.padding(top = DesignTokens.Spacing.md.dp)
                        ) {
                            Text(
                                text = "Hours of Operation",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = DesignTokens.Spacing.sm.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Opens: --:--",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Closes: --:--",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            is RestaurantDetailUiState.Error -> {
                val errorState = uiState as RestaurantDetailUiState.Error
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = errorState.message)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestaurantDetailScreenPreview() {
    MunchiesTheme {
        RestaurantDetailScreen(
            restaurantId = "Burger Palace",
            coordinator = AppCoordinator()
        )
    }
}
