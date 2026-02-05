package io.umain.munchies.android.features.restaurant.presentation.restaurantdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.umain.munchies.android.ui.components.DetailCardCompose
import io.umain.munchies.core.ui.TextId
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.domain.model.RestaurantStatus
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
    viewModel: RestaurantDetailAndroidViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load data on first composition
    LaunchedEffect(restaurantId) {
        viewModel.load(restaurantId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (uiState) {
            is RestaurantDetailUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
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
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        AsyncImage(
                            model = restaurant.imageUrl,
                            contentDescription = restaurant.name,
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { coordinator.navigateBack() },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 16.dp, top = 40.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .shadow(4.dp, CircleShape, clip = false)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = tr(TextId.AccessibilityBackButton)
                            )
                        }
                    }

                    val statusColor =
                        if (status == RestaurantStatus.OPEN) {
                            DesignTokens.Colors.Accent.positive
                        } else {
                            DesignTokens.Colors.Accent.negative
                        }

                    val statusText =
                        if (status == RestaurantStatus.OPEN) {
                            tr(TextId.RestaurantStatusOpen)
                        } else {
                            tr(TextId.RestaurantStatusClosed)
                        }

                    DetailCardCompose(
                        data = DetailCardData(
                            title = restaurant.name,
                            statusText = statusText,
                            statusColor = statusColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-45).dp)
                            .padding(DesignTokens.Spacing.lg.dp)
                            .height(DesignTokens.Sizes.Card.Detail.height.dp)
                    )
                }
            }
            is RestaurantDetailUiState.Error -> {
                val errorState = uiState as RestaurantDetailUiState.Error
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = errorState.message)
                }
            }
        }
    }
}
