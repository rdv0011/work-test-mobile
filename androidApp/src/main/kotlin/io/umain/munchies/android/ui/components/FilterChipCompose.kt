package io.umain.munchies.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.presentation.model.FilterChipData
import io.umain.munchies.android.ui.toComposeColor
import androidx.compose.runtime.getValue

@Composable
fun FilterChipCompose(
    data: FilterChipData,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }

    val backgroundColor by animateColorAsState(
        if (data.isSelected) {
            DesignTokens.Colors.Accent.selected.toComposeColor()
        } else {
            DesignTokens.Colors.Background.filterDefault.toComposeColor()
        }
    )

    val textColor by animateColorAsState(
        if (data.isSelected) {
            DesignTokens.Colors.Text.light.toComposeColor()
        } else {
            DesignTokens.Colors.Text.dark.toComposeColor()
        }
    )

    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(data.isSelected) {
        if (data.isSelected) {
            scale.animateTo(
                1.18f,
                animationSpec = tween(90)
            )
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = 400f
                )
            )
        }
    }
    
    Row(
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(DesignTokens.BorderRadius.full.dp),
                clip = false
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(DesignTokens.BorderRadius.full.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null // Remove default ripple effect for a cleaner look
            ) { onSelect(!data.isSelected) }
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
                .scale(scale.value)
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