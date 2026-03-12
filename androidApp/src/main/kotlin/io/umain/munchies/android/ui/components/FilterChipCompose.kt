package io.umain.munchies.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.presentation.model.FilterChipData

@Composable
fun FilterChipCompose(
    data: FilterChipData,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (data.isSelected) {
        Color(DesignTokens.Colors.Accent.selected.toColorInt())
    } else {
        Color.White
    }
    
    val textColor = if (data.isSelected) {
        Color(DesignTokens.Colors.Text.light.toColorInt())
    } else {
        Color(DesignTokens.Colors.Text.dark.toColorInt())
    }
    
    Row(
        modifier = modifier
            .height(48.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(DesignTokens.BorderRadius.full.dp)
            )
            .clickable { onSelect(!data.isSelected) }
            .semantics {
                contentDescription = data.contentDescription
                toggleableState = if (data.isSelected) ToggleableState.On else ToggleableState.Off
            },
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = data.iconUrl,
            contentDescription = "${data.label} icon",
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )
        
        Text(
            text = data.label,
            style = TextStyle(
                fontWeight = FontWeight(500),
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = textColor,
            modifier = Modifier.padding(end = DesignTokens.Spacing.lg.dp)
        )
    }
}