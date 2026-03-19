package io.umain.munchies.android.ui.components

import android.graphics.Color
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.android.ui.toComposeColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.toColor

/**
 * Skeleton loader for restaurant card during loading state.
 * 
 * Mimics the structure of RestaurantCardCompose with animated shimmer effect.
 * Uses design tokens for consistent sizing and spacing.
 */
@Composable
fun RestaurantCardSkeleton(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = DesignTokens.BorderRadius.md.dp,
        topEnd = DesignTokens.BorderRadius.md.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
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
        label = "shimmerProgress"
    )
    
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.Colors.Background.card.toComposeColor()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                shimmerProgress = shimmerProgress.value
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.sm.dp),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xl.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs.dp)
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(16.dp),
                        shimmerProgress = shimmerProgress.value
                    )
                    
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp),
                        shimmerProgress = shimmerProgress.value
                    )
                    
                    ShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(10.dp)
                            .padding(top = 4.dp),
                        shimmerProgress = shimmerProgress.value
                    )
                }
                
                ShimmerBox(
                    modifier = Modifier
                        .width(30.dp)
                        .height(16.dp),
                    shimmerProgress = shimmerProgress.value
                )
            }
        }
    }
}

@Composable
private fun ShimmerBox(
    modifier: Modifier = Modifier,
    shimmerProgress: Float
) {
    val baseColor = "#FFE0E0E0".toComposeColor()
    val highlight = "#FFF0F0F0".toComposeColor()
    val shimmerWidth = 300f
    val start = shimmerProgress * 1000f - shimmerWidth
    val end = shimmerProgress * 1000f

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlight,
            baseColor
        ),
        start = Offset(start, start),
        end = Offset(end, end)
    )

    Box(
        modifier = modifier
            .background(baseColor)
            .background(shimmerBrush)
    )
}

@Preview(showBackground = true)
@Composable
private fun RestaurantCardSkeletonPreview() {
    MunchiesTheme {
        RestaurantCardSkeleton()
    }
}
