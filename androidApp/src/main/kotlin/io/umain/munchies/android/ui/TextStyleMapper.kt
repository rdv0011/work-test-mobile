package io.umain.munchies.android.ui

import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.umain.munchies.designtokens.DesignTokens
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable

@Composable
fun DesignTokens.Typography.TextStyle.toComposeTextStyle(): ComposeTextStyle =
    ComposeTextStyle(
        fontFamily = resolveFontFamily(fontFamily),
        fontWeight = FontWeight(fontWeight),
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp
    )

@Composable
private fun resolveFontFamily(fontFamily: String): FontFamily =
    with(typography) {
        when (fontFamily) {
            DesignTokens.Typography.FontFamilies.helvetica -> bodyLarge.fontFamily
            DesignTokens.Typography.FontFamilies.poppins -> titleMedium.fontFamily
            DesignTokens.Typography.FontFamilies.inter -> bodyMedium.fontFamily
            else -> null
        }
    } ?: FontFamily.Default