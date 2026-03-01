package io.umain.munchies.android.features.restaurant.presentation.restaurantlist

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.core.ui.TextId
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.localization.tr
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.ModalDestination
import io.umain.munchies.android.navigation.LocalRouteRegistry
import io.umain.munchies.android.ui.components.FilterChipCompose
import io.umain.munchies.android.ui.components.RestaurantCardCompose
import io.umain.munchies.feature.restaurant.presentation.model.FilterChipData
import io.umain.munchies.feature.restaurant.presentation.model.RestaurantCardData
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import androidx.compose.runtime.remember

@Composable
fun RestaurantListScreen(
    navigationViewModel: RestaurantNavigationViewModel,
    modifier: Modifier = Modifier
) {
    val registry = LocalRouteRegistry.current
    val route = remember { io.umain.munchies.navigation.RestaurantListRoute() }
    
    val viewModel = remember {
        val scope = registry.createScopeForRoute(route)
        scope.get<RestaurantListAndroidViewModel>()
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilterIds by viewModel.selectedFilters.collectAsStateWithLifecycle()
    // Trigger load once
    LaunchedEffect(Unit) { viewModel.load() }

    val filteredRestaurants = when (val state = uiState) {
        is RestaurantListUiState.Success -> state.restaurants.map {
            // convert domain Restaurant to RestaurantCardData for Compose component
            RestaurantCardData(
                id = it.id,
                restaurantName = it.name,
                tags = it.filterIds,
                deliveryTime = "", // API doesn't provide delivery time in domain model
                distance = "",
                rating = it.rating.toDouble(),
                imageUrl = it.imageUrl
            )
        }
        else -> emptyList()
    }
    
    val filters = when (val state = uiState) {
        is RestaurantListUiState.Success -> state.filters.map {
            FilterChipData(
                id = it.id,
                label = it.name,
                iconUrl = it.imageUrl,
                isSelected = selectedFilterIds.contains(it.id)
            )
        }
        else -> emptyList()
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = DesignTokens.Spacing.lg.dp,
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
                            text = tr(TextId.AppTitle),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = tr(TextId.RestaurantListTitle),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = DesignTokens.Spacing.sm.dp)
                        )
                    }
                    Button(
                        onClick = {
                            navigationViewModel.showFilterModal(selectedFilterIds.toList())
                        },
                        modifier = Modifier.padding(start = DesignTokens.Spacing.md.dp)
                    ) {
                        Text("Filters")
                    }
                }
            }
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.Spacing.lg.dp),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm.dp)
            ) {
                items(filters) { filter ->
                    FilterChipCompose(
                        data = filter,
                        onSelect = { selected ->
                            // Delegate selection changes to ViewModel
                            viewModel.toggleFilter(filter.id)
                        },
                        modifier = Modifier
                            .height(DesignTokens.Sizes.Filter.height.dp)
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = DesignTokens.Spacing.lg.dp),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md.dp),
                contentPadding = PaddingValues(
                    horizontal = DesignTokens.Spacing.lg.dp,
                    vertical = DesignTokens.Spacing.sm.dp
                )
            ) {
                items(filteredRestaurants) { restaurant ->
                    RestaurantCardCompose(
                        data = restaurant,
                        onTap = {
                            navigationViewModel.showRestaurantDetail(restaurant.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignTokens.Sizes.Card.Restaurant.height.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestaurantListScreenPreview() {
    MunchiesTheme {
    }
}
