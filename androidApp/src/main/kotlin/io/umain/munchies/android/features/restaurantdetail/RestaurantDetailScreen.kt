package io.umain.munchies.android.features.restaurantdetail

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.android.ui.components.DetailCardCompose
import io.umain.munchies.android.ui.components.DetailCardData
import io.umain.munchies.core.TextId
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.localization.tr
import io.umain.munchies.navigation.AppCoordinator
import java.time.LocalDateTime
import java.time.LocalTime

private val exampleRestaurantDetails = mapOf(
    "Burger Palace" to RestaurantDetailInfo(
        name = "Burger Palace",
        description = "Delicious handcrafted burgers with premium toppings",
        imageUrl = "https://via.placeholder.com/343x200?text=Burger+Palace",
        opensAt = LocalTime.of(11, 0),
        closesAt = LocalTime.of(23, 0),
        id = "burger-palace"
    ),
    "Sushi Paradise" to RestaurantDetailInfo(
        name = "Sushi Paradise",
        description = "Fresh daily sushi and authentic Japanese cuisine",
        imageUrl = "https://via.placeholder.com/343x200?text=Sushi+Paradise",
        opensAt = LocalTime.of(12, 0),
        closesAt = LocalTime.of(22, 0),
        id = "sushi-paradise"
    ),
    "Pizza Pizzeria" to RestaurantDetailInfo(
        name = "Pizza Pizzeria",
        description = "Wood-fired pizzas with authentic Italian ingredients",
        imageUrl = "https://via.placeholder.com/343x200?text=Pizza+Pizzeria",
        opensAt = LocalTime.of(10, 0),
        closesAt = LocalTime.of(23, 30),
        id = "pizza-pizzeria"
    )
)

data class RestaurantDetailInfo(
    val name: String,
    val description: String,
    val imageUrl: String,
    val opensAt: LocalTime,
    val closesAt: LocalTime,
    val id: String
)

private fun isRestaurantOpen(opensAt: LocalTime, closesAt: LocalTime): Boolean {
    val now = LocalDateTime.now().toLocalTime()
    return if (closesAt.isBefore(opensAt)) {
        now.isAfter(opensAt) || now.isBefore(closesAt)
    } else {
        now.isAfter(opensAt) && now.isBefore(closesAt)
    }
}

private fun getRestaurantDetail(restaurantId: String): RestaurantDetailInfo {
    return exampleRestaurantDetails[restaurantId]
        ?: RestaurantDetailInfo(
            name = restaurantId,
            description = "Restaurant details",
            imageUrl = "https://via.placeholder.com/343x200?text=Restaurant",
            opensAt = LocalTime.of(9, 0),
            closesAt = LocalTime.of(23, 0),
            id = restaurantId
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    coordinator: AppCoordinator,
    modifier: Modifier = Modifier
) {
    val restaurant = getRestaurantDetail(restaurantId)
    val isOpen = isRestaurantOpen(restaurant.opensAt, restaurant.closesAt)
    
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
                val statusColor = if (isOpen) {
                    DesignTokens.Colors.Accent.positive
                } else {
                    DesignTokens.Colors.Accent.negative
                }
                
                val statusText = if (isOpen) {
                    tr(TextId.RestaurantStatusOpen)
                } else {
                    tr(TextId.RestaurantStatusClosed)
                }
                
                DetailCardCompose(
                    data = DetailCardData(
                        title = restaurant.name,
                        subtitle = restaurant.description,
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
                            text = "Opens: ${restaurant.opensAt}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Closes: ${restaurant.closesAt}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
