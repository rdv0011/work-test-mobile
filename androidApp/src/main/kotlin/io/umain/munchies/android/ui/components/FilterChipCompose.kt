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
import io.umain.munchies.ui.FilterChipColors
import io.umain.munchies.ui.FilterChipData
import io.umain.munchies.ui.FilterChipDimensions
import io.umain.munchies.ui.FilterChipTypography
import androidx.core.graphics.toColorInt

@Composable
fun FilterChipCompose(
    data: FilterChipData,
    dimensions: FilterChipDimensions,
    colors: FilterChipColors,
    typography: FilterChipTypography,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (data.isSelected) {
        Color(colors.selectedBackground.toColorInt())
    } else {
        Color(colors.unselectedBackground.toColorInt())
    }
    
    val textColor = if (data.isSelected) {
        Color(colors.selectedTextColor.toColorInt())
    } else {
        Color(colors.unselectedTextColor.toColorInt())
    }
    
    Text(
        text = data.label,
        style = TextStyle(
            fontWeight = FontWeight(typography.labelStyle.fontWeight),
            fontSize = typography.labelStyle.fontSize.sp
        ),
        color = textColor,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(dimensions.cornerRadius.dp)
            )
            .clickable { onSelect(!data.isSelected) }
            .semantics {
                contentDescription = data.contentDescription
                toggleableState = if (data.isSelected) ToggleableState.On else ToggleableState.Off
            }
    )
}
