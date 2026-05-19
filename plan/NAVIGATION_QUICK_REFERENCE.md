# Navigation Quick Reference

**Location:** `/plan/NAVIGATION_ARCHITECTURE_GUIDE.md` (Full Guide)

---

## Current Navigation System Capabilities

✅ **Deep Link Handling** - Full support for cold/warm start deep links  
✅ **Navigation Analytics** - Tracks screen transitions, tab switches, modal open/close with time tracking  
✅ **Shared View Models** - Feature-scoped navigation VMs trigger navigation from screens  
✅ **Tab Navigation** - Independent stacks per tab with state preservation  
✅ **Modal System** - Multiple modal destinations with conditional dismissal  
✅ **Redux Pattern** - Pure state transformations, predictable behavior  
✅ **Platform Independent** - Identical logic on Android & iOS  
✅ **State Persistence** - Written after every event; restored **only** on crash/config-change (never on clean exit)

---

## System Flow

```
┌─ User Action ─┐
│ (Tap Button)  │
└────────┬──────┘
         │
         ▼
┌──────────────────────────────────────┐
│ NavigationViewModel (Feature-scoped)  │
│ .showRestaurantDetail()               │
└────────┬──────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────┐
│ NavigationDispatcher (Generic)        │
│ .navigate(Destination.Restaurant)    │
└────────┬──────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────┐
│ AppCoordinator                       │
│ .dispatch(NavigationEvent.Push)      │
└────────┬──────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────┐
│ NavigationReducer (Pure Function)    │
│ (State, Event) → NewState            │
└────────┬──────────────────────────────┘
         │
    ┌────┴────┬─────────┬──────────┐
    │          │         │          │
    ▼          ▼         ▼          ▼
  State       UI       Scope    Analytics
  Updated    Renders  Created   Tracked
    │
    ▼
  Persist state async (always)
  ← Restoration on next launch only if:
    crash / OS kill / config change
```

---

## Core Components

| Component | File | Purpose |
|-----------|------|---------|
| **AppCoordinator** | `AppCoordinator.kt` | Central hub; manages state, dispatches events |
| **NavigationState** | `NavigationState.kt` | Immutable state tree (tabs, modals, routes) |
| **NavigationEvent** | `NavigationEvent.kt` | Sealed class: Push, Pop, ShowModal, SelectTab, etc. |
| **NavigationReducer** | `NavigationReducer.kt` | Pure functions: (State, Event) → NewState |
| **Destination** | `Destination.kt` | Type-safe sealed class for screens |
| **Route** | `Routes.kt` | Serializable route models (for persistence) |
| **NavigationDispatcher** | `NavigationDispatcher.kt` | Abstraction layer for feature VMs |
| **DeepLinkHandler** | `DeepLinkHandler.kt` | Interface: each feature implements its own |
| **DeepLinkProcessor** | `DeepLinkProcessor.kt` | Routes deep links to coordinator methods |
| **NavigationAnalyticsListener** | `NavigationAnalyticsListener.kt` | Observer: tracks state changes → analytics |

---

## Navigation Methods

### Screen Navigation
```kotlin
// Navigate to screen
coordinator.navigateToScreen(Destination.RestaurantList)
coordinator.navigateToRestaurantDetail("123")

// Go back / pop
coordinator.navigateBack()
coordinator.navigateToRoot()
```

### Modal Navigation
```kotlin
// Show modals
coordinator.showModal(ModalDestination.Filter(preSelectedFilters))
coordinator.showFilterModal(listOf("tag1"))
coordinator.submitReview("123")
coordinator.showConfirmation("Delete?")

// Dismiss modals
coordinator.dismissModal()
coordinator.dismissAllModals()
coordinator.dismissModalUntil { it is ConfirmActionRoute }
```

### Tab Navigation
```kotlin
// Switch tabs
coordinator.selectTab("settings")

// Navigate within tab
coordinator.navigateInTab(Destination.RestaurantDetail("123"))
coordinator.backInTab()
```

### Deep Links
```kotlin
// Apply deep link
coordinator.applyDeepLink("munchies://restaurants/123")
coordinator.applyDeepLink("munchies://modal/filter?filters=tag1,tag2")

// Manual state application
coordinator.applyNavigationState(navigationState, clearCurrentStack = true)
```

---

## Deep Link Examples

| Deep Link | Behavior |
|-----------|----------|
| `munchies://restaurants` | Navigate to restaurant list |
| `munchies://restaurants/123` | Navigate to restaurant detail (ID: 123) |
| `munchies://settings` | Switch to settings tab |
| `munchies://modal/filter?filters=tag1,tag2` | Show filter modal with pre-selected filters |
| `munchies://modal/submit_review/123` | Show review submission modal for restaurant 123 |
| `munchies://modal/confirm?message=Delete?` | Show confirmation dialog |
| `munchies://modal/date_picker?initialDate=2026-05-18` | Show date picker |

---

## Analytics Tracking

### Events Tracked

| Event | Triggered When | Data Captured |
|-------|---|---|
| `ScreenView` | Screen changes | Screen name, class, previous screen, properties |
| `TabSwitch` | User switches tab | Tab ID, tab name |
| `ModalOpen` | Modal is presented | Modal name, class, properties |
| `ModalDismiss` | Modal is dismissed | Modal name, time spent in modal |

### Example: Complete Flow

```
User taps restaurant card
    ↓
RestaurantNavigationViewModel.showRestaurantDetail("123")
    ↓
NavigationDispatcher.navigate(Destination.RestaurantDetail("123"))
    ↓
AppCoordinator.dispatch(NavigationEvent.Push(...))
    ↓
NavigationReducer creates new state
    ↓
NavigationAnalyticsListener detects state change
    ↓
Emits: AnalyticsEvent.ScreenView(
    screenName = "RestaurantDetail_123",
    previousScreen = "RestaurantList",
    properties = {"restaurant_id": "123"}
)
    ↓
Analytics service sends to Firebase/backend
```

---

## Key Design Patterns

### 1. Redux Pattern
- **Unidirectional data flow:** Action → Event → Reducer → State → UI
- **Immutable state:** All state objects are data classes with copy()
- **Pure functions:** Reducers have no side effects

### 2. Observer Pattern (Analytics)
- **Decoupled:** Analytics listener observes state independently
- **No coupling:** AppCoordinator unaware of analytics
- **Thread-safe:** Listener uses coroutine for state collection

### 3. Feature-Scoped View Models
- **Type-safe:** Methods have specific signatures (e.g., `showRestaurantDetail(String)`)
- **Discoverable:** IDE autocomplete shows available options
- **Testable:** Easy to mock for unit tests

### 4. Deep Link Handler Chain
- **Extensible:** New features add their own handlers
- **Pluggable:** Handlers registered at init time
- **Composable:** Parser tries each handler until success

---

## How to Add New Navigation

### Step 1: Define Destination & Route

```kotlin
// In core navigation module
sealed class Destination {
    data class ReviewDetail(val reviewId: String) : Destination()
}

@Serializable
data class ReviewDetailRoute(val reviewId: String) : Route() {
    override val key: String = "${KEY_PREFIX}$reviewId"
    // ...
}
```

### Step 2: Add Reducer Handler

```kotlin
// In NavigationReducer.kt
private fun handlePush(...): NavigationState {
    val route = resolveRoute(event.destination, handlers)
    // Handle ReviewDetailRoute...
}
```

### Step 3: Create Feature Navigation ViewModel

```kotlin
class ReviewNavigationViewModel(
    private val dispatcher: NavigationDispatcher
) : LifecycleOwner() {
    fun showReviewDetail(reviewId: String) {
        dispatcher.navigate(Destination.ReviewDetail(reviewId))
    }
}
```

### Step 4: Use in Screens

```kotlin
@Composable
fun ReviewListScreen(navVM: ReviewNavigationViewModel) {
    Button(
        onClick = { navVM.showReviewDetail("123") }
    ) {
        Text("View Review")
    }
}
```

---

## File Locations

```
core/
  src/commonMain/kotlin/io/umain/munchies/
    ├── navigation/
    │   ├── AppCoordinator.kt                    # Central coordinator
    │   ├── NavigationState.kt                   # State model
    │   ├── NavigationEvent.kt                   # Event definitions
    │   ├── NavigationReducer.kt                 # State reduction
    │   ├── NavigationEffects.kt                 # Side effects
    │   ├── Destination.kt                       # Screen destinations
    │   ├── Routes.kt                            # Route models
    │   ├── TabNavigationState.kt                # Tab state
    │   ├── ModalRoutes.kt                       # Modal routes
    │   ├── DeepLinkHandler.kt                   # Interface
    │   ├── DeepLinkProcessor.kt                 # Processor
    │   ├── DeepLinkParser.kt                    # Parser
    │   ├── DeepLinkConstants.kt                 # Constants
    │   ├── RouteHandler.kt                      # Route interface
    │   ├── ScopedRouteHandler.kt                # Scoped handler
    │   ├── RouteProvider.kt                     # Provider
    │   ├── RouteRegistry.kt                     # Registry
    │   ├── RouteNavigationMapper.kt             # Mapper
    │   ├── RouteConstants.kt                    # Route constants
    │   └── persistence/
│       ├── NavigationStateRestorer.kt       # State restoration (gated by detector)
│       └── NavigationPersistenceStore.kt    # Persistence interface (always-write, gated-read)
│   ├── restoration/
│   │   └── RestoreConditionDetector.kt      # Restoration gate (crash vs. clean exit)
    ├── core/
    │   ├── navigation/
    │   │   └── NavigationDispatcher.kt          # Navigation abstraction
    │   └── analytics/
    │       └── NavigationAnalyticsListener.kt   # Analytics observer
    └── logging/

feature-restaurant/
  src/commonMain/kotlin/io/umain/munchies/feature/restaurant/
    └── navigation/
        ├── RestaurantNavigationViewModel.kt     # Feature navigation VM
        ├── RestaurantDeepLinkHandler.kt         # Deep link handler
        └── ReviewsDeepLinkHandler.kt            # Reviews handler

androidApp/
  src/main/kotlin/io/umain/munchies/android/
    └── navigation/
        ├── AndroidRestoreConditionDetector.kt   # Bundle-based detector
        └── MainActivity.kt                      # onSaveInstanceState + onDestroy cleanup

core/src/iosMain/kotlin/io/umain/munchies/
    └── navigation/
        └── IosRestoreConditionDetector.kt       # Clean-exit-flag detector
```

---

## Testing Navigation

### Unit Tests
- **NavigationReducerTest.kt** - Test state transformations
- **DeepLinkProcessorTest.kt** - Test deep link parsing
- **NavigationAnalyticsListenerTest.kt** - Test analytics events
- **NavigationRestorationTest.kt** - Test both crash-restore and clean-exit-no-restore paths

### Example Test — Navigation Reducer

```kotlin
@Test
fun testPushNavigationEvent() {
    val state = NavigationState(/* initial */)
    val event = NavigationEvent.Push(Destination.RestaurantDetail("123"))
    
    val newState = NavigationReducer.reduce(state, event)
    
    assert(newState.tabNavigation.stacksByTab["restaurants"]?.size == 2)
}
```

### Example Tests — Restoration Gate

```kotlin
// Crash → restore
@Test
fun testRestorationAfterCrash() = runTest {
    store.saveNavigationState(savedState.toSnapshot())
    // No markCleanExit() → simulates crash

    val restored = restorer.restoreNavigationState(IosRestoreConditionDetector(store))
    assertEquals(savedState, restored)
}

// Clean exit → fresh start
@Test
fun testFreshStartAfterCleanExit() = runTest {
    store.saveNavigationState(savedState.toSnapshot())
    store.markCleanExit()

    val restored = restorer.restoreNavigationState(IosRestoreConditionDetector(store))
    assertEquals(defaultState, restored)  // Not the saved state
}
```

---

## Debugging Tips

### Enable Logging
Navigation includes detailed logging in `AppCoordinator`:

```kotlin
logInfo("AppCoordinator", "🔄 reduceState: Event=${event::class.simpleName}")
logInfo("AppCoordinator", "✅ State updated and emitted via StateFlow")
```

### Check State
```kotlin
val currentState = coordinator.getCurrentState()
println("Active Tab: ${currentState.tabNavigation.activeTabId}")
println("Current Route: ${getRouteKey(currentState)}")
println("Modal Stack Size: ${currentState.modalStack.size}")
```

### Test Deep Links
```kotlin
// Trigger deep link directly
coordinator.applyDeepLink("munchies://restaurants/123")

// Check resulting state
val state = coordinator.getCurrentState()
assert(state contains RestaurantDetailRoute("123"))
```

---

## Related Documentation

- **Full Guide:** `plan/NAVIGATION_ARCHITECTURE_GUIDE.md`
- **Navigation Restoration:** `plan/navigation_restoration/KMM_NAVIGATION_RESTORATION_ANALYSIS.md`
- **Type Export Refactor:** `plan/type_export_refactor/`

---

**Last Updated:** May 19, 2026
