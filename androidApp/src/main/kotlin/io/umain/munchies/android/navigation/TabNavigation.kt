package io.umain.munchies.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.umain.munchies.core.ui.IconId
import io.umain.munchies.core.localization.stringResource
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.TabNavigationState

/**
 * Tab navigation scaffold that shows a bottom navigation bar
 * and renders the appropriate screen content based on the active tab.
 */
@Composable
fun TabNavigationScaffold(
    tabNavigationState: TabNavigationState,
    coordinator: AppCoordinator,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            content()
        }

        // Bottom tab navigation bar
        NavigationBar {
            tabNavigationState.tabDefinitions.forEach { tabDef ->
                NavigationBarItem(
                    selected = tabDef.id == tabNavigationState.activeTabId,
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
                        Text(text = stringResource(tabDef.label))
                    }
                )
            }
        }
    }
}
