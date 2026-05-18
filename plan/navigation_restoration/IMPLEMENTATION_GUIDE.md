# KMM Navigation Restoration: Implementation Guide

**Document**: Step-by-step implementation guide for recommended improvements  
**Audience**: Backend engineers, navigation system maintainers  
**Date**: May 18, 2026

---

## Quick Start: Implementing Priority 1 Improvements

### 1. Enable `restoredFromCrash` Flag

**File**: `core/src/commonMain/.../navigation/persistence/NavigationPersistenceStore.kt`

**Current State**:
```kotlin
interface NavigationPersistenceStore {
    suspend fun saveNavigationState(snapshot: NavigationStateSnapshot): Result<Unit>
    suspend fun loadNavigationState(): Result<NavigationStateSnapshot?>
}
```

**Add to Interface**:
```kotlin
interface NavigationPersistenceStore {
    suspend fun saveNavigationState(snapshot: NavigationStateSnapshot): Result<Unit>
    suspend fun loadNavigationState(): Result<NavigationStateSnapshot?>
    
    // NEW: Track clean shutdown
    suspend fun markCleanShutdown(): Result<Unit>
    suspend fun hasUncleanShutdown(): Result<Boolean>
    suspend fun clearCrashIndicator(): Result<Unit>
}
```

**Android Implementation** (`androidApp/.../persistence/AndroidNavigationStore.kt`):
```kotlin
class AndroidNavigationStore(
    private val dataStore: DataStore<Preferences>
) : NavigationPersistenceStore {
    
    companion object {
        private val CLEAN_SHUTDOWN_KEY = booleanPreferencesKey("nav_clean_shutdown")
    }
    
    override suspend fun markCleanShutdown(): Result<Unit> = runCatching {
        dataStore.edit { prefs ->
            prefs[CLEAN_SHUTDOWN_KEY] = true
        }
    }
    
    override suspend fun hasUncleanShutdown(): Result<Boolean> = runCatching {
        dataStore.data.first().let { prefs ->
            !(prefs[CLEAN_SHUTDOWN_KEY] ?: false)
        }
    }
    
    override suspend fun clearCrashIndicator(): Result<Unit> = runCatching {
        dataStore.edit { prefs ->
            prefs[CLEAN_SHUTDOWN_KEY] = false
        }
    }
}
```

**iOS Implementation** (`core/src/iosMain/.../persistence/IosUserDefaultsPersistence.kt`):
```kotlin
class IosUserDefaultsPersistence : NavigationPersistenceStore {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val CLEAN_SHUTDOWN_KEY = "nav_clean_shutdown"
    
    override suspend fun markCleanShutdown(): Result<Unit> = runCatching {
        userDefaults.setBool(true, CLEAN_SHUTDOWN_KEY)
        userDefaults.synchronize()
    }
    
    override suspend fun hasUncleanShutdown(): Result<Boolean> = runCatching {
        !userDefaults.boolForKey(CLEAN_SHUTDOWN_KEY)
    }
    
    override suspend fun clearCrashIndicator(): Result<Unit> = runCatching {
        userDefaults.removeObjectForKey(CLEAN_SHUTDOWN_KEY)
        userDefaults.synchronize()
    }
}
```

**Update NavigationStateRestorer**:
```kotlin
class NavigationStateRestorer(
    private val persistenceStore: NavigationPersistenceStore
) {

    suspend fun restoreNavigationState(): NavigationState {
        return try {
            // Check if we're recovering from a crash
            val hadCrash = persistenceStore.hasUncleanShutdown()
                .getOrElse { false }
            
            val result = persistenceStore.loadNavigationState()
            if (result.isSuccess) {
                val snapshot = result.getOrNull()
                when {
                    snapshot == null -> {
                        logInfo("NavigationStateRestorer", "No persisted state found")
                        createDefaultNavigationState()
                    }
                    isValidSnapshot(snapshot) -> {
                        logInfo("NavigationStateRestorer", "Restored state (crash=$hadCrash)")
                        val restoredState = snapshot.toNavigationState()
                        
                        // Add crash metadata
                        if (hadCrash) {
                            persistenceStore.clearCrashIndicator()
                        }
                        
                        restoredState
                    }
                    else -> {
                        logInfo("NavigationStateRestorer", "Snapshot invalid, using default")
                        createDefaultNavigationState()
                    }
                }
            } else {
                logError("NavigationStateRestorer", 
                    "Failed to load: ${result.exceptionOrNull()?.message}")
                createDefaultNavigationState()
            }
        } catch (e: Exception) {
            logError("NavigationStateRestorer", 
                "Unexpected error: ${e.message}")
            createDefaultNavigationState()
        }
    }

    private fun isValidSnapshot(snapshot: NavigationStateSnapshot): Boolean {
        val tabNav = snapshot.tabNavigation
        return tabNav.tabDefinitions.isNotEmpty() &&
            tabNav.tabDefinitions.any { it.id == tabNav.activeTabId } &&
            tabNav.stacksByTab.values.all { it.isNotEmpty() }
    }
}
```

**Usage in AppCoordinator**:
```kotlin
// On app lifecycle ready
suspend fun onAppInitialized() {
    val restorer = NavigationStateRestorer(persistenceStore)
    val restoredState = restorer.restoreNavigationState()
    applyNavigationState(restoredState)
}

// On app going to background (Android, iOS app delegate)
suspend fun onAppGoingToBackground() {
    persistenceStore?.markCleanShutdown()
}
```

---

### 2. Add Modal Restoration Test

**File**: `core/src/commonTest/.../navigation/ModalRestorationTest.kt`

```kotlin
class ModalRestorationTest {
    
    private lateinit var persistenceStore: NavigationPersistenceStore
    private lateinit var restorer: NavigationStateRestorer
    
    @Before
    fun setup() {
        // Use in-memory test store
        persistenceStore = InMemoryNavigationPersistenceStore()
        restorer = NavigationStateRestorer(persistenceStore)
    }
    
    @Test
    fun testModalStackRestoredAfterCrash() {
        // 1. Create state with modal stack
        val restaurantListRoute = RestaurantListRoute()
        val filterModalRoute = FilterModalRoute(preSelectedFilters = listOf("vegan"))
        
        val originalState = NavigationState(
            tabNavigation = TabNavigationState(
                tabDefinitions = listOf(
                    TabDefinition(
                        id = "restaurants",
                        label = "Restaurants",
                        icon = IconId.Restaurant,
                        rootRoute = restaurantListRoute
                    )
                ),
                activeTabId = "restaurants",
                stacksByTab = mapOf("restaurants" to listOf(restaurantListRoute)),
                navigationDirection = NavigationDirection.Forward
            ),
            modalStack = listOf(filterModalRoute)
        )
        
        // 2. Save state (simulate app session)
        val snapshot = originalState.toSnapshot(restoredFromCrash = false)
        persistenceStore.saveNavigationState(snapshot)
        
        // 3. Mark as unclean shutdown (simulate crash)
        persistenceStore.hasUncleanShutdown() // Returns true
        
        // 4. Restore state (simulate app restart)
        val restoredState = restorer.restoreNavigationState()
        
        // 5. Assertions
        assertEquals(originalState.modalStack.size, restoredState.modalStack.size)
        assertEquals(1, restoredState.modalStack.size)
        
        val restoredModal = restoredState.modalStack[0]
        assertTrue(restoredModal is FilterModalRoute)
        assertEquals(
            (restoredModal as FilterModalRoute).preSelectedFilters,
            listOf("vegan")
        )
    }
    
    @Test
    fun testMultipleModalsRestoredInOrder() {
        // Create state with multiple modals stacked
        val filters = FilterModalRoute(listOf("vegetarian"))
        val confirmDialog = ConfirmActionModalRoute(
            message = "Delete restaurant?",
            confirmText = "Delete",
            cancelText = "Cancel"
        )
        
        val state = NavigationState(
            tabNavigation = TabNavigationState(
                tabDefinitions = listOf(/* ... */),
                activeTabId = "restaurants",
                stacksByTab = mapOf(/* ... */),
                navigationDirection = NavigationDirection.Forward
            ),
            modalStack = listOf(filters, confirmDialog)
        )
        
        // Save and restore
        val snapshot = state.toSnapshot()
        persistenceStore.saveNavigationState(snapshot)
        val restored = restorer.restoreNavigationState()
        
        // Assert order is preserved (FIFO)
        assertEquals(2, restored.modalStack.size)
        assertTrue(restored.modalStack[0] is FilterModalRoute)
        assertTrue(restored.modalStack[1] is ConfirmActionModalRoute)
    }
    
    @Test
    fun testModalRestorationWithTabStack() {
        // Ensure modals are restored independently of tab stack
        val tabStack = listOf(
            RestaurantListRoute(),
            RestaurantDetailRoute(restaurantId = "123")
        )
        
        val modal = SubmitReviewModalRoute(restaurantId = "123")
        
        val state = NavigationState(
            tabNavigation = TabNavigationState(
                tabDefinitions = listOf(/* ... */),
                activeTabId = "restaurants",
                stacksByTab = mapOf("restaurants" to tabStack),
                navigationDirection = NavigationDirection.Forward
            ),
            modalStack = listOf(modal)
        )
        
        // Save and restore
        persistenceStore.saveNavigationState(state.toSnapshot())
        val restored = restorer.restoreNavigationState()
        
        // Both tab stack and modal should be restored
        assertEquals(2, restored.currentStack.size)
        assertEquals(1, restored.modalStack.size)
        assertTrue(restored.topModal is SubmitReviewModalRoute)
    }
}

/**
 * In-memory implementation for testing
 */
class InMemoryNavigationPersistenceStore : NavigationPersistenceStore {
    
    private var snapshot: NavigationStateSnapshot? = null
    private var isUnclean = false
    
    override suspend fun saveNavigationState(
        snapshot: NavigationStateSnapshot
    ) = Result.success(Unit.also {
        this.snapshot = snapshot
        isUnclean = true  // Assume crash until markCleanShutdown() called
    })
    
    override suspend fun loadNavigationState() = 
        Result.success(snapshot)
    
    override suspend fun clearNavigationState() = 
        Result.success(Unit.also { snapshot = null })
    
    override suspend fun hasPersistedState() = 
        Result.success(snapshot != null)
    
    override suspend fun markCleanShutdown() = 
        Result.success(Unit.also { isUnclean = false })
    
    override suspend fun hasUncleanShutdown() = 
        Result.success(isUnclean)
    
    override suspend fun clearCrashIndicator() = 
        Result.success(Unit.also { isUnclean = false })
}
```

---

### 3. Document Deep Link State Application Strategy

**File**: `core/src/commonMain/.../navigation/DeepLinkStrategy.kt`

```kotlin
/**
 * Strategy for applying deep link navigation states.
 *
 * When a deep link arrives, we must decide:
 * 1. Should we clear existing navigation stack?
 * 2. Should we navigate within current tab or switch tabs?
 * 3. What happens to modals?
 */
sealed class DeepLinkApplicationStrategy {
    
    /**
     * FULL_REPLACE: Clear entire app navigation, start fresh from deep link
     *
     * Use case: External deep links (push notifications, browser links)
     * Behavior: All tabs reset, active tab determined by deep link
     * Modals: Dismissed
     *
     * Example: Notification tap while browsing → Go to detail screen
     */
    object FullReplace : DeepLinkApplicationStrategy()
    
    /**
     * PRESERVE_TAB: Keep current active tab stack, but apply deep link within tab
     *
     * Use case: App-to-app deep links during active session
     * Behavior: Current tab stack preserved, new route appended to active tab
     * Modals: Dismissed (new navigation takes precedence)
     *
     * Example: Deep link to restaurant detail while on restaurants tab
     *         → Append detail to existing tab stack, user can navigate back
     */
    object PreserveTabStack : DeepLinkApplicationStrategy()
    
    /**
     * APPEND_TO_TAB: Append deep link destination to current tab stack
     *
     * Use case: Contextual deep links within current session
     * Behavior: Switch to linked tab, append route to that tab's stack
     * Modals: Dismissed
     *
     * Example: Link to another tab's route → Switch to that tab, user can navigate back
     */
    object AppendToTab : DeepLinkApplicationStrategy()
    
    /**
     * MODAL_OVERLAY: Show deep link as modal on top of current state
     *
     * Use case: Modal deep links (e.g., deep link to a dialog)
     * Behavior: Current navigation untouched, route shown as modal
     * Modals: Stacked on top
     *
     * Example: Deep link to feedback form → Show as modal, user can dismiss
     */
    object ModalOverlay : DeepLinkApplicationStrategy()
}

/**
 * Deep link result with strategy metadata
 */
data class DeepLinkResult(
    val navigationState: NavigationState,
    val strategy: DeepLinkApplicationStrategy,
    val isValid: Boolean = true,
    val error: String? = null
)

/**
 * Base interface for deep link handlers with strategy support
 */
interface DeepLinkHandler {
    
    fun canHandle(deepLink: String): Boolean
    
    /**
     * Parse deep link with explicit strategy
     */
    fun parseDeepLink(deepLink: String): DeepLinkResult
    
    // Default: Use FullReplace (safest for external links)
}
```

**Update AppCoordinator to use strategy**:
```kotlin
open class AppCoordinator(
    // ... existing params
) {
    
    fun applyDeepLink(deepLink: String) {
        val parser = DeepLinkParser(routeHandlers.filterIsInstance<DeepLinkHandler>())
        val result = parser.parse(deepLink)
        
        if (!result.isValid) {
            logInfo("AppCoordinator", "Deep link invalid: $deepLink")
            return
        }
        
        val currentState = _navigationState.value
        val newState = when (result.strategy) {
            DeepLinkApplicationStrategy.FullReplace -> {
                // Completely replace navigation
                result.navigationState.copy(modalStack = emptyList())
            }
            DeepLinkApplicationStrategy.PreserveTabStack -> {
                // Keep current tab stack, switch tab if needed
                val currentTabStack = currentState.tabNavigation.getActiveTabStack()
                result.navigationState.copy(
                    tabNavigation = result.navigationState.tabNavigation.copy(
                        stacksByTab = result.navigationState.tabNavigation.stacksByTab.toMutableMap().apply {
                            this[currentState.tabNavigation.activeTabId] = currentTabStack
                        }
                    ),
                    modalStack = emptyList()
                )
            }
            DeepLinkApplicationStrategy.AppendToTab -> {
                // Append to target tab's existing stack
                val targetTabId = result.navigationState.tabNavigation.activeTabId
                val targetStack = result.navigationState.tabNavigation.stacksByTab[targetTabId] ?: emptyList()
                val currentTabStack = currentState.tabNavigation.getActiveTabStack()
                
                result.navigationState.copy(
                    tabNavigation = result.navigationState.tabNavigation.copy(
                        stacksByTab = result.navigationState.tabNavigation.stacksByTab.toMutableMap().apply {
                            this[targetTabId] = currentTabStack + targetStack
                        }
                    ),
                    modalStack = emptyList()
                )
            }
            DeepLinkApplicationStrategy.ModalOverlay -> {
                // Show as modal on top of current state
                currentState.copy(
                    modalStack = currentState.modalStack + 
                        result.navigationState.modalStack
                )
            }
        }
        
        logInfo("AppCoordinator", "Applying deep link with ${result.strategy::class.simpleName}")
        applyNavigationState(newState, clearCurrentStack = false)
    }
}
```

---

### 4. Add Stack Size Monitoring

**File**: `core/src/commonMain/.../navigation/NavigationStateExtensions.kt`

```kotlin
/**
 * Extensions for monitoring navigation state metrics
 */

object NavigationMetrics {
    const val MAX_SAFE_STACK_DEPTH = 20
    const val WARN_STACK_DEPTH = 15
}

fun NavigationState.currentStackDepth(): Int {
    return tabNavigation.stacksByTab.values
        .maxOrNull()?.size ?: 0
}

fun NavigationState.maxStackDepthPerTab(): Map<String, Int> {
    return tabNavigation.stacksByTab.mapValues { it.value.size }
}

fun NavigationState.totalRoutesInState(): Int {
    return tabNavigation.stacksByTab.values.sumOf { it.size } + modalStack.size
}

fun NavigationState.toMetricsString(): String {
    return buildString {
        appendLine("=== Navigation Metrics ===")
        appendLine("Total depth: ${currentStackDepth()}")
        appendLine("Total routes: ${totalRoutesInState()}")
        appendLine("Modal count: ${modalStack.size}")
        appendLine("\nPer-tab breakdown:")
        maxStackDepthPerTab().forEach { (tabId, depth) ->
            val icon = if (depth > NavigationMetrics.WARN_STACK_DEPTH) "⚠️" else "✓"
            appendLine("$icon Tab '$tabId': $depth routes")
        }
    }
}

/**
 * In AppCoordinator.reduceState()
 */
open fun reduceState(event: NavigationEvent) {
    val currentState = _navigationState.value
    val newState = NavigationReducer.reduce(currentState, event, routeHandlers)
    
    // Check stack depth
    val depth = newState.currentStackDepth()
    when {
        depth > NavigationMetrics.MAX_SAFE_STACK_DEPTH -> {
            logError(
                "AppCoordinator",
                "⚠️ Stack depth CRITICAL: $depth (max=${NavigationMetrics.MAX_SAFE_STACK_DEPTH})\n${newState.toMetricsString()}"
            )
            // Optionally: Auto-pop to reasonable depth
        }
        depth > NavigationMetrics.WARN_STACK_DEPTH -> {
            logInfo(
                "AppCoordinator",
                "⚠️ Stack depth elevated: $depth\n${newState.toMetricsString()}"
            )
        }
    }
    
    // ... rest of reduction logic
}
```

---

## Testing All Improvements

**File**: `core/src/commonTest/.../navigation/Priority1ImprovementsTest.kt`

```kotlin
class Priority1ImprovementsTest {
    
    private lateinit var persistenceStore: NavigationPersistenceStore
    private lateinit var coordinator: AppCoordinator
    
    @Before
    fun setup() {
        persistenceStore = InMemoryNavigationPersistenceStore()
        coordinator = AppCoordinator(persistenceStore = persistenceStore)
    }
    
    @Test
    fun testCrashRecoveryFullWorkflow() {
        // 1. Navigate to detail screen
        coordinator.navigateToScreen(Destination.RestaurantDetail("123"))
        
        // 2. Show filter modal
        coordinator.showFilterModal(listOf("vegan"))
        
        // 3. Simulate mark clean shutdown (graceful)
        runBlocking {
            persistenceStore.markCleanShutdown()
        }
        
        // 4. Simulate app restart - load persisted state
        val restorer = NavigationStateRestorer(persistenceStore)
        val restoredState = runBlocking { restorer.restoreNavigationState() }
        
        // 5. Apply restored state
        coordinator.applyNavigationState(restoredState)
        
        // 6. Verify state matches
        val currentState = coordinator.getCurrentState()
        assertEquals(1, currentState.currentStack.size)  // Has detail route
        assertEquals(1, currentState.modalStack.size)    // Has filter modal
    }
    
    @Test
    fun testStackMonitoringWarnings() {
        // Manually create state with deep stack
        val deepStack = (0..25).map { i ->
            RestaurantDetailRoute("$i")
        }
        
        val state = NavigationState(
            tabNavigation = TabNavigationState(
                tabDefinitions = listOf(/* ... */),
                activeTabId = "restaurants",
                stacksByTab = mapOf("restaurants" to deepStack),
                navigationDirection = NavigationDirection.Forward
            )
        )
        
        // Check metrics
        assertEquals(25, state.currentStackDepth())
        assertTrue(state.currentStackDepth() > NavigationMetrics.WARN_STACK_DEPTH)
        
        val metrics = state.toMetricsString()
        assertTrue(metrics.contains("⚠️"))
    }
}
```

---

## Rollout Checklist

- [ ] Implement all Priority 1 items
- [ ] Add comprehensive tests
- [ ] Run test suite to ensure no regressions
- [ ] Update documentation
- [ ] Code review
- [ ] Merge to main
- [ ] Deploy to staging
- [ ] Monitor crash reports for improvement
- [ ] Plan Phase 2 improvements

---

## Common Issues & Solutions

### Issue: Crash indicator not being set on Android

**Problem**: App crashes before `markCleanShutdown()` is called

**Solution**: Use app process death listener:
```kotlin
class CrashHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        // Don't call markCleanShutdown()
        // Leave crash indicator set
        defaultHandler.uncaughtException(t, e)
    }
}
```

### Issue: Modal doesn't restore on iOS

**Problem**: Modal routing isn't exported to Swift

**Solution**: Add to `NavigationExports.ios.kt`:
```kotlin
fun _exportModalRoutesForSwift() {
    // Force include in Swift framework
}
```

---

## Next Steps

1. Implement these 4 Priority 1 improvements
2. Gather feedback from team
3. Move to Phase 2 (history limits, navigation IDs)
4. Create runbooks for crash debugging

