# KMP Navigation System - Quick Reference Guide

**Purpose**: Fast lookup for architecture decisions and patterns  
**Status**: Ready to use during implementation

---

## Decision Matrix

### When to Use What?

| Scenario | Use | Why |
|----------|-----|-----|
| User navigates to new screen | `coordinator.navigateToScreen()` → Push to `primaryStack` | Normal stack-based navigation |
| Show filter options overlay | `coordinator.showModal()` → Add to `modalStack` | Modal shouldn't replace screen, independent lifecycle |
| Dismiss filter without navigating | `coordinator.dismissModal()` → Pop from `modalStack` | Modal stays separate from main back stack |
| App has multiple sections (Tabs) | `usesTabs = true`, each tab has own stack | Tabs need independent histories |
| User taps tab bar | `coordinator.selectTab(tabId)` | Switch tabs, preserve other tabs' stacks |
| Back button pressed | `coordinator.navigateBack()` | Dismiss modal if showing, else pop screen |
| Notification with app link | Deep link → `applyNavigationState()` | Reconstruct full navigation from URL |

### Architecture Pattern Choice

```
Simple App (No Tabs, No Modals)
└─ usesTabs = false
   └─ Single primaryStack
      └─ Linear navigation

App with Modals (No Tabs)
└─ usesTabs = false
   ├─ primaryStack (main screens)
   └─ modalStack (overlays)

Tabbed App
├─ usesTabs = true
└─ TabNavigationState
   ├─ Tab1Stack
   ├─ Tab2Stack  
   ├─ Tab3Stack
   └─ modalStack (can show modal in any tab)
```

---

## Code Patterns

### Pattern 1: Basic Screen Navigation

```kotlin
// Show a screen
coordinator.navigateToScreen(Destination.RestaurantDetail(restaurantId))

// Go back
coordinator.navigateBack()

// Return to home
coordinator.navigateToRoot()
```

### Pattern 2: Modal Overlay

```kotlin
// Show a modal
coordinator.showModal(ModalDestination.Filter(preSelectedFilters))

// Dismiss it
coordinator.dismissModal()

// Dismiss all modals
coordinator.dismissAllModals()
```

### Pattern 3: Handle Back Button

```kotlin
// In Android Compose:
BackHandler {
    coordinator.navigateBack()
}

// In iOS:
@Environment(\.dismiss) var dismiss
Button(action: { coordinator.navigateBack() }) {
    Image(systemName: "chevron.left")
}
```

### Pattern 4: Tab Navigation

```kotlin
// Switch tab
coordinator.selectTab("search")

// Navigate within current tab
coordinator.navigateInTab(Destination.SearchDetail(itemId))

// Go back in current tab
coordinator.backInTab()
```

### Pattern 5: Deep Link Handling

```kotlin
// Parse and apply deep link
val result = deepLinkParser.parse("munchies://restaurant/123")
when (result) {
    is DeepLinkResult.Success -> {
        coordinator.applyNavigationState(result.navigationState)
    }
    is DeepLinkResult.NotFound -> {
        coordinator.navigateToRoot()
    }
}
```

### Pattern 6: Feature Module - Route Handler

```kotlin
// In feature module
class RestaurantDetailRouteHandler : ScopedRouteHandler {
    override val route = RestaurantDetailRoute()
    
    override fun canHandle(destination: Destination): Boolean {
        return destination is Destination.RestaurantDetail
    }
    
    override fun destinationToRoute(destination: Destination): Route? {
        return (destination as? Destination.RestaurantDetail)?.let {
            RestaurantDetailRoute(it.restaurantId)
        }
    }
    
    override fun createScope(route: Route): Scope {
        val restaurantId = (route as RestaurantDetailRoute).restaurantId
        return createRestaurantDetailScope(restaurantId)
    }
}

// Register in feature's RouteProvider
class RestaurantRouteProvider : RouteProvider {
    override fun getRoutes(): List<Route> {
        return listOf(RestaurantDetailRouteHandler())
    }
}
```

### Pattern 7: Modal Route Handler

```kotlin
// In feature module
class FilterModalRouteHandler : ModalRouteHandler {
    override fun canHandleModal(destination: ModalDestination): Boolean {
        return destination is ModalDestination.Filter
    }
    
    override fun destinationToModalRoute(destination: ModalDestination): ModalRoute? {
        return (destination as? ModalDestination.Filter)?.let {
            FilterModalRoute(it.preSelectedFilters)
        }
    }
}
```

### Pattern 8: Composable that Navigates

```kotlin
@Composable
fun RestaurantCard(restaurantId: String, coordinator: NavigationCoordinator) {
    Card(
        modifier = Modifier.clickable {
            coordinator.navigateToRestaurantDetail(restaurantId)
        }
    ) {
        // Card content
    }
}
```

### Pattern 9: Listening to Navigation State

```kotlin
@Composable
fun MyScreen(coordinator: NavigationCoordinator) {
    val navigationState by coordinator.navigationState.collectAsState()
    
    // React to state changes
    LaunchedEffect(navigationState.currentStack.size) {
        println("Stack changed to: ${navigationState.currentStack}")
    }
}
```

### Pattern 10: Deep Link Handler

```kotlin
class RestaurantDeepLinkHandler : DeepLinkHandler {
    override fun canHandle(deepLink: String): Boolean {
        return deepLink.contains("restaurant")
    }
    
    override fun parseDeepLink(deepLink: String): DeepLinkResult {
        return try {
            val uri = Uri.parse(deepLink)
            val restaurantId = uri.pathSegments.getOrNull(1)
                ?: return DeepLinkResult.NotFound(deepLink)
            
            DeepLinkResult.Success(
                NavigationState(
                    primaryStack = listOf(
                        RestaurantListRoute(),
                        RestaurantDetailRoute(restaurantId)
                    )
                )
            )
        } catch (e: Exception) {
            DeepLinkResult.Error(deepLink, e)
        }
    }
}
```

---

## Data Model Relationships

```
NavigationState
├─ primaryStack: List<StackRoute>
├─ modalStack: List<ModalRoute>
├─ tabNavigation: TabNavigationState?
│  ├─ tabDefinitions: List<TabDefinition>
│  ├─ activeTabId: String
│  └─ stacksByTab: Map<String, List<StackRoute>>
└─ originDeepLink: String?

Route (interface)
├─ StackRoute (pushes to primaryStack or tab stack)
├─ ModalRoute (shows as overlay)
└─ TabRoute (contains other routes)

Destination (sealed class)
├─ RestaurantList
├─ RestaurantDetail(restaurantId)
└─ ... (other screen destinations)

ModalDestination (sealed class)
├─ Filter(preSelectedFilters)
├─ ConfirmAction(message)
├─ DatePicker(initialDate)
└─ ... (other modal destinations)
```

---

## State Transitions

### Simple Navigation Flow

```
User taps button
    ↓
navigateToScreen(Destination)
    ↓
NavigationEvent.Push emitted
    ↓
Platform layer calls NavigationReducer.reduce()
    ↓
NavigationState updated (primaryStack += newRoute)
    ↓
UI recomposes / SwiftUI updates
    ↓
New screen appears
```

### Modal + Screen Interaction

```
Screen visible, user taps "Filter"
    ↓
showModal(ModalDestination.Filter)
    ↓
NavigationEvent.ShowModal emitted
    ↓
NavigationReducer adds to modalStack
    ↓
ModalLayer composable renders modal
    ↓
User interacts with modal, applies filters
    ↓
dismissModal()
    ↓
NavigationReducer removes from modalStack
    ↓
Modal disappears, screen still visible below
```

### Tab Switching

```
User in Tab1 (DetailRoute stack)
    ↓
selectTab("Tab2")
    ↓
TabNavigationState.activeTabId = "Tab2"
    ↓
UI renders Tab2's stack (SearchRoute)
    ↓
User navigates: SearchRoute → DetailRoute
    ↓
Tab2's stack updated: [SearchRoute, DetailRoute]
    ↓
User switches to Tab1
    ↓
activeTabId = "Tab1"
    ↓
UI renders Tab1's stack (DetailRoute) ← preserved!
    ↓
User sees exactly where they were before switching
```

---

## Common Implementation Mistakes

### ❌ Mistake 1: Mixing Modal and Screen Logic

```kotlin
// WRONG: Treating modal like a screen
showModal(ModalDestination.Filter)
navigateBack() // User expects modal to dismiss, but this pops screen instead!
```

**Fix**: Back button should dismiss modal, not screen
```kotlin
fun navigateBack() {
    if (state.modalStack.isNotEmpty()) {
        dismissModal()
    } else {
        // Pop screen
    }
}
```

### ❌ Mistake 2: Not Preserving Tab State

```kotlin
// WRONG: Losing tab stack when navigating
selectTab("Tab2")
// Only keep current screen, lose history
```

**Fix**: Keep complete stack for each tab
```kotlin
tabNavigation.stacksByTab[tabId] = completeStack
selectTab("Tab2") // Tab1's stack is preserved in map
```

### ❌ Mistake 3: Deep Link Not Clearing Old State

```kotlin
// WRONG: Appending to existing stack
applyNavigationState(newState, clearCurrentStack = false)
// User sees confused navigation state
```

**Fix**: Clear old state for most deep links
```kotlin
applyNavigationState(newState, clearCurrentStack = true)
```

### ❌ Mistake 4: Not Handling Modal Back Button

```kotlin
// WRONG: Not checking if modal is showing
fun back() {
    navController.popBackStack() // Only handles screen, not modal!
}
```

**Fix**: Check modal stack first
```kotlin
fun back() {
    if (navigationState.hasModals) {
        dismissModal()
    } else {
        navigateBack()
    }
}
```

### ❌ Mistake 5: Creating Route Handlers without Feature Ownership

```kotlin
// WRONG: Everything in app layer
// app/navigation/AllRouteHandlers.kt - 5000+ lines

// RIGHT: Each feature owns its routes
// feature-restaurant/navigation/RestaurantRouteHandler.kt
// feature-profile/navigation/ProfileRouteHandler.kt
```

---

## Testing Checklist

### Reducer Tests (Unit)
- [ ] Push adds route to stack
- [ ] Pop removes route from stack
- [ ] Pop to root clears stack
- [ ] Show modal doesn't affect stack
- [ ] Dismiss modal pops from modal stack only
- [ ] Tab selection updates active tab
- [ ] Navigation in tab updates tab stack only

### Platform Tests (Integration)
- [ ] Modal appears when showModal called
- [ ] Modal dismisses when dismissModal called
- [ ] Back button in modal dismisses modal
- [ ] Back button in screen pops screen
- [ ] Switching tabs preserves history
- [ ] Modal works on top of any screen

### Deep Link Tests (Integration)
- [ ] Valid deep link navigates to correct screen
- [ ] Invalid deep link shows error/fallback
- [ ] Deep link with modal shows modal on top
- [ ] Deep link with tabs switches to correct tab

---

## Performance Considerations

### Memory Usage by Component

| Component | Impact | Notes |
|-----------|--------|-------|
| Each StackRoute | ~10KB | Small data classes |
| Each ModalRoute | ~5KB | Even smaller, minimal state |
| Modal ViewModel | ~50-200KB | Depends on feature |
| Koin Scope | ~100KB | Holds dependencies for one route |
| Per-Tab Stack | N/A | Stored as List<Route>, very efficient |

### Optimization Opportunities

```
Problematic
├─ Having 10+ screens in back stack
│  └─ Solution: Limit stack depth, or pop intermediate screens
├─ Creating all tab content at once
│  └─ Solution: Lazy-load tab content
└─ Not cleaning up old routes' scopes
   └─ Solution: RouteRegistry.cleanup() called automatically

Good Patterns
├─ Route handlers in features (isolated compilation)
├─ Pure reducers (no I/O, fast)
├─ StateFlow (efficient observable)
└─ Lazy modal rendering (only top modal visible)
```

---

## Troubleshooting Guide

### Issue: Modal doesn't appear

**Check**:
1. Is `showModal()` being called?
2. Is `ModalLayer` composable included in your UI?
3. Is the modal route handler registered?

**Debug**:
```kotlin
println("Modal stack: ${navigationState.modalStack}")
println("Top modal: ${navigationState.topModal}")
```

### Issue: Back button doesn't dismiss modal

**Check**:
1. Back handler checking `state.modalStack.isEmpty()`?
2. Call `dismissModal()` or `navigateBack()`?

**Fix**:
```kotlin
BackHandler {
    if (navigationState.hasModals) {
        coordinator.dismissModal()
    } else {
        coordinator.navigateBack()
    }
}
```

### Issue: Tab history lost when switching

**Check**:
1. Is `stacksByTab` map being updated correctly?
2. Is reducer calling `updateActiveTabStack()`?
3. Is each tab getting its own List<StackRoute>?

**Debug**:
```kotlin
tabNavigation?.stacksByTab?.forEach { (tabId, stack) ->
    println("Tab $tabId: ${stack.map { it.key }}")
}
```

### Issue: Deep link not navigating to correct screen

**Check**:
1. Is `DeepLinkParser` finding the right handler?
2. Is handler parsing URL correctly?
3. Is `applyNavigationState()` being called?

**Debug**:
```kotlin
val result = parser.parse(deepLink)
println("Parse result: $result")
when (result) {
    is DeepLinkResult.Success -> println("Routes: ${result.navigationState.primaryStack}")
    is DeepLinkResult.NotFound -> println("Handler not found")
    is DeepLinkResult.Error -> println("Error: ${result.exception}")
}
```

### Issue: High memory usage with tabs

**Check**:
1. How many screens per tab?
2. Are routes holding large objects?
3. Are ViewModels being cleaned up?

**Optimize**:
```kotlin
// Limit stack depth
val MAX_STACK_SIZE = 10
if (newStack.size > MAX_STACK_SIZE) {
    // Pop intermediate screens
}

// Or: Clear old stacks when not needed
if (dontNeedTabHistory) {
    stacksByTab[tabId] = listOf(rootRoute)
}
```

---

## Quick Wins - Low Effort, High Value

1. **Add modal support first** (1-2 weeks)
   - Extend existing infrastructure
   - Minimal breaking changes
   - Immediate UX improvement

2. **Implement deep link handler base** (1 week)
   - Just the interface and parser registry
   - Features add handlers incrementally
   - Start with 1-2 links

3. **Add "max stack depth" validation** (1 day)
   - Prevents accidental deep navigation
   - Tests navigation logic

4. **Implement navigation logging** (1 day)
   - Log every state change
   - Helps debugging
   - Great for QA

5. **Add navigation state persistence** (1-2 days)
   - Save state to DataStore/Preferences
   - Restore on app restart
   - Improves perceived app quality

---

## Integration with Existing Code

### Update RouteHandler Interface

```kotlin
// core/navigation/RouteHandler.kt
interface RouteHandler {
    val route: Route
    fun toRouteString(): String
    fun canHandle(destination: Destination): Boolean
    fun destinationToRoute(destination: Destination): Route?
    
    // NEW: Optional methods for modal support
    fun canHandleModal(destination: ModalDestination): Boolean = false
    fun destinationToModalRoute(destination: ModalDestination): ModalRoute? = null
}

// Backward compatible! Existing handlers don't change
```

### Update Koin Setup

```kotlin
// core/di/KoinModule.kt
fun coreModule() = module {
    // Existing
    single { AppCoordinator() }
    
    // NEW: NavigationCoordinator (can be alias to AppCoordinator for migration)
    single { NavigationCoordinator() }
    
    // NEW: Reducer (pure, no dependencies)
    single { NavigationReducer }
    
    // NEW: Deep link parser
    single {
        DeepLinkParser(getAll<DeepLinkHandler>())
    }
    
    // Features contribute handlers
    includes(featureRestaurantModule)
}
```

---

## Key Files to Create/Modify

### Phase 0: Foundation

**Create** (new files):
- `core/navigation/NavigationState.kt`
- `core/navigation/NavigationReducer.kt`
- `core/navigation/TabNavigationState.kt`
- `core/navigation/ModalDestination.kt`
- `core/navigation/deeplink/DeepLink.kt`
- `core/navigation/deeplink/DeepLinkHandler.kt`
- `core/navigation/deeplink/DeepLinkParser.kt`

**Modify** (extend):
- `core/navigation/NavigationEvent.kt` (add modal/tab events)
- `core/navigation/AppCoordinator.kt` → rename to `NavigationCoordinator`
- `core/navigation/RouteHandler.kt` (add optional modal methods)

**Platform-Specific** (new):
- `androidApp/navigation/modals/ModalLayer.kt`
- `androidApp/navigation/deeplink/DeepLinkProcessor.kt`
- `iosApp/Navigation/ModalPresentation.swift`
- `iosApp/Navigation/DeepLinkProcessor.swift`

### Phase 1: Modals

**Create**:
- Feature-specific modal routes + handlers
- Feature-specific modal composables/views

### Phase 2: Tabs

**Create**:
- `androidApp/navigation/tabs/TabNavigation.kt`
- `iosApp/Navigation/TabNavigationView.swift`

### Phase 3: Deep Links

**Create**:
- Per-feature `DeepLinkHandler` implementations
- Tests for deep link parsing

---

## Success Criteria

✅ **Phase 0 Complete When**:
- All new data classes compile
- NavigationReducer has >90% test coverage
- Existing navigation still works unchanged
- No warnings or errors in codebase

✅ **Phase 1 (Modals) Complete When**:
- First modal displays and dismisses
- Back button works correctly with modal
- Modal memory cleaned up on dismiss
- Unit tests pass for modal reducer

✅ **Phase 2 (Tabs) Complete When**:
- Tab switching works
- Each tab preserves history
- Modal works on top of tabs
- Back button works in tabs

✅ **Phase 3 (Deep Links) Complete When**:
- Deep link parsing works for main flows
- Invalid links handled gracefully
- Notifications can open via deep links
- Web links work (iOS Universal Links, Android App Links)

---

## Further Reading

- Review `NAVIGATION_SYSTEM_ARCHITECTURE.md` for detailed architecture
- Check `NAVIGATION_IMPLEMENTATION_EXAMPLES.md` for copy-paste code
- Reference existing files: `AppNavigation.kt`, `RouteHandler.kt`, `Destination.kt`

---

## Questions? Discuss

Key decision points to align on with your team:

1. **Modal presentation**: Always ModalBottomSheet, or support multiple styles?
2. **Tab support timeline**: Implement now, or when needed?
3. **Deep link scope**: Start with restaurants only, or all features?
4. **State persistence**: Restore nav state on app restart?
5. **Back button handling**: Consistent across both platforms?

