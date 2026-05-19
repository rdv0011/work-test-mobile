# KMM Navigation Restoration Without Native Navigation Controller

**Document**: Navigation Architecture Analysis & Improvement Plan  
**Date**: May 19, 2026  
**Project**: Umain Munchies Mobile (KMM)  
**Status**: Revised — Crash/Config-Change-Only Restoration

---

## Executive Summary

Kotlin Multiplatform Mobile (KMM) provides no native navigation controller. The current implementation successfully **manually mimics native navigation controller behavior** through a Redux-based state management pattern.

This revision aligns the restoration strategy with **native Android `NavController` semantics**:

> **Navigation state is restored ONLY when the process is killed unexpectedly (crash / OS-initiated kill) or a configuration change (rotation, locale, multi-window) triggers Activity recreation. Normal user-initiated exits clear the saved state.**

This is the contract Android's back-stack and `SavedStateHandle` honour by default and what users expect.

### Current State: ✅ Solid Foundation
- Redux-based navigation state management
- Tab-based navigation with per-tab stacks
- Modal overlay support
- Deep link processing
- State persistence mechanism
- Platform-specific scope lifecycle management (Koin)

### Key Change from Previous Version
The old design **always** restored navigation state on every app launch. The revised design introduces **restoration conditions** — restoration only fires when one of these triggers is detected:

| Trigger | Restore? | Rationale |
|---------|----------|-----------|
| Process crash / OS kill | ✅ Yes | User lost context involuntarily |
| Configuration change (rotation, locale) | ✅ Yes | Same user session, UI reborn |
| Normal user exit (home button, swipe away) | ❌ No | User intentionally left; start fresh |
| Fresh install / first launch | ❌ No | No prior state |

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [How Navigation is Currently Managed](#how-navigation-is-currently-managed)
3. [State Restoration Mechanism](#state-restoration-mechanism)
4. [Restoration Trigger Detection](#restoration-trigger-detection)
5. [Platform-Specific Implementation](#platform-specific-implementation)
6. [Current Strengths](#current-strengths)
7. [Identified Gaps & Issues](#identified-gaps--issues)
8. [Recommended Improvements](#recommended-improvements)
9. [Implementation Roadmap](#implementation-roadmap)

---

## Architecture Overview

### Core Pattern: Redux for Navigation

The navigation system follows Redux principles:
- **Single State**: `NavigationState` holds all navigation data
- **Pure Reducers**: `NavigationReducer.reduce()` transforms state without side effects
- **Events**: `NavigationEvent` captures all navigation actions
- **Side Effects**: `NavigationEffects` manages scope lifecycle

```
┌─────────────────────────────────────────────────────────────┐
│                    Navigation Architecture                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  UI Layer (Compose/SwiftUI)                                │
│  ↓                                                          │
│  AppCoordinator (Central Orchestrator)                      │
│  ├─ Events Dispatch                                        │
│  ├─ State Reduction                                        │
│  ├─ Scope Lifecycle                                        │
│  └─ Conditional Persistence                                │
│  ↓                                                          │
│  NavigationReducer (Pure State Transformation)             │
│  ├─ Push/Pop/PopToRoot (Screen Navigation)                │
│  ├─ ShowModal/DismissModal (Modal Navigation)             │
│  ├─ SelectTab/PushInTab (Tab Navigation)                  │
│  └─ ApplyNavigationState (Deep Link / Restoration)        │
│  ↓                                                          │
│  NavigationState (Observable State)                        │
│  ├─ TabNavigationState                                     │
│  │  ├─ Tab Definitions                                     │
│  │  ├─ Active Tab ID                                       │
│  │  └─ Stack per Tab                                       │
│  └─ Modal Stack                                            │
│  ↓                                                          │
│  Persistence Layer (conditional, see below)               │
│  ├─ Serialization (NavigationStateSnapshot)               │
│  ├─ Storage (Platform-specific: DataStore/UserDefaults)   │
│  └─ Restoration (NavigationStateRestorer)                 │
│  ↓                                                          │
│  RestoreConditionDetector                                  │
│  ├─ Android: Activity.isFinishing + process death flag    │
│  └─ iOS: UIApplication termination reason flag            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## How Navigation is Currently Managed

### 1. Navigation Events & Reduction

**Navigation Events** (sealed class):
- `Push(destination)` - Navigate to a new screen
- `Pop` - Go back (dismiss modal if open, else pop stack)
- `PopToRoot` - Clear stack to root
- `ShowModal(destination)` - Present modal overlay
- `DismissModal` / `DismissAllModals` / `DismissModalUntil` - Modal dismissal
- `SelectTab(tabId)` - Switch active tab
- `PushInTab(destination)` - Push within active tab
- `PopInTab` - Pop from active tab
- `ApplyNavigationState(newState)` - Apply full state (deep link / restoration)

**Reducer Pattern** (NavigationReducer.kt):
```kotlin
fun reduce(
    currentState: NavigationState,
    event: NavigationEvent,
    routeHandlers: List<RouteHandler>
): NavigationState {
    // Each event handler is a pure function
    // No side effects, deterministic output
    // Handlers resolve Destination → Route via RouteHandlers
}
```

**Why Redux for Navigation?**
1. **Deterministic**: Same state + event always produces same result
2. **Testable**: Pure functions, no mocking required
3. **Debuggable**: Event history shows exact navigation path
4. **Restorable**: Previous state can be reapplied after crash
5. **Time-travel**: Navigate back/forward through history

### 2. State Structure: Tab Navigation + Modals

```kotlin
data class NavigationState(
    val modalStack: List<ModalRoute> = emptyList(),  // Top overlay layer
    val tabNavigation: TabNavigationState,             // Base tab-based navigation
    val originDeepLink: String? = null
)

data class TabNavigationState(
    val tabDefinitions: List<TabDefinition>,          // Tab metadata
    val activeTabId: String,                          // Which tab is visible
    val stacksByTab: Map<String, List<Route>>,       // Per-tab navigation stacks
    val navigationDirection: NavigationDirection       // Animation direction hint
)
```

### 3. Scope Lifecycle Management (Koin Integration)

**Problem**: ViewModels must survive configuration changes (rotation, locale change)

**Solution**: Scoped Dependency Injection
- Each Route gets a unique `scopeId = "${routeKey}"`
- Scope is created when route enters navigation state
- Scope persists across recompositions (via `remember(scopeId)`)
- Scope is closed when route exits navigation state

---

## State Restoration Mechanism

### Revised Restoration Contract (Android NavController Semantics)

The restored state should behave exactly like the Android `NavController` back-stack:

```
┌──────────────────────────────────────────────────────────────┐
│               Restoration Decision Gate                      │
│                                                              │
│  On app startup, BEFORE loading any UI:                      │
│                                                              │
│  1. Was the process killed while the user was NOT inside     │
│     the app (crash / OS memory pressure)?  → RESTORE        │
│                                                              │
│  2. Is this Activity recreated due to a configuration        │
│     change (rotation, locale, dark mode toggle)?  → RESTORE │
│                                                              │
│  3. Did the user explicitly dismiss the app                  │
│     (recent-apps swipe, Finish(), home button tap)?  → SKIP │
│                                                              │
│  4. Is this a cold start with no prior session?  → SKIP     │
└──────────────────────────────────────────────────────────────┘
```

### Revised Restoration Flow

```
1. APP STARTUP
   ↓
2. RestoreConditionDetector.shouldRestore()
   ├─ Android: check Activity.isChangingConfigurations OR process-death flag
   └─ iOS:     check UIApplication.applicationState transition reason
   ↓
3a. shouldRestore == true?
   ├─ YES → NavigationStateRestorer.restoreNavigationState()
   │       ├─ Validate snapshot
   │       ├─ Valid? → Deserialize to NavigationState
   │       └─ Invalid? → Use default state
   └─ NO  → NavigationStateRestorer.createDefaultNavigationState()
            + Clear any stale persisted snapshot
   ↓
4. Apply resulting state to AppCoordinator
   ↓
5. Create Koin scopes for all routes in state
   ↓
6. UI renders from NavigationState
```

### What Persistence Looks Like Now

State is still saved **after every navigation event** (so crash recovery is always possible).  
The difference is in the **read path** — restoration is gated, not unconditional.

```
WRITE path (unchanged):  every NavigationEvent → persist snapshot async
READ  path (revised):    startup → check condition → restore OR start fresh
                                                              ↑
                                             NEW: this gate did not exist before
```

### Snapshot Serialization

**NavigationStateSnapshot** (`NavigationStateSnapshot.kt`):
```kotlin
@Serializable
data class NavigationStateSnapshot(
    val tabNavigation: TabNavigationStateSnapshot,
    val modalStack: List<ModalRoute> = emptyList(),
    val originDeepLink: String? = null,
    val restoredFromCrash: Boolean = false,          // set true when restoring after crash
    val restorationTimestamp: Long = 0L
)
```

**Why Snapshots?**
- `NavigationState` contains lambdas (not serializable)
- Snapshots use only serializable primitives
- `toSnapshot()` / `toNavigationState()` convert bidirectionally

---

## Restoration Trigger Detection

### Android

On Android, the distinction between crash/config-change and deliberate exit maps directly to Activity lifecycle:

| Scenario | `isFinishing` | `isChangingConfigurations` | Action |
|----------|---------------|----------------------------|--------|
| Rotation / locale change | false | **true** | Restore |
| Process killed by OS (low memory, crash) | — | — (no `onDestroy`) | Restore via saved instance state / flag |
| User swipes app from recents | **true** | false | Start fresh + clear snapshot |
| User presses back to root | **true** | false | Start fresh + clear snapshot |
| Normal home button | false | false | Keep snapshot (may crash later) |

**Implementation strategy for Android**:
1. Use `Activity.onSaveInstanceState` to write a `RESTORE_REQUESTED = true` marker into the Bundle. This is what `NavController` does internally.
2. On `Activity.onCreate`, check `savedInstanceState?.getBoolean(RESTORE_REQUESTED)`.
3. If the Bundle is present → restore. If absent → fresh start.
4. For **process death** (crash, OS kill): the Bundle is delivered by the system automatically when the process is re-created; the marker is already inside it.

```kotlin
// In MainActivity
private const val KEY_RESTORE_NAV = "nav_restore_requested"

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val shouldRestore = savedInstanceState?.getBoolean(KEY_RESTORE_NAV, false) ?: false
    coordinator.initializeNavigation(restoreState = shouldRestore)
}

override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(KEY_RESTORE_NAV, true)
    // State is already persisted asynchronously on every navigation event
}

override fun onDestroy() {
    super.onDestroy()
    if (isFinishing && !isChangingConfigurations) {
        // Deliberate exit — clear snapshot so next launch starts fresh
        lifecycleScope.launch { coordinator.clearPersistedNavigationState() }
    }
}
```

### iOS

iOS has no direct equivalent of `isChangingConfigurations`. The closest pattern:

| Scenario | Action |
|----------|--------|
| App process terminated by OS / crash | Restore (no `applicationWillTerminate` called) |
| User force-quits from app switcher | `applicationWillTerminate` IS called → write "clean exit" flag → on next launch, skip restore |
| Scene disconnected (multi-window dismissed) | `sceneDidDisconnect` called → treat as config change if scene reconnects → restore |

```kotlin
// In AppDelegate equivalent (Kotlin side bridged to Swift)
fun onApplicationWillTerminate() {
    // User explicitly killed the app — clear snapshot
    persistenceStore.clearNavigationSnapshot()
}

// On launch without "clean exit" flag → restore (crash or OS kill occurred)
fun shouldRestoreNavigation(): Boolean {
    return !persistenceStore.hasCleanExitFlag().also {
        persistenceStore.clearCleanExitFlag()
    }
}
```

---

## Platform-Specific Implementation

### Android Integration

**Navigation Entry Point** (`AppNavigation.kt`):
- Collects `navigationState` from `AppCoordinator`
- Processes pending deep links in `LaunchedEffect`
- Renders tab scaffold with animated content
- Renders modal overlay if needed
- Each route resolves ViewModel from Koin scope

**State Survival Test** (`ViewModelConfigChangeIntegrationTest.kt`):
```
Test Strategy:
1. Define route with specific scope ID
2. Retrieve ViewModel via Koin scope
3. Trigger recomposition (state change / rotation)
4. Retrieve ViewModel again
5. Assert: Same object reference (ViewModel survived)
```

### iOS Integration

**Type Exports** (`NavigationExports.ios.kt`):
- Forces Swift compilation to include route types
- Enables SwiftUI to access route data
- Factory methods for string resources and constants

**Scope Lifecycle** (`NavigationEffects.ios.kt`):
```kotlin
actual fun getKoinScopeOrNull(scopeId: String): Closeable? {
    val scope = koin.getScopeOrNull(scopeId)
    return scope?.let { IOSKoinScopeCloseable(it, scopeId) }
}
```

---

## Current Strengths

### ✅ 1. Clean Separation of Concerns
- UI layer knows nothing about state management
- Coordinator handles all navigation logic
- Reducer is pure, testable, debuggable
- Effects are isolated to specific platform layers

### ✅ 2. Deterministic State Management
- Redux pattern ensures reproducible behavior
- Same event + state always yields same result
- Crash recovery is deterministic (replay saved state)

### ✅ 3. Deep Link Support
- `DeepLinkParser` converts URLs to navigation states
- `AppCoordinator.applyDeepLink()` applies parsed state
- State can be applied with or without clearing current stack

### ✅ 4. Tab Navigation Foundation
- Per-tab stacks prevent "cross-tab contamination"
- Tab switching is efficient (just change activeTabId)
- Stack history preserved per tab

### ✅ 5. Modal Overlay System
- Independent modal stack
- Multiple presentation styles
- Dismissal predicates for conditional modal removal

### ✅ 6. Scope Lifecycle Integration
- ViewModels survive configuration changes
- Scopes created/destroyed with routes
- Koin integration is minimal, non-invasive

### ✅ 7. Persistence Abstraction
- Storage implementation can be swapped (DataStore/UserDefaults)
- Async persistence to avoid blocking main thread
- Serialization module is extensible

---

## Identified Gaps & Issues

### ⚠️ 1. Always-Restoring on Every Launch (CRITICAL — New)

**Issue**: Previous design restores state unconditionally on every app launch.

**Problem**: This violates user expectations. When a user deliberately dismisses the app and relaunches, they expect a fresh start — not their previous session. This is the behaviour both `UINavigationController` (iOS) and `NavController` (Android) implement by default.

**Fix**: Gate restoration on `savedInstanceState` Bundle presence (Android) and the absence of a "clean exit" flag (iOS), as detailed in the [Restoration Trigger Detection](#restoration-trigger-detection) section.

---

### ⚠️ 2. Missing `onDestroy` / `applicationWillTerminate` Cleanup

**Issue**: Saved snapshot is never cleared on deliberate exit.

**Gap**: If the user swipes the app away from recents, the snapshot persists. On next launch, the old design would incorrectly restore it.

**Fix**: In `onDestroy` (when `isFinishing && !isChangingConfigurations`), clear the persisted snapshot. On iOS, clear in `applicationWillTerminate`.

---

### ⚠️ 3. Incomplete Modal Stack Restoration

**Issue**: Modal stack is restored but UI may not render it correctly after crash.

**Current Behavior**:
- Modal routes are serialized in snapshot
- Modal stack is restored
- But modal UI rendering depends on platform layer

**Gap**: No explicit test for modal restoration after crash

---

### ⚠️ 4. Missing Crash Context Metadata

**Issue**: `restoredFromCrash` flag exists in snapshot but is never set to `true`.

**Fix**: Set the flag when restoration fires because of an unclean shutdown (no "clean exit" marker found).

---

### ⚠️ 5. No Navigation History Limits

**Issue**: No maximum stack depth enforcement.

**Gap**: Unbounded stack growth possible in edge cases, bloating the persisted snapshot.

---

### ⚠️ 6. Async Persistence Race Condition

**Issue**: Rapid navigation might outpace persistence.

**Scenario**:
```
1. User navigates rapidly: A → B → C → D
2. Each navigation triggers persistNavigationStateAsync()
3. App crashes before all states persisted
4. Recovery loads incomplete state
```

**Gap**: Concurrent navigation + persistence not synchronized.

---

### ⚠️ 7. No Validation During Route Resolution

**Issue**: Routes resolved via handlers but not validated. Silent failures possible if a route handler returns invalid state.

---

### ⚠️ 8. Incomplete Error Recovery Logging

**Issue**: Deserialization failures fall back to default state but the reason is never logged.

---

## Recommended Improvements

### Priority 1: Crash/Config-Change-Only Restoration Gate

#### 1.1 Add `RestoreConditionDetector`
```kotlin
interface RestoreConditionDetector {
    /**
     * Returns true ONLY if the app should restore navigation state:
     *   - Configuration change (Android: savedInstanceState present + isChangingConfigurations)
     *   - Process death / crash (Android: savedInstanceState present, iOS: no clean-exit flag)
     *
     * Returns false for deliberate user exits and cold starts.
     */
    fun shouldRestoreNavigation(): Boolean
}
```

**Android implementation**:
```kotlin
class AndroidRestoreConditionDetector(
    private val savedInstanceState: Bundle?
) : RestoreConditionDetector {
    override fun shouldRestoreNavigation(): Boolean =
        savedInstanceState?.getBoolean(KEY_RESTORE_NAV, false) ?: false
}
```

**iOS implementation**:
```kotlin
class IosRestoreConditionDetector(
    private val persistenceStore: NavigationPersistenceStore
) : RestoreConditionDetector {
    override fun shouldRestoreNavigation(): Boolean {
        val hadCleanExit = persistenceStore.hasCleanExitFlag()
        persistenceStore.clearCleanExitFlag()
        return !hadCleanExit
    }
}
```

---

#### 1.2 Clear Snapshot on Deliberate Exit

**Android** (in `MainActivity`):
```kotlin
override fun onDestroy() {
    super.onDestroy()
    if (isFinishing && !isChangingConfigurations) {
        lifecycleScope.launch {
            coordinator.clearPersistedNavigationState()
        }
    }
}
```

**iOS** (bridged from Swift `AppDelegate`):
```kotlin
fun onApplicationWillTerminate() {
    persistenceStore.markCleanExit()
}
```

---

#### 1.3 Enable `restoredFromCrash` Flag
```kotlin
suspend fun restoreNavigationState(
    detector: RestoreConditionDetector
): NavigationState {
    if (!detector.shouldRestoreNavigation()) {
        return createDefaultNavigationState()
    }

    val snapshot = persistenceStore.loadNavigationState().getOrNull()
        ?: return createDefaultNavigationState()

    if (!isValidSnapshot(snapshot)) {
        return createDefaultNavigationState()
    }

    val isCrashRecovery = !persistenceStore.hadCleanShutdown()
    return snapshot.toNavigationState().also {
        if (isCrashRecovery) {
            logInfo("NavigationStateRestorer", "Restored after crash")
        }
    }
}
```

---

#### 1.4 Add Modal Restoration Test
```kotlin
@Test
fun testModalStackRestoredAfterCrash() {
    // Given: state with open modal saved, no clean-exit flag
    val state = stateWithModal(FilterModalRoute(listOf("vegan")))
    persistenceStore.saveNavigationState(state.toSnapshot())
    // no markCleanExit() call → simulates crash

    val detector = IosRestoreConditionDetector(persistenceStore)
    val restored = restorer.restoreNavigationState(detector)

    assertEquals(1, restored.modalStack.size)
    assertTrue(restored.modalStack[0] is FilterModalRoute)
}

@Test
fun testNoRestorationOnCleanExit() {
    // Given: state saved AND clean exit marked
    persistenceStore.saveNavigationState(someState.toSnapshot())
    persistenceStore.markCleanExit()

    val detector = IosRestoreConditionDetector(persistenceStore)
    val restored = restorer.restoreNavigationState(detector)

    // Should start fresh
    assertEquals(defaultState, restored)
}
```

---

### Priority 2: High Impact, Medium Effort

#### 2.1 Implement Navigation History Limit with Auto-Pruning
```kotlin
data class NavigationConfig(
    val maxStackDepthPerTab: Int = 20,
    val pruningStrategy: StackPruningStrategy = StackPruningStrategy.KEEP_ROOT_PLUS_N(10)
)
```

#### 2.2 Add Navigation ID & Lifecycle Tracking
Prevents stale data rendering during rapid navigation (e.g. quick push/pop).

#### 2.3 Implement Persistence Ordering & Sync
Use a `Channel<NavigationState>(capacity = 1)` so that only the latest state is ever written.

#### 2.4 Add Route Handler Validation
Wrap handlers in a `ValidatingRouteHandler` that catches exceptions and logs invalid routes.

---

### Priority 3: Nice to Have

- Analytics integration hook
- Debugging tools
- Navigation documentation / playbook
- Comprehensive test coverage

---

## Implementation Roadmap

### Phase 1: Correct Restoration Semantics (Weeks 1-2)
1. ✅ Add `RestoreConditionDetector` interface + platform implementations
2. ✅ Clear snapshot on `onDestroy(isFinishing)` / `applicationWillTerminate`
3. ✅ Enable `restoredFromCrash` flag
4. ✅ Add modal restoration tests (crash path AND clean-exit path)

**Deliverables**:
- Navigation restored only when OS/crash demands it
- Clean exit always produces a fresh launch
- Test coverage for both paths

---

### Phase 2: Robustness (Weeks 3-4)
1. ✅ Navigation history limits
2. ✅ Navigation ID tracking
3. ✅ Sync persistence queue
4. ✅ Route handler validation

---

### Phase 3: Observability (Weeks 5-6)
1. ✅ Analytics hook
2. ✅ Debugging tools
3. ✅ Navigation documentation
4. ✅ Integration tests

---

## Comparison to Native Navigation Controllers

### Android NavController vs KMM Implementation (Revised)

| Feature | NavController | KMM Redux Pattern |
|---------|--------------|-------------------|
| **Backstack** | Automatic LIFO | Manual via reducer |
| **Restoration trigger** | `savedInstanceState` Bundle | `RestoreConditionDetector` (same semantics) |
| **Clean exit clears state** | ✅ Yes (Fragment back-stack destroyed) | ✅ Yes (after this revision) |
| **Crash / config change restores** | ✅ Yes (Bundle survives) | ✅ Yes (after this revision) |
| **Deep Link** | Built-in nav graph | Parser-based |
| **State Restoration** | Auto via Bundle | Manual snapshot persistence |

### iOS UINavigationController vs KMM Implementation (Revised)

| Feature | UINavigationController | KMM Redux Pattern |
|---------|----------------------|-------------------|
| **Backstack** | Automatic LIFO | Manual via reducer |
| **Restoration trigger** | `UIStateRestoration` (opt-in) | `IosRestoreConditionDetector` |
| **Clean exit clears state** | ✅ Yes (no state restoration by default) | ✅ Yes (after this revision) |
| **Crash restores** | ❌ Not by default | ✅ Yes (after this revision) |

---

## Conclusion

The KMM navigation system is a solid, deterministic, Redux-based manual navigation controller. The primary gap identified in this revision is that **state was being unconditionally restored** on every app launch, violating the contract that users and both native platforms expect: only crash recovery and configuration changes should restore the back-stack.

**Recommended immediate next steps**:
1. Implement `RestoreConditionDetector` on both platforms (highest priority)
2. Add `onDestroy` / `applicationWillTerminate` snapshot cleanup
3. Enable `restoredFromCrash` flag in snapshot
4. Test both the restoration and the non-restoration paths

---

## References

- **Android NavController state restoration**: https://developer.android.com/guide/navigation/navigate#restore-state
- **Activity.isChangingConfigurations**: https://developer.android.com/reference/android/app/Activity#isChangingConfigurations()
- **savedInstanceState semantics**: https://developer.android.com/topic/libraries/architecture/saving-states
- **Redux Pattern**: https://redux.js.org/understanding/thinking-in-redux
- **Kotlin Multiplatform**: https://kotlinlang.org/docs/multiplatform.html
- **Koin Scope Management**: https://insert-koin.io/docs/reference/koin-compose/scopes/
