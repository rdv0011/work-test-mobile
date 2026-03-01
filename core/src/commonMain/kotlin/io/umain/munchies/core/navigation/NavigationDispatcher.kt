package io.umain.munchies.core.navigation

import io.umain.munchies.core.lifecycle.KmpViewModel
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.ModalDestination

/**
 * Generic navigation dispatcher that wraps AppCoordinator.
 *
 * This is the single point of abstraction for all navigation logic.
 * Feature modules create typed extension VMs that use this dispatcher.
 * Screens receive feature-scoped navigation VMs, never this dispatcher directly.
 */
class NavigationDispatcher(
    private val coordinator: AppCoordinator
) : KmpViewModel() {
    
    /**
     * Navigate to a typed destination (screen/route).
     *
     * @param destination The destination to navigate to (e.g., RestaurantDetail("123"))
     */
    fun navigate(destination: Destination) {
        coordinator.navigateToScreen(destination)
    }
    
    /**
     * Present a modal (overlay, sheet, dialog).
     *
     * @param modal The modal to present (e.g., Filter([...]), DatePicker(...))
     */
    fun presentModal(modal: ModalDestination) {
        coordinator.showModal(modal)
    }
    
    /**
     * Select a tab by ID.
     *
     * @param tabId The tab identifier (e.g., "restaurants", "settings")
     */
    fun selectTab(tabId: String) {
        coordinator.selectTab(tabId)
    }
}
