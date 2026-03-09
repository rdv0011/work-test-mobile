//
//  SettingsView.swift
//  iosApp
//
import SwiftUI
import shared

struct SettingsView: View {
    let navigationViewModel: SettingsNavigationViewModel
    let viewModel: SettingsViewModel

    @State private var uiState: SettingsUiState = SettingsUiState(
        darkModeEnabled: false,
        notificationsEnabled: false,
        appVersion: ""
    )
    
    private let R = StringResources.shared

    var body: some View {
        VStack(spacing: .zero) {
            ScrollView {
                VStack(alignment: .leading, spacing: .spacingUI.lg) {
                    Text(stringResource(key: R.settings_title))
                        .font(.headline)
                        .padding(.horizontal, .spacingUI.lg)

                    VStack(spacing: .spacingUI.md) {
                        Toggle(stringResource(key: R.settings_dark_mode), isOn: Binding(
                            get: { uiState.darkModeEnabled },
                            set: { _ in viewModel.toggleDarkMode() }
                        ))

                        Toggle(stringResource(key: R.settings_notifications), isOn: Binding(
                            get: { uiState.notificationsEnabled },
                            set: { _ in viewModel.toggleNotifications() }
                        ))

                        HStack {
                            Text(stringResource(key: R.settings_about))
                            Spacer()
                            Text(uiState.appVersion)
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.horizontal, .spacingUI.lg)
                }
            }
        }
        .navigationTitle(stringResource(key: R.settings_title))
        .task(id: viewModel) {
            await observe()
        }
    }

    @MainActor
    private func observe() async {
        for await state in asyncStateStream(viewModel) as AsyncStream<SettingsUiState> {
            uiState = state
        }
    }
}
