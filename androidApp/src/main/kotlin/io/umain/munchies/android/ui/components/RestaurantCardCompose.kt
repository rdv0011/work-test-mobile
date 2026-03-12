package io.umain.munchies.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.presentation.model.RestaurantCardData
import io.umain.munchies.android.ui.IconCompose
import io.umain.munchies.android.ui.toComposeColor
import io.umain.munchies.android.ui.toComposeTextStyle
import io.umain.munchies.core.localization.StringKey
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.core.localization.stringResource

@Composable
fun RestaurantCardCompose(
    data: RestaurantCardData,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = DesignTokens.BorderRadius.md.dp,
        topEnd = DesignTokens.BorderRadius.md.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    Card(
        modifier = modifier
            .semantics { contentDescription = data.contentDescription },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.Colors.Background.card.toComposeColor()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onTap
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            // Image Section
            AsyncImage(
                model = data.imageUrl,
                contentDescription = "${data.restaurantName} restaurant image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                contentScale = ContentScale.Crop
            )
            
            // Content Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.sm.dp),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xl.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Text
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs.dp)
                ) {
                    Text(
                        text = data.restaurantName,
                        style = DesignTokens.Typography.TextStyles.title1.toComposeTextStyle(),
                        color = DesignTokens.Colors.Text.dark.toComposeColor()
                    )
                    
                    Text(
                        text = data.tags.joinToString(" • "),
                        style = DesignTokens.Typography.TextStyles.subtitle1.toComposeTextStyle(),
                        color = DesignTokens.Colors.Text.subtitle.toComposeColor()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconCompose(
                            iconResourceName = DesignTokens.Icons.Resources.clock,
                            contentDescription = "Delivery time",
                            modifier = Modifier.size(DesignTokens.Sizes.Icon.small.dp),
                            tint = DesignTokens.Colors.Accent.brightRed
                        )
                        
                        Text(
                            text = "${data.deliveryTime} min - ${data.distance} km",
                            style = DesignTokens.Typography.TextStyles.footer1.toComposeTextStyle(),
                            color = DesignTokens.Colors.Text.footer.toComposeColor()
                        )
                    }
                }
                
                // Right Column: Rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconCompose(
                        iconResourceName = DesignTokens.Icons.Resources.star,
                        contentDescription = "Star rating",
                        modifier = Modifier.size(DesignTokens.Sizes.Icon.small.dp),
                        tint = DesignTokens.Colors.Accent.star
                    )
                    
                    Text(
                        text = stringResource(StringResources.rating_format, data.rating),
                        style = DesignTokens.Typography.TextStyles.footer1.toComposeTextStyle(),
                        color = DesignTokens.Colors.Text.footer.toComposeColor()
                    )
                }
            }
        }
    }
}
