//
//  SettingsView.swift
//  iosApp
//
import SwiftUI
import shared
import os.log

private let logger = Logger(subsystem: "com.munchies.ios", category: "SettingsView")

struct SettingsView: View {
    let viewModel: SettingsViewModel

    @State private var uiState: Any = [:]

    var body: some View {
        VStack(spacing: .zero) {
            ScrollView {
                VStack(alignment: .leading, spacing: .spacingUI.lg) {
                    Text(stringResource(key: "settings_title"))
                        .font(.headline)
                        .padding(.horizontal, .spacingUI.lg)

                    VStack(spacing: .spacingUI.md) {
                        Toggle(stringResource(key: "settings_dark_mode"), isOn: Binding(
                            get: { darkModeEnabled() },
                            set: { _ in viewModel.toggleDarkMode() }
                        ))

                        Toggle(stringResource(key: "settings_notifications"), isOn: Binding(
                            get: { notificationsEnabled() },
                            set: { _ in viewModel.toggleNotifications() }
                        ))

                        HStack {
                            Text(stringResource(key: "settings_about"))
                            Spacer()
                            Text(appVersion())
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.horizontal, .spacingUI.lg)
                }
            }
        }
        .navigationTitle(stringResource(key: "settings_title"))
        .task(id: viewModel) {
            await observe()
        }
    }

    private func darkModeEnabled() -> Bool {
        if let darkMode = (uiState as AnyObject).value(forKey: "darkModeEnabled") as? Bool {
            return darkMode
        }
        return false
    }

    private func notificationsEnabled() -> Bool {
        if let notifications = (uiState as AnyObject).value(forKey: "notificationsEnabled") as? Bool {
            return notifications
        }
        return true
    }

    private func appVersion() -> String {
        if let version = (uiState as AnyObject).value(forKey: "appVersion") as? String {
            return version
        }
        return "1.0.0"
    }

    @MainActor
    private func observe() async {
        logger.debug("observe: Starting to observe StateFlow")
        var count = 0
        let stream: AsyncStream<Any> = asyncStateStream(viewModel)
        for await state in stream {
            count += 1
            logger.debug("observe: ✓ Received state update #\(count)")
            logger.debug("observe: state type: \(String(describing: type(of: state)))")
            logger.debug("observe: uiState before update: \(String(describing: self.uiState))")
            uiState = state
            logger.debug("observe: uiState after update: \(String(describing: self.uiState))")
        }
        logger.debug("observe: AsyncStream completed (unsubscribed)")
    }
}

