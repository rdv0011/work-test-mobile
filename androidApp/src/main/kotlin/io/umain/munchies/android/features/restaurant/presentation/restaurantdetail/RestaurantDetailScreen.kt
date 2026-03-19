package io.umain.munchies.android.features.restaurant.presentation.restaurantdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.umain.munchies.android.navigation.LocalRouteRegistry
import io.umain.munchies.android.ui.components.DetailCardCompose
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.core.localization.stringResource
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.model.DetailCardData
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel

@Composable
fun RestaurantDetailContent(
    uiState: RestaurantDetailUiState,
    onBackClick: () -> Unit,
    onLeaveReviewClick: (restaurantId: String) -> Unit,
    modifier: Modifier = Modifier,
    restaurantId: String = "",
    backButtonContentDescription: String? = null,
) {
    Column(
        modifier = modifier.fillMaxSize()
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
                val detailCardData = uiState.detailCardData

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
                            model = detailCardData.imageUrl,
                            contentDescription = detailCardData.title,
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { onBackClick() },
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
                                   contentDescription = backButtonContentDescription ?: stringResource(StringResources.accessibility_back_button)
                               )
                          }
                      }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        DetailCardCompose(
                            data = detailCardData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-40).dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing.lg.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    onLeaveReviewClick(restaurantId)
                                }
                            ) {
                                Text("Leave a Review")
                            }
                        }
                    }
                }
            }

            is RestaurantDetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    navigationViewModel: RestaurantNavigationViewModel,
) {
    val registry = LocalRouteRegistry.current
    val route = remember { RestaurantDetailRoute(restaurantId) }

    val viewModel = remember {
        val scope = registry.createScopeForRoute(route)
        scope.get<RestaurantDetailViewModel>()
    }
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()

    RestaurantDetailContent(
        uiState = uiState,
        restaurantId = restaurantId,
        onBackClick = { navigationViewModel.navigateBack() },
        onLeaveReviewClick = { navigationViewModel.showSubmitReviewModal(it) },
        backButtonContentDescription = stringResource(StringResources.accessibility_back_button)
    )
}

@Preview(showBackground = true)
@Composable
private fun RestaurantDetailScreenPreview() {
    MunchiesTheme {
        RestaurantDetailContent(
            uiState = RestaurantDetailUiState.Success(
                detailCardData = DetailCardData(
                    title = "Wayne's Burgers",
                    imageUrl = "",
                    tags = listOf("Take-Out", "Fast delivery", "Eat-In"),
                    statusColor = "#2ECC71",
                    statusText = "Open Now",
                ),
            ),
            restaurantId = "1",
            onBackClick = {},
            onLeaveReviewClick = {},
            backButtonContentDescription = "Back",
        )
    }
}