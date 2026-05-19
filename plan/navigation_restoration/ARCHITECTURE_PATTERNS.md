# KMM Navigation: Architecture Patterns & Diagrams

**Document**: Visual architecture guide and pattern reference  
**Purpose**: Quick reference for understanding and extending the navigation system  
**Date**: May 19, 2026 (Revised — Crash/Config-Change-Only Restoration)

---

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PLATFORM LAYER                             │
│  ┌──────────────────────────┐  ┌─────────────────────────────────┐ │
│  │  Android (Jetpack        │  │  iOS (SwiftUI)                  │ │
│  │  Compose)                │  │                                 │ │
│  │                          │  │                                 │ │
│  │  - AppNavigation.kt      │  │  - NavigationController.swift   │ │
│  │  - RouteRenderer.kt      │  │  - StateObserver.swift          │ │
│  │  - Navigation Effects    │  │  - DeepLinkHandler.swift        │ │
│  │  - MainActivity          │  │  - AppDelegate (terminate hook) │ │
│  └──────────────────────────┘  └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                 ↑
                    Collects NavigationState
                                 ↑
┌─────────────────────────────────────────────────────────────────────┐
│                     NAVIGATION LAYER (KMM)                         │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  AppCoordinator (Central Orchestrator)                       │ │
│  │  - Owns: NavigationState, Reducers, RouteHandlers          │ │
│  │  - Dispatch events                                          │ │
│  │  - Manage scope lifecycle                                   │ │
│  │  - Trigger persistence                                      │ │
│  │  - clearPersistedNavigationState() on clean exit           │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationReducer (Pure State Machine)                      │ │
│  │  - (State, Event) → State (deterministic)                   │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationState (Observable State)                          │ │
│  │  - TabNavigationState (per-tab stacks)                      │ │
│  │  - ModalStack (overlay routes)                              │ │
│  └──────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                 ↑
                    Observes and reduces events
                                 ↑
┌─────────────────────────────────────────────────────────────────────┐
│                     RESTORATION LAYER (KMM)                        │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  RestoreConditionDetector (NEW)                              │ │
│  │  - shouldRestoreNavigation(): Boolean                       │ │
│  │  - Android: checks savedInstanceState Bundle presence      │ │
│  │  - iOS:     checks absence of "clean exit" flag            │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationStateRestorer                                     │ │
│  │  - Accepts RestoreConditionDetector                         │ │
│  │  - If shouldRestore → loads snapshot                        │ │
│  │  - If !shouldRestore → returns default state               │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationStateSnapshot (Serializable)                      │ │
│  │  - toSnapshot() / toNavigationState() converters            │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationPersistenceStore (Interface)                      │ │
│  │  + hasCleanExitFlag() / markCleanExit() / clearCleanExitFlag│ │
│  └──────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                 ↑
                    Save/Load snapshots asynchronously
                                 ↑
┌─────────────────────────────────────────────────────────────────────┐
│                    PLATFORM-SPECIFIC STORAGE                        │
│  ┌──────────────────────────────┐  ┌──────────────────────────────┐ │
│  │  Android DataStore           │  │  iOS UserDefaults            │ │
│  │  /data/user/0/.../prefs.pb   │  │  ~/Library/Preferences/...   │ │
│  └──────────────────────────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Restoration Decision Flow

```
APP STARTUP
│
├─ RestoreConditionDetector.shouldRestoreNavigation()
│
│  Android:
│  ├─ savedInstanceState Bundle present?
│  │  ├─ YES (config change or process death) ──────────────────→ RESTORE
│  │  └─ NO  (cold start / clean launch) ──────────────────────→ FRESH
│
│  iOS:
│  ├─ "clean exit" flag absent?
│  │  ├─ YES (crash, OS kill, first launch) ───────────────────→ RESTORE
│  │  └─ NO  (applicationWillTerminate was called) ────────────→ FRESH
│
├─ RESTORE path:
│  ├─ Load snapshot from storage
│  ├─ Validate (tabs, stacks, active tab)
│  ├─ Valid? → deserialize to NavigationState
│  └─ Invalid? → createDefaultNavigationState()
│
└─ FRESH path:
   ├─ createDefaultNavigationState()
   └─ (optionally clear stale snapshot from disk)
```

---

## State Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Navigation Event                         │
│                    (Push, Pop, ShowModal, etc)                  │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│                  AppCoordinator.dispatch()                      │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│              AppCoordinator.reduceState()                       │
│                                                                 │
│  1. Get current state                                           │
│  2. Call NavigationReducer.reduce(state, event)                │
│  3. Create/destroy Koin scopes via NavigationEffects           │
│  4. Update _navigationState                                    │
│  5. Enqueue async persistence (Channel.CONFLATED)             │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│           NavigationReducer.reduce() [PURE]                    │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│        NavigationState (Observable via StateFlow)              │
│  ┌─────────────────────┐                                        │
│  │ TabNavigationState  │  Map<TabId, List<Route>>              │
│  └─────────────────────┘                                        │
│  ┌──────────────────────┐                                       │
│  │ modalStack           │  List<ModalRoute>                    │
│  └──────────────────────┘                                       │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│           UI Layer (Android/iOS)                                │
│  collectAsState(navigationState) { state ->                    │
│    render(state.tabNavigation.getActiveTabStack())             │
│    if (state.modalStack.isNotEmpty()) {                        │
│      renderModal(state.modalStack.last())                      │
│    }                                                           │
│  }                                                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## Tab Navigation State Machine

```
                          ┌─────────────┐
                          │  Restaurants│ (activeTab)
                          │   Tab Root  │
                          └──────┬──────┘
                                 │ navigateToScreen(DetailRoute(123))
                                 ↓
                          ┌──────────────────┐
                   ┌──────│  Restaurants     │
                   │      │  - ListRoute     │
                   │      │  → DetailRoute   │
                   │      └──────────────────┘
                   │
                   │ navigateToScreen(ReviewRoute(123))
                   ↓
        ┌──────────────────────────────┐
        │  Restaurants                 │
        │  - ListRoute                 │
        │  - DetailRoute               │
        │  → ReviewRoute               │
        └──────┬───────────────────────┘
               │ navigateBack()
               ↓
        ┌──────────────────────────────┐
        │  Restaurants                 │
        │  - ListRoute                 │
        │  → DetailRoute               │
        └──────┬───────────────────────┘
               │ selectTab("settings")
               ↓
        ┌──────────────────────────────┐
        │  Settings                    │  (activeTab switched)
        │  → SettingsRoute             │  Restaurants stack preserved
        └──────┬───────────────────────┘
               │ selectTab("restaurants")
               ↓
        ┌──────────────────────────────┐
        │  Restaurants                 │
        │  - ListRoute                 │
        │  → DetailRoute               │  ← same state as before
        └──────────────────────────────┘
```

---

## Modal Overlay State Machine

```
Current Screen: RestaurantDetailRoute

   ┌────────────────────────────────────┐
   │ Screen: RestaurantDetailRoute       │
   │ Modal:  (none)                     │
   └────────────────────────────────────┘
           │ showFilterModal([...])
           ↓
   ┌────────────────────────────────────┐
   │ Screen: RestaurantDetailRoute       │
   │ Modal:  FilterModalRoute            │
   │ ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲│
   └────────────────────────────────────┘
           │ showModal(ConfirmDelete)
           ↓
   ┌────────────────────────────────────┐
   │ Screen: RestaurantDetailRoute       │
   │ Modal:  ConfirmActionModalRoute     │  (stacked on top)
   │ ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲│
   │  (FilterModalRoute underneath)      │
   └────────────────────────────────────┘
           │ dismissModal()
           ↓
   ┌────────────────────────────────────┐
   │ Screen: RestaurantDetailRoute       │
   │ Modal:  FilterModalRoute            │
   └────────────────────────────────────┘
           │ dismissAllModals()
           ↓
   ┌────────────────────────────────────┐
   │ Screen: RestaurantDetailRoute       │
   │ Modal:  (none)                     │
   └────────────────────────────────────┘
```

---

## Crash Recovery Flow (Revised)

```
APP RUNNING
│
├─ User navigates: RestaurantList → Detail → Modal
│
├─ persistNavigationStateAsync() saves state after each event
│  └─ Snapshot written to DataStore/UserDefaults
│
└─ CRASH (or OS kills process without warning)
   └─ applicationWillTerminate / onDestroy NOT called
      └─ No "clean exit" flag written (iOS)
      └─ savedInstanceState written by system (Android)


APP RESTART
│
├─ MainActivity.onCreate(savedInstanceState) ← Bundle present (Android)
│   └─ AndroidRestoreConditionDetector(bundle).shouldRestoreNavigation() = true
│
│ (iOS: IosRestoreConditionDetector sees no clean-exit flag → true)
│
├─ NavigationStateRestorer.restoreNavigationState(detector)
│  ├─ detector.shouldRestoreNavigation() → TRUE
│  ├─ Load snapshot from persistence store
│  ├─ Validate snapshot
│  └─ Deserialize to NavigationState
│
├─ AppCoordinator.applyNavigationState(restoredState)
│
├─ Create Koin scopes for all routes in state
│
├─ UI renders from restored state
│  └─ Same screens + modals as before crash
│
└─ USER SEES: App recovered to previous state ✓
```

---

## Clean Exit Flow (Revised)

```
APP RUNNING
│
├─ User navigates: RestaurantList → Detail
│
├─ State persisted: [ListRoute, DetailRoute]
│
└─ USER SWIPES APP FROM RECENTS
   │
   ├─ Android: onDestroy(isFinishing=true, isChangingConfigurations=false)
   │  └─ coordinator.clearPersistedNavigationState()
   │
   └─ iOS: applicationWillTerminate
      └─ persistenceStore.markCleanExit()


APP RESTART (fresh launch)
│
├─ Android: savedInstanceState = null
│  └─ AndroidRestoreConditionDetector(null).shouldRestoreNavigation() = false
│
│ iOS: clean-exit flag present
│  └─ IosRestoreConditionDetector.shouldRestoreNavigation() = false
│
├─ NavigationStateRestorer.restoreNavigationState(detector)
│  └─ detector.shouldRestoreNavigation() → FALSE
│  └─ return createDefaultNavigationState()
│
└─ USER SEES: Fresh app launch ✓  (no stale state)
```

---

## Configuration Change Flow (Revised)

```
APP RUNNING (Android)
│
├─ User navigates: RestaurantList → Detail → Modal
│
└─ USER ROTATES DEVICE
   │
   ├─ Activity.onSaveInstanceState(bundle)
   │  └─ bundle.putBoolean(KEY_RESTORE_NAV, true)
   │
   ├─ Activity.onDestroy(isFinishing=false, isChangingConfigurations=true)
   │  └─ isChangingConfigurations=true → do NOT clear snapshot
   │
   └─ Activity recreated immediately


ACTIVITY RECREATED
│
├─ MainActivity.onCreate(savedInstanceState ← bundle with KEY_RESTORE_NAV=true)
│  └─ AndroidRestoreConditionDetector(bundle).shouldRestoreNavigation() = true
│
├─ NavigationStateRestorer.restoreNavigationState(detector)
│  └─ Deserialize → same state
│
└─ USER SEES: Rotation preserved navigation state ✓
   └─ Koin scopes already exist (ViewModel survived via Koin)
```

---

## Serialization Architecture

```
NavigationState (in-memory)
│
├─ May contain lambdas → not serializable
│
└─ CONVERT via toSnapshot()
       ↓
NavigationStateSnapshot (@Serializable)
│
├─ tabNavigation: TabNavigationStateSnapshot
├─ modalStack: List<ModalRoute>
├─ originDeepLink: String?
├─ restoredFromCrash: Boolean
└─ restorationTimestamp: Long
       ↓
JSON String  (via navigationJson)
       ↓
Write to Disk
├─ Android: DataStore
└─ iOS: UserDefaults
       ↓
APP RESTART  (only if RestoreConditionDetector fires)
       ↓
Read from Disk
       ↓
Deserialize JSON
       ↓
NavigationStateSnapshot
       ↓
CONVERT via toNavigationState()
       ↓
NavigationState → Apply to UI
```

---

## Scope Lifecycle Diagram

```
Navigation Event: Push(DetailRoute(123))
       ↓
NavigationReducer creates: Route = DetailRoute(123)
       ↓
AppCoordinator.reduceState()
Detects: Route added to navigation
       ↓
ScopedRouteHandler.createScope()
- scopeId = "detail_123"
- Creates Koin scope
- Registers ViewModel factory
       ↓
Scope persists until route exits navigation state
       ↓
UI: remember(route.key) { koin.getScope(key).get<ViewModel>() }
Same ViewModel returned across recompositions (survives rotation)

─────────────────────────────────────────

Navigation Event: Pop
Route removed
       ↓
NavigationEffects.handleNavigationSideEffects()
Finds detail_123 no longer in state
       ↓
scope.close()  →  ViewModel cleaned up
```

---

## Platform Integration Points

### Android Platform

```
MainActivity.onCreate(savedInstanceState)
│
├─ AndroidRestoreConditionDetector(savedInstanceState)
├─ coordinator.initializeNavigation(detector)
└─ setContent { AppNavigation(coordinator) }

MainActivity.onSaveInstanceState(bundle)
└─ bundle.putBoolean(KEY_RESTORE_NAV, true)

MainActivity.onDestroy()
└─ if (isFinishing && !isChangingConfigurations)
   └─ coordinator.clearPersistedNavigationState()

AppNavigation.kt (Composable)
├─ Collect navigationState
├─ Process pending deep link
├─ Render TabNavigationScaffold
└─ Render modals if needed
```

### iOS Platform

```
AppDelegate.applicationWillTerminate
└─ coordinator.onApplicationWillTerminate()
   └─ persistenceStore.markCleanExit()

App launch
└─ IosRestoreConditionDetector(persistenceStore)
   └─ shouldRestoreNavigation() = !hasCleanExitFlag()
      └─ clearCleanExitFlag() (consume flag)

NavigationController.swift
└─ @StateObject navigationState: coordinator.navigationState.toObservable()
   └─ NavigationStack { RenderScreen(...) }
      └─ .sheet(isPresented:) { RenderModal(...) }
```

---

## Testing Patterns

### Unit Test Pattern
```kotlin
// Pure reducer testing
@Test
fun testReducerPush() {
    val state = initialState()
    val event = NavigationEvent.Push(DetailDestination("123"))
    val newState = NavigationReducer.reduce(state, event)

    assertEquals(2, newState.currentStack.size)
    assertTrue(newState.currentStack.last() is DetailRoute)
}
```

### Restoration Test Patterns (Revised)
```kotlin
// Crash path — should restore
@Test
fun testRestorationAfterCrash() = runTest {
    store.saveNavigationState(stateWithDetail().toSnapshot())
    // No markCleanExit → crash

    val restored = restorer.restoreNavigationState(crashDetector())
    assertEquals(2, restored.currentStack.size)
}

// Clean exit path — should NOT restore
@Test
fun testNoRestorationAfterCleanExit() = runTest {
    store.saveNavigationState(stateWithDetail().toSnapshot())
    store.markCleanExit()  // Deliberate exit

    val restored = restorer.restoreNavigationState(cleanDetector())
    assertEquals(1, restored.currentStack.size)  // Default: root only
}
```

### Platform Test Pattern
```kotlin
// Android config change survival
@Test
fun testViewModelSurvivesRotation() {
    val vm1 = getViewModelFromScope(scopeId)
    triggerRecomposition()
    val vm2 = getViewModelFromScope(scopeId)
    assert(vm1 === vm2)  // Same object
}
```

---

## Error Recovery Strategies

```
┌─────────────────────────────────────────────────┐
│        Serialization Failure                    │
│  (Route type not registered)                    │
└──────────────────┬──────────────────────────────┘
                   ↓
        Catch SerializationException
                   ↓
    ┌───────────────────────────────┐
    │ Log error with details        │
    │ Load default state instead    │
    │ User sees app in safe state   │
    └───────────────────────────────┘


┌─────────────────────────────────────────────────┐
│        Validation Failure                       │
│  (Snapshot missing tabs/stacks)                 │
└──────────────────┬──────────────────────────────┘
                   ↓
        isValidSnapshot() = false
                   ↓
    ┌───────────────────────────────┐
    │ Load default state            │
    │ Log reason for skipping       │
    └───────────────────────────────┘


┌─────────────────────────────────────────────────┐
│        shouldRestoreNavigation() = false        │
│  (Clean exit detected)                          │
└──────────────────┬──────────────────────────────┘
                   ↓
    ┌───────────────────────────────┐
    │ Skip restoration entirely     │
    │ Return fresh default state    │
    └───────────────────────────────┘
```
