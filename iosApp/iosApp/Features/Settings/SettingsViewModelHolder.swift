import SwiftUI
import shared

class SettingsViewModelHolder {
    let viewModel: SettingsViewModel
    let scope: Scope
    
    init(scope: Scope) {
        self.scope = scope
        self.viewModel = scope.getSettingsViewModel()
    }
    
    deinit {
        // CRITICAL: Do NOT close the scope here.
        // RouteRegistry is responsible for closing scopes.
        // This holder just retains the ViewModel during view lifecycle.
        viewModel.close()
    }
}
