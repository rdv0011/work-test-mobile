import SwiftUI
import shared

class SettingsViewModelHolder {
    let viewModel: SettingsViewModel
    
    init(scope: Scope) {
        self.viewModel = scope.get()
    }
}
