package io.umain.munchies.android.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.android.ui.toComposeColor

@Composable
fun FilterChipSkeleton(
    modifier: Modifier = Modifier
) {
    // Shimmer animation setup (mirrored from RestaurantCardSkeleton)
    val infiniteTransition = rememberInfiniteTransition(label = "chip_shimmer")
    val shimmerProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "chipShimmerProgress"
    )

    // Colors like RestaurantCardSkeleton
    val baseColor = "#FFE0E0E0".toComposeColor()
    val highlight = "#FFF0F0F0".toComposeColor()
    val shimmerWidth = 160f
    val start = shimmerProgress.value * 300f - shimmerWidth
    val end = shimmerProgress.value * 300f

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlight, baseColor),
        start = Offset(start, 0f),
        end = Offset(end, DesignTokens.Sizes.Filter.height.toFloat())
    )

    Row(
        modifier = modifier
            .height(DesignTokens.Sizes.Filter.height.dp)
            .width(DesignTokens.Sizes.Filter.width.dp)
            .background(
                brush = shimmerBrush,
                shape = RoundedCornerShape(DesignTokens.BorderRadius.full.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm.dp)
    ) {
        // Icon placeholder (circle)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .background(
                    color = baseColor,
                    shape = CircleShape
                )
        )
        // Text placeholder (rounded rectangle)
        Box(
            modifier = Modifier
                .height(16.dp)
                .width(48.dp)
                .background(
                    color = baseColor,
                    shape = RoundedCornerShape(DesignTokens.BorderRadius.sm.dp)
                )
                .padding(end = DesignTokens.Spacing.lg.dp)
        )
    }
}
