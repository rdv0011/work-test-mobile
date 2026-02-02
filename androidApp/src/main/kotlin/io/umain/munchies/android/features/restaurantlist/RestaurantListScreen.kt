package io.umain.munchies.android.features.restaurantlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import org.koin.androidx.compose.koinInject
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.core.TextId
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.localization.tr
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.android.ui.components.FilterChipCompose
import io.umain.munchies.android.ui.components.FilterChipData
import io.umain.munchies.android.ui.components.RestaurantCardCompose
import io.umain.munchies.android.ui.components.RestaurantCardData

private val exampleRestaurants = listOf(
    RestaurantCardData(
        restaurantName = "Burger Palace",
        tags = listOf("Burgers", "Fast Food", "American"),
        deliveryTime = "25-35 min",
        distance = "2.4 km",
        rating = 4.8,
        imageUrl = "https://via.placeholder.com/343x132?text=Burger+Palace"
    ),
    RestaurantCardData(
        restaurantName = "Sushi Paradise",
        tags = listOf("Japanese", "Sushi", "Seafood"),
        deliveryTime = "30-45 min",
        distance = "3.1 km",
        rating = 4.6,
        imageUrl = "https://via.placeholder.com/343x132?text=Sushi+Paradise"
    ),
    RestaurantCardData(
        restaurantName = "Pizza Pizzeria",
        tags = listOf("Italian", "Pizza", "Pasta"),
        deliveryTime = "20-30 min",
        distance = "1.8 km",
        rating = 4.7,
        imageUrl = "https://via.placeholder.com/343x132?text=Pizza+Pizzeria"
    )
)

private val exampleFilters = listOf(
    FilterChipData(
        id = "all",
        label = "All",
        iconUrl = "https://via.placeholder.com/48x48?text=All"
    ),
    FilterChipData(
        id = "fast-food",
        label = "Fast Food",
        iconUrl = "https://via.placeholder.com/48x48?text=Fast"
    ),
    FilterChipData(
        id = "asian",
        label = "Asian",
        iconUrl = "https://via.placeholder.com/48x48?text=Asian"
    )
)

@Composable
fun RestaurantListScreen(
    coordinator: AppCoordinator,
    modifier: Modifier = Modifier
) {
    // Use shared ViewModel (provided by Koin)
    val viewModel: RestaurantListViewModel by koinInject()
    val uiState by viewModel.uiState.collectAsState(initial = RestaurantListUiState.Loading)
    val selectedFilterIds by viewModel.selectedFilters.collectAsState(initial = emptySet())
    // Trigger load once
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.load() }

    val filteredRestaurants = when (val state = uiState) {
        is RestaurantListUiState.Success -> state.restaurants.map {
            // convert domain Restaurant to RestaurantCardData for Compose component
            RestaurantCardData(
                restaurantName = it.name,
                tags = it.filterIds,
                deliveryTime = "", // API doesn't provide delivery time in domain model
                distance = "",
                rating = it.rating.toDouble(),
                imageUrl = it.imageUrl
            )
        }
        else -> exampleRestaurants
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
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.Spacing.lg.dp),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm.dp)
            ) {
                items(exampleFilters) { filter ->
                    val isSelected = selectedFilterIds.contains(filter.id)
                    FilterChipCompose(
                        data = filter.copy(isSelected = isSelected),
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = DesignTokens.Spacing.lg.dp,
                    vertical = DesignTokens.Spacing.sm.dp
                )
            ) {
                items(filteredRestaurants) { restaurant ->
                    RestaurantCardCompose(
                        data = restaurant,
                        onTap = {
                            coordinator.navigateToRestaurantDetail(restaurant.restaurantName)
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
        RestaurantListScreen(
            coordinator = AppCoordinator()
        )
    }
}
