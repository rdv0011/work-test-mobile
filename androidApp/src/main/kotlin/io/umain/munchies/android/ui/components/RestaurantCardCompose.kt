package io.umain.munchies.android.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.presentation.model.RestaurantCardData

@Composable
fun RestaurantCardCompose(
    data: RestaurantCardData,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleStyle = DesignTokens.Typography.TextStyles.title1
    val tagStyle = DesignTokens.Typography.TextStyles.subtitle1
    val metaStyle = DesignTokens.Typography.TextStyles.footer1
    
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
        colors = CardDefaults.cardColors(containerColor = Color(DesignTokens.Colors.Background.card.toColorInt())),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onTap
    ) {
        Text(
            text = data.restaurantName,
            style = TextStyle(
                fontWeight = FontWeight(titleStyle.fontWeight),
                fontSize = titleStyle.fontSize.sp
            ),
            color = Color(DesignTokens.Colors.Text.dark.toColorInt())
        )
        
        Text(
            text = data.tags.joinToString(" • "),
            style = TextStyle(
                fontWeight = FontWeight(tagStyle.fontWeight),
                fontSize = tagStyle.fontSize.sp
            ),
            color = Color(DesignTokens.Colors.Text.subtitle.toColorInt())
        )
        
        Text(
            text = "${data.deliveryTime} - ${data.distance}",
            style = TextStyle(
                fontWeight = FontWeight(metaStyle.fontWeight),
                fontSize = metaStyle.fontSize.sp
            ),
            color = Color(DesignTokens.Colors.Text.footer.toColorInt())
        )
        
        Text(
            text = "★ " + String.format("%.1f", data.rating),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = Color(DesignTokens.Colors.Accent.star.toColorInt())
        )
    }
}
