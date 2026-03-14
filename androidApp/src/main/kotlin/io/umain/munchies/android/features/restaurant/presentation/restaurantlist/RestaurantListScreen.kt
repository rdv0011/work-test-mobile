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
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.core.localization.stringResource
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.android.navigation.LocalRouteRegistry
import io.umain.munchies.android.ui.components.FilterChipCompose
import io.umain.munchies.android.ui.components.RestaurantCardCompose
import io.umain.munchies.feature.restaurant.presentation.model.FilterChipData
import io.umain.munchies.feature.restaurant.presentation.model.RestaurantCardData
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.modifier.modifierLocalConsumer
import com.google.common.base.Strings
import io.umain.munchies.android.ui.toComposeTextStyle
import io.umain.munchies.android.ui.toComposeColor
import io.umain.munchies.core.localization.StringKey
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignTokens.Spacing.lg.dp,)
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
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(StringResources.app_title),
                            style = DesignTokens.Typography.TextStyles.headline1.toComposeTextStyle()
                        )
                        Text(
                            text = stringResource(StringResources.restaurant_list_title),
                            style = DesignTokens.Typography.TextStyles.title1.toComposeTextStyle(),
                            modifier = Modifier.padding(top = DesignTokens.Spacing.sm.dp)
                        )
                    }
                    Button(
                        onClick = {
                            navigationViewModel.showFilterModal(selectedFilterIds.toList())
                        },
                        modifier = Modifier.padding(start = DesignTokens.Spacing.md.dp)
                    ) {
                        Text(
                            "Filters",
                            color = DesignTokens.Colors.Text.picto.toComposeColor()
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

            if (uiState is RestaurantListUiState.Success) {
                AnimatedContent(
                    modifier = Modifier
                        .padding(top = DesignTokens.Spacing.lg.dp),
                    targetState = filteredRestaurants.size,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    }
                ) { count ->
                        Text(
                            text = stringResource(StringResources.restaurantsResultCount, count),
                            style = DesignTokens.Typography.TextStyles.title2.toComposeTextStyle()
                        )
}
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
}

@Preview(showBackground = true)
@Composable
private fun RestaurantListScreenPreview() {
    MunchiesTheme {
        // TODO: Replace with actual ViewModel preview once state management pattern is finalized
        // Preview shown with Scaffold container only - full mock state integration pending
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "RestaurantListScreen",
                    style = DesignTokens.Typography.TextStyles.headline1.toComposeTextStyle()
                )
            }
        }
    }
}
