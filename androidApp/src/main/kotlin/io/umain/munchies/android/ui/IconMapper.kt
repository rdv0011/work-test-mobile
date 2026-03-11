package io.umain.munchies.android.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import io.umain.munchies.android.R
import io.umain.munchies.designtokens.DesignTokens

object IconMapper {
    
    @Composable
    fun getIconPainter(iconResourceName: String): Painter {
        val resourceId = iconResourceName.toIconResourceId()
        return painterResource(id = resourceId)
    }
    
    fun String.toIconResourceId(): Int {
        return when (this) {
            DesignTokens.Icons.Resources.clock -> R.drawable.ic_clock_icon
            DesignTokens.Icons.Resources.star -> R.drawable.ic_star_icon
            DesignTokens.Icons.Resources.logo -> R.drawable.ic_logo
            else -> throw IllegalArgumentException("Unknown icon resource: $this")
        }
    }
}

@Composable
fun IconCompose(
    iconResourceName: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: String? = null
) {
    val painter = IconMapper.getIconPainter(iconResourceName)
    val tintColor = tint?.toComposeColor() ?: DesignTokens.Colors.Text.dark.toComposeColor()
    
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
    )
}
