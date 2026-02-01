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
import io.umain.munchies.ui.RestaurantCardColors
import io.umain.munchies.ui.RestaurantCardData
import io.umain.munchies.ui.RestaurantCardDimensions
import io.umain.munchies.ui.RestaurantCardTypography
import androidx.core.graphics.toColorInt

@Composable
fun RestaurantCardCompose(
    data: RestaurantCardData,
    dimensions: RestaurantCardDimensions,
    colors: RestaurantCardColors,
    typography: RestaurantCardTypography,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = dimensions.cornerRadius.dp,
        topEnd = dimensions.cornerRadius.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    Card(
        modifier = modifier
            .semantics { contentDescription = data.contentDescription },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color(colors.backgroundColor.toColorInt())),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onTap
    ) {
        Text(
            text = data.restaurantName,
            style = TextStyle(
                fontWeight = FontWeight(typography.titleStyle.fontWeight),
                fontSize = typography.titleStyle.fontSize.sp
            ),
            color = Color(colors.titleColor.toColorInt())
        )
        
        Text(
            text = data.tags.joinToString(" • "),
            style = TextStyle(
                fontWeight = FontWeight(typography.tagStyle.fontWeight),
                fontSize = typography.tagStyle.fontSize.sp
            ),
            color = Color(colors.tagColor.toColorInt())
        )
        
        Text(
            text = "${data.deliveryTime} - ${data.distance}",
            style = TextStyle(
                fontWeight = FontWeight(typography.metaStyle.fontWeight),
                fontSize = typography.metaStyle.fontSize.sp
            ),
            color = Color(colors.metaColor.toColorInt())
        )
        
        Text(
            text = "★ " + String.format("%.1f", data.rating),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = Color(colors.ratingColor.toColorInt())
        )
    }
}
