package io.umain.munchies.android.ui

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

/**
 * Convert hex color strings to Compose Color objects.
 *
 * Enables clean usage of design token colors:
 * ```
 * Text(
 *     text = "Hello",
 *     color = DesignTokens.Colors.Text.dark.toComposeColor()
 * )
 * ```
 *
 * See: figma/figma_css_normalization_pipeline.md - "Android Implementation" section
 */
fun String.toComposeColor(): Color = Color(this.toColorInt())
