package io.umain.munchies.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import io.umain.munchies.android.ui.theme.MunchiesTheme
import io.umain.munchies.android.ui.toComposeTextStyle
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.feature.restaurant.presentation.model.DetailCardData

@Composable
fun DetailCardCompose(
    data: DetailCardData,
    modifier: Modifier = Modifier
) {
    val titleStyle = DesignTokens.Typography.TextStyles.headline1
    val subtitleStyle = DesignTokens.Typography.TextStyles.headline2
    val statusStyle = DesignTokens.Typography.TextStyles.title1
    val background = Color(0xFFF8F8F8)
    val textPrimary = Color(0xFF1F2B2E)
    val textMuted = Color(0xFF999999)

    Surface(
        color = background,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), clip = false)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = data.title,
                color = textPrimary,
                style = titleStyle.toComposeTextStyle()
            )
            Text(
                text = data.tags.joinToString(" • "),
                color = textMuted,
                style = subtitleStyle.toComposeTextStyle()
            )
            Text(
                text = data.statusText,
                color = Color(data.statusColor.toColorInt()),
                style = statusStyle.toComposeTextStyle()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailCardComposePreview() {
    MunchiesTheme {
        DetailCardCompose(
            data = DetailCardData(
                title = "Burger King",
                tags = listOf("sports", "burgers", "fast food"),
                statusText = "Open Now",
                statusColor = "#2ECC71"
            )
        )
    }
}
