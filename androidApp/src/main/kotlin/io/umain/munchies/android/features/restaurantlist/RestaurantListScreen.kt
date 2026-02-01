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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.core.TextId
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.localization.tr
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.android.ui.components.FilterChipCompose
import io.umain.munchies.android.ui.components.RestaurantCardCompose
import io.umain.munchies.ui.FilterChipColors
import io.umain.munchies.ui.FilterChipData
import io.umain.munchies.ui.FilterChipDimensions
import io.umain.munchies.ui.FilterChipTypography
import io.umain.munchies.ui.RestaurantCardColors
import io.umain.munchies.ui.RestaurantCardData
import io.umain.munchies.ui.RestaurantCardDimensions
import io.umain.munchies.ui.RestaurantCardTypography

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
    val selectedFilterIds = remember { mutableStateOf(setOf("all")) }
    
    val filteredRestaurants = if (selectedFilterIds.value.contains("all")) {
        exampleRestaurants
    } else {
        exampleRestaurants.filter { restaurant ->
            selectedFilterIds.value.any { filterId ->
                when (filterId) {
                    "fast-food" -> restaurant.tags.contains("Fast Food")
                    "asian" -> restaurant.tags.any { it in listOf("Japanese", "Thai", "Asian") }
                    "vegetarian" -> restaurant.tags.contains("Vegetarian")
                    "premium" -> restaurant.rating >= 4.7
                    else -> false
                }
            }
        }
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
                    val isSelected = selectedFilterIds.value.contains(filter.id)
                    FilterChipCompose(
                        data = filter.copy(isSelected = isSelected),
                        dimensions = FilterChipDimensions(),
                        colors = FilterChipColors(),
                        typography = FilterChipTypography(),
                        onSelect = { selected ->
                            val newSelectedIds = selectedFilterIds.value.toMutableSet()
                            if (selected) {
                                newSelectedIds.remove("all")
                                newSelectedIds.add(filter.id)
                            } else {
                                newSelectedIds.remove(filter.id)
                                if (newSelectedIds.isEmpty()) {
                                    newSelectedIds.add("all")
                                }
                            }
                            selectedFilterIds.value = newSelectedIds
                        },
                        modifier = Modifier
                            .height(FilterChipDimensions().height.dp)
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
                        dimensions = RestaurantCardDimensions(),
                        colors = RestaurantCardColors(),
                        typography = RestaurantCardTypography(),
                        onTap = {
                            coordinator.navigateToRestaurantDetail(restaurant.restaurantName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(RestaurantCardDimensions().height.dp)
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
