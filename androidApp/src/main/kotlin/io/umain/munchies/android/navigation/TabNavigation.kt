package io.umain.munchies.android.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.umain.munchies.core.ui.IconId
import io.umain.munchies.core.localization.stringResource
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.android.ui.toComposeColor
import io.umain.munchies.android.ui.toComposeTextStyle
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.TabNavigationState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.with
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.umain.munchies.android.navigation.ScreenTransitionAnimations

/**
 * Tab navigation scaffold that shows a bottom navigation bar
 * and renders the appropriate screen content based on the active tab.
 *
 * The screen content fades into the bottom bar via a lightweight gradient
 * strip drawn between the content area and the navigation bar, creating a
 * smooth visual transition instead of a sharp edge.
 *
 * Styled to match the app's clean, light aesthetic from the Figma designs:
 * - White bottom bar on #F8F8F8 background
 * - Accent-colored selected indicator
 * - Design-token-driven colors and typography
 */
@Composable
fun TabNavigationScaffold(
    tabNavigationState: TabNavigationState,
    coordinator: AppCoordinator,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cardColor = DesignTokens.Colors.Background.card.toComposeColor()
    val selectedColor = DesignTokens.Colors.Accent.selected.toComposeColor()
    val unselectedColor = DesignTokens.Colors.Text.subtitle.toComposeColor()
    val darkTextColor = DesignTokens.Colors.Text.dark.toComposeColor()

    Column(modifier = modifier.fillMaxSize()) {
        // Main content — takes all remaining vertical space
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
    val activeTab = tabNavigationState.activeTabId
    AnimatedContent(
        targetState = activeTab,
        transitionSpec = { ScreenTransitionAnimations.fadeOnlyTransition },
        modifier = Modifier.fillMaxSize()
    ) { targetTabId ->
        content()
    }

    // Gradient overlay at the bottom of the content area,
    // fading from transparent to the bar colour
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(4.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, cardColor)
                )
            )
    )
}

        // Bottom navigation bar
        NavigationBar(
            containerColor = cardColor,
            tonalElevation = 0.dp
        ) {
            tabNavigationState.tabDefinitions.forEach { tabDef ->
                val isSelected = tabDef.id == tabNavigationState.activeTabId
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        coordinator.selectTab(tabDef.id)
                    },
                    icon = {
                        Icon(
                            imageVector = when (tabDef.icon) {
                                IconId.Restaurant -> Icons.Filled.Home
                                IconId.Settings -> Icons.Filled.Settings
                                else -> Icons.Filled.Home
                            },
                            contentDescription = stringResource(tabDef.label)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tabDef.label),
                            style = DesignTokens.Typography.TextStyles.footer1.toComposeTextStyle()
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = darkTextColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = selectedColor.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}
