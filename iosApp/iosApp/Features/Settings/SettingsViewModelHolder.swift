import SwiftUI
import shared
import os.log

private let logger = Logger(subsystem: "com.munchies.ios", category: "SettingsViewModelHolder")

class SettingsViewModelHolder {
    let viewModel: SettingsViewModel
    let scope: Koin_coreScope
    
    init(scope: Koin_coreScope) {
        self.scope = scope
        self.viewModel = IosAggregatorExportsKt.getSettingsViewModelFromFramework()
        logger.debug("[SettingsViewModelHolder] init() called")
    }
    
    deinit {
        // CRITICAL: Do NOT close the scope here.
        // RouteRegistry is responsible for closing scopes.
        // This holder just retains the ViewModel during view lifecycle.
        logger.debug("[SettingsViewModelHolder] deinit() called - about to close viewModel")
        viewModel.close()
        logger.debug("[SettingsViewModelHolder] deinit() completed - viewModel closed")
    }
}
