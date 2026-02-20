//
//  SettingsView.swift
//  iosApp
//
import SwiftUI
import shared

struct SettingsView: View {
    let viewModel: SettingsViewModel

    @State private var uiState: SettingsUiState = SettingsUiState(
        darkModeEnabled: false,
        notificationsEnabled: false,
        appVersion: ""
    )

    var body: some View {
        VStack(spacing: .zero) {
            ScrollView {
                VStack(alignment: .leading, spacing: .spacingUI.lg) {
                    Text(tr(.settingsTitle))
                        .font(.headline)
                        .padding(.horizontal, .spacingUI.lg)

                    VStack(spacing: .spacingUI.md) {
                        Toggle(tr(.darkMode), isOn: Binding(
                            get: { uiState.darkModeEnabled },
                            set: { _ in viewModel.toggleDarkMode() }
                        ))

                        Toggle(tr(.notifications), isOn: Binding(
                            get: { uiState.notificationsEnabled },
                            set: { _ in viewModel.toggleNotifications() }
                        ))

                        HStack {
                            Text(tr(.about))
                            Spacer()
                            Text(uiState.appVersion)
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.horizontal, .spacingUI.lg)
                }
            }
        }
        .navigationTitle(tr(.settingsTitle))
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
