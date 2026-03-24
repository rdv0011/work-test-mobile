package io.umain.munchies.android.ui.components

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.android.ui.toComposeColor
import androidx.compose.ui.tooling.preview.Preview
import io.umain.munchies.android.ui.theme.MunchiesTheme

@Composable
fun RestaurantDetailSkeleton(
    modifier: Modifier = Modifier
) {
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
    val shimmerValue = shimmerProgress.value

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Image placeholder
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shimmerProgress = shimmerValue
        )
        // Back button placeholder (circle)
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = (-180).dp)
                .size(40.dp)
                .background(
                    color = "#FFE0E0E0".toComposeColor(),
                    shape = CircleShape
                )
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            // Title placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(28.dp)
                    .offset(y = (-40).dp),
                shimmerProgress = shimmerValue
            )
            // Tags placeholder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp),
                        shimmerProgress = shimmerValue
                    )
                }
            }
            // Status placeholder
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(16.dp),
                shimmerProgress = shimmerValue
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Button placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(ButtonDefaults.MinHeight),
                shimmerProgress = shimmerValue
            )
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
private fun RestaurantDetailSkeletonPreview() {
    MunchiesTheme {
        RestaurantDetailSkeleton()
    }
}
