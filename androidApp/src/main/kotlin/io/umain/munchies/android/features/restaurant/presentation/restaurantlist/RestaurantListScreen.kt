package io.umain.munchies.android.features.restaurant.presentation.restaurantlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.umain.munchies.android.R
import io.umain.munchies.android.navigation.LocalRouteRegistry
import io.umain.munchies.android.ui.components.FilterChipCompose
import io.umain.munchies.android.ui.components.FilterChipSkeleton
import io.umain.munchies.android.ui.components.RestaurantCardCompose
import io.umain.munchies.android.ui.components.RestaurantCardSkeleton
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.android.ui.toComposeColor
import io.umain.munchies.android.ui.toComposeTextStyle
import io.umain.munchies.core.localization.StringResourceProvider
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.RestaurantListRoute

/**
 * Restaurant List Screen
 * 
 * Displays a list of restaurants with filter capabilities.
 * 
 * Design Token Usage:
 * - Spacing: [DesignTokens.Spacing] for all margins and padding
 * - Sizes: [DesignTokens.Sizes] for component dimensions
 * - Typography: [DesignTokens.Typography.TextStyles] converted via [toComposeTextStyle]
 * 
 * See: figma/figma_css_normalization_pipeline.md - "Android Implementation" section
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun RestaurantListScreen(
    navigationViewModel: RestaurantNavigationViewModel,
    stringProvider: StringResourceProvider,
    modifier: Modifier = Modifier
) {
    val registry = LocalRouteRegistry.current
    val route = remember { RestaurantListRoute() }
    
    val viewModel = remember {
        val scope = registry.createScopeForRoute(route)
        scope.get<RestaurantListAndroidViewModel>()
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilterIds by viewModel.selectedFilters.collectAsStateWithLifecycle()
    // Trigger load once
    LaunchedEffect(Unit) { viewModel.load() }

    val filteredRestaurants = when (val state = uiState) {
        is RestaurantListUiState.Success -> state.restaurants
        else -> emptyList()
    }
    
    val filters = when (val state = uiState) {
        is RestaurantListUiState.Success -> state.filters
        else -> emptyList()
    }

    val isFiltering = uiState is RestaurantListUiState.Success
            && (uiState as RestaurantListUiState.Success).isFiltering

    val scale by animateFloatAsState(
        targetValue = if (isFiltering) 0.97f else 1f
    )

    val alpha by animateFloatAsState(
        targetValue = if (isFiltering) 0.6f else 1f
    )
    
    Scaffold(
        containerColor = DesignTokens.Colors.Background.primary.toComposeColor(),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when (uiState) {
            is RestaurantListUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = DesignTokens.Spacing.lg.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = DesignTokens.Spacing.lg.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(55.dp, 54.dp)
                            )
                            IconButton(
                                onClick = { }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_tune),
                                    contentDescription = "Filters",
                                    tint = DesignTokens.Colors.Text.picto.toComposeColor()
                                )
                            }
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg.dp)
                    ) {
                        items(3) {
                            FilterChipSkeleton()

                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = DesignTokens.Spacing.lg.dp),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg.dp)
                    ) {
                        items(3) {
                            RestaurantCardSkeleton(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            is RestaurantListUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = DesignTokens.Spacing.lg.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = DesignTokens.Spacing.lg.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(55.dp, 54.dp)
                            )
                            IconButton(
                                onClick = {
                                    navigationViewModel.showFilterModal(selectedFilterIds.toList())
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_tune),
                                    contentDescription = "Filters",
                                    tint = DesignTokens.Colors.Text.picto.toComposeColor()
                                )
                            }
                        }
                    }
                    
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg.dp)
                    ) {
                        items(filters) { filter ->
                            FilterChipCompose(
                                data = filter,
                                onSelect = { _ ->
                                    // Delegate selection changes to ViewModel
                                    viewModel.toggleFilter(filter.id)
                                },
                                modifier = Modifier
                                    .height(DesignTokens.Sizes.Filter.height.dp)
                            )
                        }
                    }

                    AnimatedContent(
                        modifier = Modifier
                            .padding(top = DesignTokens.Spacing.lg.dp),
                        targetState = filteredRestaurants.size,
                        transitionSpec = {
                            fadeIn().togetherWith(fadeOut())
                        }
                    ) { count ->
                        Text(
                            text = stringProvider.stringResource(StringResources.restaurantsResultCount, count),
                            style = DesignTokens.Typography.TextStyles.title2.toComposeTextStyle()
                        )
                    }
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                            .padding(top = DesignTokens.Spacing.lg.dp),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg.dp)
                    ) {
                        items(
                            items = filteredRestaurants,
                            key = { it.id }
                        ) { restaurant ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically() + slideInVertically { it / 4 },
                                exit = fadeOut() + shrinkVertically(),
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                RestaurantCardCompose(
                                    data = restaurant,
                                    onTap = {
                                        navigationViewModel.showRestaurantDetail(restaurant.id)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            is RestaurantListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as RestaurantListUiState.Error).message,
                            style = DesignTokens.Typography.TextStyles.title2.toComposeTextStyle()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestaurantListScreenPreview() {
    val fakeStringProvider = object : StringResourceProvider {
        override fun stringResource(key: String, vararg args: Any) = "Preview"
        override fun pluralResource(key: String, quantity: Int, vararg args: Any) = "Preview"
    }
    // Fake AppCoordinator for preview
    val fakeCoordinator = object : AppCoordinator() {}
    val fakeDispatcher = NavigationDispatcher(fakeCoordinator)
    val fakeNavigationViewModel = RestaurantNavigationViewModel(fakeDispatcher)
    MunchiesTheme {
        RestaurantListScreen(
            navigationViewModel = fakeNavigationViewModel,
            stringProvider = fakeStringProvider
        )
    }
}
