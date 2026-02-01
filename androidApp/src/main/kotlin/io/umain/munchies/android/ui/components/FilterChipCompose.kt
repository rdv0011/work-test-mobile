package io.umain.munchies.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.umain.munchies.designtokens.DesignTokens

data class FilterChipData(
    val id: String,
    val label: String,
    val iconUrl: String,
    val isSelected: Boolean = false,
    val contentDescription: String = "Filter: $label"
)

@Composable
fun FilterChipCompose(
    data: FilterChipData,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (data.isSelected) {
        Color(DesignTokens.Colors.Accent.selected.toColorInt())
    } else {
        Color(DesignTokens.Colors.Background.filterDefault.toColorInt())
    }
    
    val textColor = if (data.isSelected) {
        Color(DesignTokens.Colors.Text.light.toColorInt())
    } else {
        Color(DesignTokens.Colors.Text.picto.toColorInt())
    }
    
    val textStyle = DesignTokens.Typography.TextStyles.title2
    
    Text(
        text = data.label,
        style = TextStyle(
            fontWeight = FontWeight(textStyle.fontWeight),
            fontSize = textStyle.fontSize.sp
        ),
        color = textColor,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(DesignTokens.BorderRadius.full.dp)
            )
            .clickable { onSelect(!data.isSelected) }
            .semantics {
                contentDescription = data.contentDescription
                toggleableState = if (data.isSelected) ToggleableState.On else ToggleableState.Off
            }
    )
}
