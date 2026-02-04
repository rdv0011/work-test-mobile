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
import io.umain.munchies.feature.restaurant.presentation.model.DetailCardData

@Composable
fun DetailCardCompose(
    data: DetailCardData,
    modifier: Modifier = Modifier
) {
    val titleStyle = DesignTokens.Typography.TextStyles.headline1
    val subtitleStyle = DesignTokens.Typography.TextStyles.headline2
    val statusStyle = DesignTokens.Typography.TextStyles.title1
    
    Card(
        modifier = modifier
            .semantics { contentDescription = data.contentDescription },
        shape = RoundedCornerShape(DesignTokens.BorderRadius.md.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(DesignTokens.Colors.Background.card.toColorInt())
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = data.title,
            style = TextStyle(
                fontWeight = FontWeight(titleStyle.fontWeight),
                fontSize = titleStyle.fontSize.sp
            ),
            color = Color(DesignTokens.Colors.Text.dark.toColorInt())
        )
        
        Text(
            text = data.subtitle,
            style = TextStyle(
                fontWeight = FontWeight(subtitleStyle.fontWeight),
                fontSize = subtitleStyle.fontSize.sp
            ),
            color = Color(DesignTokens.Colors.Text.subtitle.toColorInt())
        )
        
        Text(
            text = data.statusText,
            style = TextStyle(
                fontWeight = FontWeight(statusStyle.fontWeight),
                fontSize = statusStyle.fontSize.sp
            ),
            color = Color(data.statusColor.toColorInt())
        )
    }
}
