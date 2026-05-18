# KMM Navigation: Architecture Patterns & Diagrams

**Document**: Visual architecture guide and pattern reference  
**Purpose**: Quick reference for understanding and extending navigation system

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
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationReducer (Pure State Machine)                      │ │
│  │  - (State, Event) → State (deterministic)                   │ │
│  │  - Route resolution via handlers                            │ │
│  │  - No side effects                                          │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationState (Observable State)                          │ │
│  │  - TabNavigationState (per-tab stacks)                      │ │
│  │  - ModalStack (overlay routes)                              │ │
│  │  - NavigationDirection (animation hint)                     │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  Specialized Handlers                                        │ │
│  │  - RouteHandler (Destination → Route)                       │ │
│  │  - ScopedRouteHandler (+ scope lifecycle)                   │ │
│  │  - DeepLinkHandler (URL → Navigation State)                 │ │
│  └──────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                 ↑
                    Observes and reduces events
                                 ↑
┌─────────────────────────────────────────────────────────────────────┐
│                     PERSISTENCE LAYER (KMM)                        │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationStateRestorer                                     │ │
│  │  - Loads snapshot from storage                              │ │
│  │  - Validates state structure                                │ │
│  │  - Falls back to default state                              │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationStateSnapshot (Serializable)                      │ │
│  │  - toSnapshot() / toNavigationState() converters            │ │
│  │  - Tab stacks, modal stacks, metadata                       │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  NavigationPersistenceStore (Interface)                      │ │
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

## State Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Navigation Event                         │
│                    (Push, Pop, ShowModal, etc)                  │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│                  AppCoordinator.dispatch()                      │
│              (emits event to _navigationEvents)                 │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│              AppCoordinator.reduceState()                       │
│                                                                 │
│  1. Get current state from _navigationState                     │
│  2. Call NavigationReducer.reduce(state, event)                │
│  3. Create/destroy Koin scopes via NavigationEffects           │
│  4. Update _navigationState with new state                     │
│  5. Trigger async persistence                                  │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│           NavigationReducer.reduce() [PURE]                    │
│                                                                 │
│  Input:  (state, event, routeHandlers)                         │
│  Output: newState                                              │
│                                                                 │
│  Switch on event type:                                         │
│  - Push: append route to active tab stack                      │
│  - Pop: remove route from active tab stack                     │
│  - ShowModal: append to modal stack                            │
│  - DismissModal: remove from modal stack                       │
│  - SelectTab: change activeTabId                              │
│  - ApplyNavigationState: return new state entirely            │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│        NavigationState (Observable via StateFlow)              │
│                                                                 │
│  ┌─────────────────────┐                                        │
│  │ TabNavigationState  │                                        │
│  │ ┌────────────────┐  │                                        │
│  │ │ activeTabId    │  │                                        │
│  │ ├────────────────┤  │                                        │
│  │ │ stacksByTab    │  │  Map<TabId, List<Route>>              │
│  │ │ - restaurants: │  │  [RestaurantListRoute, DetailRoute]  │
│  │ │ - settings:    │  │  [SettingsRoute]                     │
│  │ └────────────────┘  │                                        │
│  └─────────────────────┘                                        │
│                                                                 │
│  ┌──────────────────────┐                                       │
│  │ modalStack           │  List<ModalRoute>                    │
│  │ [FilterModalRoute]   │                                       │
│  └──────────────────────┘                                       │
└──────────────────────────────┬──────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────┐
│           UI Layer (Android/iOS)                                │
│                                                                 │
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
               │ selectTab("restaurants")  (return to restaurants)
               ↓
        ┌──────────────────────────────┐
        │  Restaurants                 │
        │  - ListRoute                 │
        │  → DetailRoute               │  (same state as before!)
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
   │ ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲│
   └────────────────────────────────────┘
           │ dismissAllModals()
           ↓
   ┌────────────────────────────────────┐
   │ Screen: RestaurantDetailRoute       │
   │ Modal:  (none)                     │
   └────────────────────────────────────┘
```

---

## Scope Lifecycle Diagram

```
┌─────────────────────────────────────────┐
│ Navigation Event: Push(DetailRoute(123))│
└──────────────┬──────────────────────────┘
               ↓
    NavigationReducer.reduce()
    Creates: Route = DetailRoute(123)
               ↓
    ┌──────────────────────────────────────┐
    │ Route created with key: detail_123   │
    └──────────────────────────────────────┘
               ↓
    AppCoordinator.reduceState()
    Detects: Route added to navigation
               ↓
    ┌──────────────────────────────────────┐
    │ Find handler that can handle          │
    │ Destination.RestaurantDetail(123)     │
    └──────────────┬───────────────────────┘
                   ↓
    ┌──────────────────────────────────────┐
    │ ScopedRouteHandler.createScope()      │
    │ - scopeId = "detail_123"              │
    │ - Creates Koin scope with qualifier   │
    │ - Registers ViewModel factory         │
    │ - ViewModel created and cached        │
    └──────────────┬───────────────────────┘
                   ↓
    ┌──────────────────────────────────────┐
    │ Scope persists in Koin container     │
    │ (until explicitly closed)             │
    └──────────────────────────────────────┘
                   ↓
    UI Layer: remember(route.key) {
        getKoin().getScope(route.key).get<ViewModel>()
    }
    ↓
    Same ViewModel returned across recompositions
    (survives config changes)

─────────────────────────────────────────

    Navigation Event: Pop
    Route removed from navigation
               ↓
    AppCoordinator.reduceState()
    Detects: Route removed
               ↓
    NavigationEffects.handleNavigationSideEffects()
    Finds: detail_123 scope no longer in state
               ↓
    Closes scope: scope.close()
    - ViewModel cleanup called
    - Resources released
    - Scope removed from Koin
```

---

## Event Dispatch Sequence

```
User Action: Taps "Restaurant Detail" in list

    UI Layer (Compose)
    └─ RestaurantListScreen
       └─ Button(onClick = { ... })
          └─ navigateToRestaurantDetail("123")
             ↓
    AppCoordinator.navigateToRestaurantDetail("123")
    └─ navigateToScreen(Destination.RestaurantDetail("123"))
       ↓
    AppCoordinator.dispatch(NavigationEvent.Push(destination))
    └─ Emits to _navigationEvents (StateFlow)
    └─ Calls reduceState(event)
       ↓
    AppCoordinator.reduceState(event)
    ├─ Get current state
    ├─ Call NavigationReducer.reduce()
    │  └─ Resolve Destination → Route
    │  └─ Create new state with route added
    ├─ Create Koin scopes for new routes
    ├─ Close Koin scopes for removed routes
    ├─ Update _navigationState (StateFlow)
    └─ Persist state async
       ↓
    UI Layer (Compose)
    ├─ navigationState.collectAsState() triggered
    ├─ RenderTabContent() recomposes
    │  └─ AnimatedContent(targetState = newRoute)
    │     └─ Renders RestaurantDetailScreen
    ├─ Scope retrieved: getKoin().getScope(route.key)
    └─ ViewModel created/retrieved from scope
       ↓
    Screen Rendered with Latest State
```

---

## Deep Link Processing Flow

```
Browser/Notification
└─ "munchies://restaurants/123/reviews"
   ↓
Android: Intent.ACTION_VIEW
iOS: UISceneDelegate.scene(_:openURLContexts:)
   ↓
AppNavigation/NavigationController
└─ processPendingDeepLink(uri)
   ↓
DeepLinkParser.parse(deepLink)
├─ Find handler where canHandle(deepLink) == true
├─ Call handler.parseDeepLink(deepLink)
└─ Return DeepLinkResult
   ├─ navigationState (full state to apply)
   ├─ strategy (how to apply it)
   ├─ isValid (was parsing successful)
   ↓
DeepLinkResult enum:
├─ Success: navigationState + clearCurrentStack
├─ Partial: navigationState (incomplete) + strategy
├─ NotFound: No handler matched
└─ Error: Handler threw exception
   ↓
AppCoordinator.applyDeepLink(result)
├─ Apply navigation state
├─ Create necessary scopes
└─ UI renders new state
   ↓
User sees: RestaurantDetail → ReviewsScreen
(from deep link, starting from scratch)
```

---

## Crash Recovery Flow

```
APP RUNNING
│
├─ User navigates: RestaurantList → Detail → Modal
│
├─ persistNavigationStateAsync() saves state
│  └─ Snapshot written to DataStore/UserDefaults
│
└─ USER FORCE-QUITS APP (crash)


APP RESTART
│
├─ MainActivity.onCreate() / AppDelegate.didFinishLaunching
│
├─ NavigationStateRestorer.restoreNavigationState()
│  ├─ Load snapshot from persistence store
│  ├─ Deserialize to NavigationStateSnapshot
│  ├─ Validate snapshot structure
│  └─ Convert to NavigationState
│
├─ Check crash indicator flag
│  └─ Mark restoredFromCrash = true
│
├─ AppCoordinator.applyNavigationState(restoredState)
│
├─ Create Koin scopes for all routes in state
│  ├─ DetailRoute scope
│  └─ ModalRoute scope
│
├─ UI renders from restored state
│  └─ Same screens, modals as before crash
│
└─ USER SEES: App recovered exactly to previous state ✓
```

---

## Serialization Architecture

```
NavigationState (in-memory)
│
├─ Contains:
│  ├─ TabNavigationState
│  │  ├─ tabDefinitions: List<TabDefinition>
│  │  ├─ activeTabId: String
│  │  └─ stacksByTab: Map<String, List<Route>>
│  │
│  ├─ modalStack: List<ModalRoute>
│  │
│  └─ originDeepLink: String?
│
└─ Not serializable because:
   └─ May contain lambdas, non-serializable objects
      ↓
      
CONVERT via toSnapshot()
      ↓

NavigationStateSnapshot (@Serializable)
│
├─ Contains same data as NavigationState
├─ All fields are serializable primitives
├─ Uses kotlinx.serialization.Serializable
│
└─ Registered routes in SerializersModule:
   ├─ RestaurantListRoute
   ├─ RestaurantDetailRoute
   ├─ SettingsRoute
   ├─ FilterModalRoute
   ├─ ConfirmActionModalRoute
   └─ (all route subclasses)
      ↓

JSON String
(via navigationJson instance)
      ↓
      
Write to Disk
├─ Android: DataStore → /data/.../.../prefs.pb
└─ iOS: UserDefaults → ~/Library/Preferences/...
      ↓

APP RESTART
      ↓

Read from Disk
      ↓

Deserialize JSON
(via navigationJson instance)
      ↓

NavigationStateSnapshot
      ↓

CONVERT via toNavigationState()
      ↓

NavigationState
      ↓

Apply to UI
```

---

## Platform Integration Points

### Android Platform

```
┌─────────────────────────────────────┐
│ androidApp/                         │
└─────────────────────────────────────┘
         ↓
┌─────────────────────────────────────┐
│ MainActivity.onCreate()              │
│                                     │
│ 1. Initialize AppCoordinator        │
│ 2. Restore navigation state         │
│ 3. Set content to @Composable       │
│    AppNavigation()                  │
└─────────────────────────────────────┘
         ↓
┌─────────────────────────────────────┐
│ AppNavigation.kt (Composable)       │
│                                     │
│ 1. Collect navigationState          │
│ 2. Process pending deep link        │
│ 3. Render TabNavigationScaffold     │
│ 4. Render RenderTabContent()        │
│    (with animations)                │
│ 5. Render RenderModalsIfNeeded()    │
└─────────────────────────────────────┘
         ↓
┌─────────────────────────────────────┐
│ Platform-Specific Storage           │
│                                     │
│ AndroidNavigationStore              │
│ ├─ DataStore<Preferences>           │
│ ├─ Navigation state proto buffer    │
│ └─ Async read/write                 │
└─────────────────────────────────────┘
```

### iOS Platform

```
┌─────────────────────────────────────┐
│ iosApp/                             │
└─────────────────────────────────────┘
         ↓
┌─────────────────────────────────────┐
│ AppDelegate / App (SwiftUI @main)   │
│                                     │
│ 1. Initialize AppCoordinator        │
│ 2. Restore navigation state         │
│ 3. Create @StateObject              │
│    navigationObserver               │
│ 4. Create NavigationView            │
└─────────────────────────────────────┘
         ↓
┌─────────────────────────────────────┐
│ NavigationController.swift          │
│ (Observes navigationState)          │
│                                     │
│ @StateObject navigationState:       │
│   coordinator.navigationState       │
│   .toObservable()                   │
│                                     │
│ NavigationStack(@binding) {         │
│   RenderScreen(navigationState)     │
│   .sheet(isPresented:...) {         │
│     RenderModal(navigationState)    │
│   }                                 │
│ }                                   │
└─────────────────────────────────────┘
         ↓
┌─────────────────────────────────────┐
│ Platform-Specific Storage           │
│                                     │
│ IosUserDefaultsPersistence          │
│ ├─ NSUserDefaults.standardDefaults  │
│ ├─ Navigation state as JSON         │
│ └─ Async read/write                 │
└─────────────────────────────────────┘
```

---

## Error Recovery Strategies

```
┌─────────────────────────────────────────────┐
│        Serialization Failure                │
│  (Route type not registered)                │
└──────────────────┬──────────────────────────┘
                   ↓
        Catch SerializationException
                   ↓
    ┌───────────────────────────────┐
    │ Log error with details        │
    │ Load default state instead    │
    │ User sees app in safe state   │
    └───────────────────────────────┘


┌─────────────────────────────────────────────┐
│        Corrupted Snapshot File              │
│  (Disk read error)                          │
└──────────────────┬──────────────────────────┘
                   ↓
        Catch IOException
                   ↓
    ┌───────────────────────────────┐
    │ Log error                     │
    │ Clear corrupted file          │
    │ Load default state            │
    │ Create new snapshot next time │
    └───────────────────────────────┘


┌─────────────────────────────────────────────┐
│        Validation Failure                   │
│  (Snapshot has missing tabs/stacks)         │
└──────────────────┬──────────────────────────┘
                   ↓
        isValidSnapshot() returns false
                   ↓
    ┌───────────────────────────────┐
    │ Snapshot is stale/corrupted   │
    │ Load default state            │
    │ Keep old snapshot (for debug) │
    └───────────────────────────────┘
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
    
    assert(newState != state)  // New object
    assert(newState.currentStack.size == 2)
    assert(newState.currentStack.last() is DetailRoute)
}
```

### Integration Test Pattern
```kotlin
// State restoration testing
@Test
fun testStateRestoration() {
    val snapshot = state.toSnapshot()
    persistenceStore.save(snapshot)
    
    val restored = restorer.restore()
    assertEquals(state, restored)
}
```

### Platform Test Pattern
```kotlin
// Android config change survival
@Test
fun testViewModelSurvivesRotation() {
    val vm1 = getViewModelFromScope(scopeId)
    triggerRecomposition()  // Simulate rotation
    val vm2 = getViewModelFromScope(scopeId)
    
    assert(vm1 === vm2)  // Same object
}
```

---

