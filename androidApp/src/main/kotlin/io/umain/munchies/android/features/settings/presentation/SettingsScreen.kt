package io.umain.munchies.android.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.umain.munchies.feature.settings.presentation.SettingsViewModel
import io.umain.munchies.localization.tr
import io.umain.munchies.core.ui.TextId
import io.umain.munchies.designtokens.DesignTokens
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.android.navigation.LocalRouteRegistry
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.feature.settings.navigation.SettingsNavigationViewModel

@Composable
fun SettingsScreen(
    navigationViewModel: SettingsNavigationViewModel,
    modifier: Modifier = Modifier
) {
    val registry = LocalRouteRegistry.current
    val route = remember { SettingsRoute() }

    val viewModel = remember {
        val scope = registry.createScopeForRoute(route)
        scope.get<SettingsViewModel>()
    }
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.lg.dp)
        ) {
            Text(
                text = tr(TextId.SettingsTitle),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Spacing.md.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(TextId.DarkMode),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = uiState.darkModeEnabled,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Spacing.md.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(TextId.Notifications),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications() }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Spacing.md.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(TextId.About),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = uiState.appVersion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
