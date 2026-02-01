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
import io.umain.munchies.ui.DetailCardColors
import io.umain.munchies.ui.DetailCardData
import io.umain.munchies.ui.DetailCardDimensions
import io.umain.munchies.ui.DetailCardTypography
import androidx.core.graphics.toColorInt

@Composable
fun DetailCardCompose(
    data: DetailCardData,
    dimensions: DetailCardDimensions,
    colors: DetailCardColors,
    typography: DetailCardTypography,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .semantics { contentDescription = data.contentDescription },
        shape = RoundedCornerShape(dimensions.cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(colors.backgroundColor.toColorInt())
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = data.title,
            style = TextStyle(
                fontWeight = FontWeight(typography.titleStyle.fontWeight),
                fontSize = typography.titleStyle.fontSize.sp
            ),
            color = Color(colors.titleColor.toColorInt())
        )
        
        Text(
            text = data.subtitle,
            style = TextStyle(
                fontWeight = FontWeight(typography.subtitleStyle.fontWeight),
                fontSize = typography.subtitleStyle.fontSize.sp
            ),
            color = Color(colors.subtitleColor.toColorInt())
        )
        
        Text(
            text = data.statusText,
            style = TextStyle(
                fontWeight = FontWeight(typography.statusStyle.fontWeight),
                fontSize = typography.statusStyle.fontSize.sp
            ),
            color = Color(data.statusColor.toColorInt())
        )
    }
}
