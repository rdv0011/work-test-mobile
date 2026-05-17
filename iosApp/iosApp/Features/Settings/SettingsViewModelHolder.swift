import SwiftUI
import shared

class SettingsViewModelHolder {
    let viewModel: Feature_settingsSettingsViewModel
    let navigationViewModel: Feature_settingsSettingsNavigationViewModel
    let scope: Koin_coreScope
    
    init(scope: Koin_coreScope) {
        self.scope = scope
        self.viewModel = scope.getFeature_settingsSettingsViewModel()
        self.navigationViewModel = scope.getFeature_settingsSettingsNavigationViewModel()
    }
    
    deinit {
        // CRITICAL: Do NOT close the scope here.
        // RouteRegistry is responsible for closing scopes.
        // This holder just retains the ViewModel during view lifecycle.
        viewModel.close()
    }
}
