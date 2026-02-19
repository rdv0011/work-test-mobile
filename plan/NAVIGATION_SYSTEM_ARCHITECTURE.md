# KMP Navigation System Architecture Plan
## Supporting Modals, Tabs, and Deep Links

**Document Version**: 1.0  
**Date**: February 2026  
**Target Platforms**: Android (Jetpack Compose), iOS (SwiftUI)  
**Status**: Ready for Implementation

---

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Current State Assessment](#current-state-assessment)
3. [Architecture Design](#architecture-design)
4. [Implementation Approach](#implementation-approach)
5. [Feature-Specific Guidance](#feature-specific-guidance)
6. [Code Structure Examples](#code-structure-examples)
7. [Integration Considerations](#integration-considerations)
8. [Migration Path](#migration-path)
9. [Performance & Best Practices](#performance--best-practices)
10. [Implementation Checklist](#implementation-checklist)

---

## Executive Summary

Your current navigation system uses a **clean, feature-based architecture** with excellent separation of concerns through `RouteHandler` and `RouteNavigationMapper` abstractions. This plan extends your system to support:

1. **Modal Dialogs** - Overlay navigation without replacing the stack
2. **Tab Navigation** - Parallel stacks with independent back histories
3. **Deep Links** - External URL → Navigation State reconstruction

**Key Decisions**:
- ✅ Keep your existing core abstractions (they're solid!)
- ✅ Extend `Route` hierarchy to support modal and tab variants
- ✅ Create new `ModalStack` and `TabStack` primitives
- ✅ Implement deep link parsing at the app layer
- ✅ Use platform-specific presentation for overlays (sheets vs modals)

**Expected Timeline**: 
- Phase 1 (Modals): 1-2 weeks
- Phase 2 (Tabs): 2-3 weeks  
- Phase 3 (Deep Links): 1-2 weeks
- Total: 4-7 weeks for full implementation

---

## Current State Assessment

### ✅ Strengths of Your Current System

1. **Feature-Based Route Handlers**
   - Features can self-register routes (`RestaurantDetailRouteHandler`)
   - `RouteHandler` interface decouples features from app layer
   - Platform-specific implementations can override behavior
   - **Impact**: Easy to add new routes without modifying app layer

2. **Clean Abstraction Layers**
   - `Destination` (intent) → `Route` (concrete navigation state)
   - `AppCoordinator` provides command interface
   - `NavigationEvent` system allows async, buffered navigation
   - **Impact**: Testable, loose coupling, clear data flow

3. **Scoped View Models**
   - Routes own their lifetimes (via `RouteRegistry` on Android)
   - Clean creation/destruction of scoped dependencies
   - **Impact**: Memory management, proper lifecycle handling

### ❌ Pain Points This Plan Addresses

#### 1. **Modal Dialogs Are Tricky**
**Problem**: Your current system assumes linear stack (push → pop). Modals need:
- Overlay presentation (don't pop the underlying screen)
- Separate back stack or skip in back handling
- Platform-specific presentation (iOS Sheet vs Android ModalBottomSheet)

**Example Pain**: 
```kotlin
// Current: All navigation treats screen as a "route"
coordinator.navigateTo(Destination.RestaurantDetail(...)) // Replaces/stacks

// Needed: Modal semantics
coordinator.showModal(Destination.FilterModal(...)) // Overlay, separate handling
coordinator.dismissModal() // Close just the modal
```

#### 2. **Tab Navigation Is Complex**
**Problem**: Tabs need independent back stacks. Your current system has:
- Single shared back stack across entire app
- `trackedRouteKeys` as a flat set
- No concept of "which tab am I in?"

**Example Pain**:
```
User navigates: RestaurantList → Tab1
  ├─ Tab1: RestaurantDetail(id=1) → RestaurantDetail(id=2)
  └─ Tab2: Empty

User switches to Tab2, navigates:
  ├─ Tab1: [id=1, id=2] ← History should be preserved!
  └─ Tab2: RestaurantDetail(id=3)

User presses back in Tab2: should pop to Tab2 root
User switches to Tab1: should show id=2 (back to where they were)
```

#### 3. **Deep Links Are Underspecified**
**Problem**: No current mechanism for:
- Parsing `munchies://restaurant/123` → `RestaurantDetail(id=123)`
- Reconstructing full navigation state from URL
- Fallback handling for invalid/unknown routes

**Example Pain**:
```
Notification: munchies://restaurant/456?ref=promo
Needed: Parse URL → [RestaurantListRoute(), RestaurantDetailRoute(456)]
        Apply that state to navigation
```

---

## Architecture Design

### 3.1 Navigation Data Model

The foundation of the new system extends your existing `Route` hierarchy:

```
Route (interface)
├── StackRoute (single-stack navigation)
│   ├── RestaurantListRoute
│   ├── RestaurantDetailRoute
│   └── [Other feature routes]
├── ModalRoute (overlay presentation)
│   ├── FilterModalRoute
│   ├── DatePickerModalRoute
│   └── [Other modal routes]
└── TabRoute (multi-stack container)
    ├── HomeTabRoute (contains a StackRoute)
    ├── SearchTabRoute (contains a StackRoute)
    └── ProfileTabRoute (contains a StackRoute)
```

#### Key Data Structures

```kotlin
// core/navigation/NavigationState.kt
data class NavigationState(
    // Primary stack (for regular screen navigation)
    val stackRoutes: List<StackRoute>,
    
    // Modal overlays (independent of primary stack)
    val modalStack: List<ModalRoute>,
    
    // Tabbed navigation (if app uses tabs)
    val tabNavigation: TabNavigationState? = null,
    
    // Deep link that triggered this state (for analytics/debugging)
    val originDeepLink: String? = null
)

// core/navigation/TabNavigationState.kt
data class TabNavigationState(
    val tabs: List<TabDefinition>,
    val activeTabId: String,
    val stacksByTab: Map<String, List<StackRoute>>
)

data class TabDefinition(
    val id: String,
    val label: TextId,
    val icon: IconId,
    val rootRoute: StackRoute // Each tab must have a root
)

// core/navigation/DeepLinkResult.kt
sealed class DeepLinkResult {
    data class Success(
        val navigationState: NavigationState,
        val clearCurrentStack: Boolean = true
    ) : DeepLinkResult()
    
    data class Partial(
        val navigationState: NavigationState,
        val failedSegments: List<String>, // Segments that couldn't be parsed
        val clearCurrentStack: Boolean = false
    ) : DeepLinkResult()
    
    data class NotFound(val link: String) : DeepLinkResult()
    data class Error(val link: String, val exception: Exception) : DeepLinkResult()
}
```

### 3.2 Navigation Hierarchy Structure

```
AppNavigation
├── PrimaryStack (NavigationStack)
│   └── [StackRoute items]
├── TabNavigation (Optional, if using tabs)
│   ├── Tab1Stack
│   ├── Tab2Stack
│   └── Tab3Stack
└── ModalLayer (ModalStack)
    └── [ModalRoute items]

Navigation Flow:
PrimaryStack.push() → Shows new screen, pushes to back stack
showModal() → Shows overlay, separate back handling
selectTab(id) → Switches active tab (preserves other tabs' stacks)
```

### 3.3 State Management Architecture

```
┌─────────────────────────────────────────────┐
│  NavigationCoordinator (extends AppCoordinator)│
│  - navigationState: StateFlow<NavigationState>│
│  - navigateToScreen(Destination)             │
│  - showModal(Destination)                    │
│  - selectTab(String)                         │
└──────────────┬──────────────────────────────┘
               │
               ├─► NavigationReducer
               │   (Pure state transformations)
               │
               ├─► DeepLinkHandler
               │   (URL → NavigationState)
               │
               └─► RouteRegistry + Handlers
                   (Create/cleanup scopes)

Data Flow:
1. UI Action → navigateToScreen(Destination)
2. Coordinator emits NavigationEvent
3. Reducer transforms NavigationState
4. Platform layer receives new state
5. Render updated UI (Compose/SwiftUI)
6. Registry manages lifetimes
```

---

## Implementation Approach

### 4.1 Module Organization

**New structure** (extend existing):

```
core/
├── src/commonMain/
│   └── navigation/
│       ├── RouteHandler.kt (existing)
│       ├── Route.kt (extend for ModalRoute, StackRoute traits)
│       ├── Destination.kt (existing)
│       ├── AppCoordinator.kt → NavigationCoordinator (extend)
│       ├── NavigationEvent.kt (extend for modal/tab events)
│       ├── NavigationState.kt (NEW)
│       ├── NavigationReducer.kt (NEW)
│       ├── DeepLink.kt (NEW)
│       ├── DeepLinkParser.kt (NEW)
│       ├── TabNavigationState.kt (NEW)
│       └── modals/
│           ├── ModalRoute.kt (NEW)
│           ├── ModalStack.kt (NEW)
│           └── ModalStateManager.kt (NEW)
│
androidApp/
├── src/main/kotlin/
│   └── navigation/
│       ├── AppNavigation.kt (modify for modals/tabs)
│       ├── DeepLinkProcessor.kt (NEW)
│       ├── modals/
│       │   ├── ModalLayer.kt (NEW)
│       │   └── ModalComposable.kt (NEW)
│       └── tabs/
│           ├── TabNavigation.kt (NEW)
│           └── TabContent.kt (NEW)
│
iosApp/
├── src/iosMain/
│   └── navigation/
│       ├── DeepLinkProcessor.swift (NEW)
│       ├── modals/
│       │   └── ModalPresentation.swift (NEW)
│       └── tabs/
│           └── TabNavigation.swift (NEW)
```

**Why this structure**:
- Keeps related concerns together (modal files in `modals/` subfolder)
- Separates concerns by platform where needed
- Shared state logic in common, presentation in platform-specific
- Easy to find and maintain feature-specific code

### 4.2 Library/Framework Recommendations

#### Option A: Custom Solution (RECOMMENDED for your project)
**Pros**:
- Leverages your existing `RouteHandler` system perfectly
- Maximum control over modal/tab behavior
- Minimal external dependencies
- Your team already understands the current architecture
- Easy to test and debug

**Cons**:
- Need to implement tab state preservation yourself
- More code to maintain

**Recommendation**: Start with custom solution, since your current system is already well-designed.

---

#### Option B: Decompose (Circuit)
**Pros**:
- Purpose-built for KMP navigation with modals support
- Strong community, well-documented
- Handles memory management elegantly
- Built-in support for nested navigation

**Cons**:
- Requires rewriting your current `Route`/`Destination` system
- Different mental model (Stack, Screen traits)
- Larger learning curve for team
- More opinionated about structure

**When to use**: If you're refactoring entire navigation from scratch.

---

#### Option C: Voyager
**Pros**:
- Lightweight, KMP-first navigation library
- Good documentation, active development
- Handles back stack well

**Cons**:
- Modal/tab support is less mature
- Less community examples for complex scenarios
- Fewer built-in solutions for deep links

**When to use**: Simple navigation needs, minimal complexity.

---

**DECISION**: Implement **Option A (Custom Solution)** as primary recommendation, with Option B (Decompose) as future migration path if complexity grows.

### 4.3 Deep Link Parsing Strategy

```kotlin
// core/navigation/DeepLink.kt
interface DeepLinkHandler {
    /** Return true if this handler can parse the given deep link */
    fun canHandle(deepLink: String): Boolean
    
    /** Parse URL to navigation state */
    fun parseDeepLink(deepLink: String): DeepLinkResult
}

// Example parsers (one per feature):
// feature-restaurant/DeepLinkHandler.kt
class RestaurantDeepLinkHandler : DeepLinkHandler {
    override fun canHandle(deepLink: String): Boolean {
        return deepLink.startsWith("munchies://restaurant/") ||
               deepLink.startsWith("https://munchies.app/restaurant/")
    }
    
    override fun parseDeepLink(deepLink: String): DeepLinkResult {
        return try {
            val uri = Uri.parse(deepLink)
            val restaurantId = uri.pathSegments.getOrNull(1)
                ?: return DeepLinkResult.NotFound(deepLink)
            
            DeepLinkResult.Success(
                navigationState = NavigationState(
                    stackRoutes = listOf(
                        RestaurantListRoute(),
                        RestaurantDetailRoute(restaurantId)
                    ),
                    modalStack = emptyList()
                ),
                clearCurrentStack = true
            )
        } catch (e: Exception) {
            DeepLinkResult.Error(deepLink, e)
        }
    }
}
```

**Deep Link Format Examples**:
```
munchies://restaurant/123
  → RestaurantListRoute() → RestaurantDetailRoute(id=123)

munchies://restaurant/123?filter=italian&sort=rating
  → RestaurantListRoute(filter=italian, sort=rating) 
    → RestaurantDetailRoute(id=123)

munchies://tab/search?query=pizza
  → SelectTabRoute(id=search) with initial query navigation

munchies://restaurant/123/reviews
  → RestaurantListRoute() 
    → RestaurantDetailRoute(id=123) 
    → ReviewsModalRoute() [modal overlay]
```

---

## Feature-Specific Guidance

### 5.1 Modal Dialogs

#### Design Requirements
1. **Overlay Semantics**: Modal appears over existing content without replacing it
2. **Separate Back Stack**: Dismissing modal pops the modal stack, not the primary screen
3. **Platform Differences**:
   - iOS: Use `.sheet()`, `.fullScreenCover()`, `.alert()`
   - Android: Use `Dialog`, `ModalBottomSheet`, `AlertDialog`

#### Data Model
```kotlin
// Base trait for modal routes
interface ModalRoute : Route {
    override val isRootRoute: Boolean get() = false
    
    /** Determine presentation style for this platform */
    val presentationStyle: ModalPresentationStyle
    
    /** Whether this modal dismisses on background tap */
    val dismissOnBackgroundTap: Boolean get() = true
}

enum class ModalPresentationStyle {
    SHEET,              // iOS: .sheet(), Android: ModalBottomSheet
    FULL_SCREEN,        // iOS: .fullScreenCover(), Android: Dialog(fullscreen)
    DIALOG,             // iOS: .alert(), Android: AlertDialog
    SNACKBAR_BOTTOM     // Toast-like, custom impl
}

// Concrete examples:
data class FilterModalRoute(
    val preSelectedFilters: List<String> = emptyList()
) : ModalRoute {
    override val key: String = "FilterModal_${preSelectedFilters.hashCode()}"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
    override val dismissOnBackgroundTap: Boolean = true
}

data class ConfirmationDialogRoute(
    val message: String,
    val confirmAction: String = "OK",
    val cancelAction: String = "Cancel"
) : ModalRoute {
    override val key: String = "ConfirmDialog_${message.hashCode()}"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.DIALOG
    override val dismissOnBackgroundTap: Boolean = false
}
```

#### Navigation Commands
```kotlin
class NavigationCoordinator {
    // Regular stack navigation
    fun navigateToScreen(destination: Destination)
    
    // Modal navigation
    fun showModal(destination: ModalDestination)
    fun dismissModal()
    fun dismissModalsUntil(predicate: (ModalRoute) -> Boolean)
    fun dismissAllModals()
    
    // Combine: push screen, then show modal
    fun navigateAndShowModal(screen: Destination, modal: ModalDestination)
}

// New Destination types:
sealed class ModalDestination {
    data class Filter(val preSelectedFilters: List<String>) : ModalDestination()
    data class ConfirmAction(val message: String) : ModalDestination()
    data class DatePicker(val selectedDate: LocalDate) : ModalDestination()
}
```

#### Android Implementation (Compose)
```kotlin
// androidApp/modals/ModalLayer.kt
@Composable
fun ModalLayer(
    navigationState: NavigationState,
    coordinator: NavigationCoordinator
) {
    // Show modals in order (if multiple stacked)
    navigationState.modalStack.forEach { modal ->
        when (modal) {
            is FilterModalRoute -> {
                ModalBottomSheet(
                    onDismissRequest = { coordinator.dismissModal() },
                    shape = RoundedCornerShape(16.dp),
                    content = {
                        FilterModalContent(
                            preSelectedFilters = modal.preSelectedFilters,
                            onApply = { selectedFilters ->
                                coordinator.dismissModal()
                                // Notify screen of selection
                            }
                        )
                    }
                )
            }
            is ConfirmationDialogRoute -> {
                AlertDialog(
                    onDismissRequest = { coordinator.dismissModal() },
                    title = { Text(modal.message) },
                    confirmButton = {
                        Button(onClick = { coordinator.dismissModal() }) {
                            Text(modal.confirmAction)
                        }
                    }
                )
            }
        }
    }
}

// Modify AppNavigation to include modal layer:
@Composable
fun AppNavigation(
    coordinator: NavigationCoordinator,
    navigationState: NavigationState
) {
    NavHost(...) { /* existing stack routes */ }
    
    // Render modal layer on top
    ModalLayer(navigationState, coordinator)
}
```

#### iOS Implementation (SwiftUI)
```swift
// iosApp/navigation/ModalPresentation.swift
struct ModalLayer: View {
    @ObservedObject var navigationState: NavigationState
    let coordinator: NavigationCoordinator
    
    var body: some View {
        Group {
            if !navigationState.modalStack.isEmpty {
                let topModal = navigationState.modalStack.last!
                modalPresentation(for: topModal)
            }
        }
    }
    
    @ViewBuilder
    private func modalPresentation(for modal: ModalRoute) -> some View {
        switch modal {
        case let filterModal as FilterModalRoute:
            sheet(isPresented: .constant(true)) {
                FilterModalView(
                    preSelectedFilters: filterModal.preSelectedFilters,
                    onDismiss: { coordinator.dismissModal() },
                    onApply: { filters in
                        coordinator.dismissModal()
                        // Handle filter selection
                    }
                )
            }
        
        case let confirmModal as ConfirmationDialogRoute:
            alert(isPresented: .constant(true)) {
                Alert(
                    title: Text(confirmModal.message),
                    primaryButton: .default(Text(confirmModal.confirmAction)) {
                        coordinator.dismissModal()
                    },
                    secondaryButton: .cancel(Text(confirmModal.cancelAction)) {
                        coordinator.dismissModal()
                    }
                )
            }
        
        default:
            EmptyView()
        }
    }
}
```

#### Back Stack Handling
```kotlin
// core/navigation/NavigationReducer.kt
fun handleModalDismissal(state: NavigationState): NavigationState {
    return state.copy(
        // Pop from modal stack, keep primary stack unchanged
        modalStack = state.modalStack.dropLast(1)
    )
}

// This differs from normal back button:
fun handleBackButton(state: NavigationState): NavigationState {
    return when {
        // If modals are showing, dismiss top modal
        state.modalStack.isNotEmpty() -> {
            handleModalDismissal(state)
        }
        // Otherwise, pop primary stack
        state.stackRoutes.size > 1 -> {
            state.copy(stackRoutes = state.stackRoutes.dropLast(1))
        }
        // At root, no navigation
        else -> state
    }
}
```

### 5.2 Tab Navigation

#### Design Requirements
1. **Independent Back Stacks**: Each tab maintains separate navigation history
2. **Preserve State**: Switching tabs remembers where user was
3. **Lazy Loading**: Don't create tab content until selected
4. **Shared Root**: Often all tabs start from same root screen

#### Data Model
```kotlin
// core/navigation/TabNavigationState.kt
data class TabNavigationState(
    val tabDefinitions: List<TabDefinition>,
    val activeTabId: String,
    val stacksByTab: Map<String, List<StackRoute>>
) {
    fun getActiveTabStack(): List<StackRoute> {
        return stacksByTab[activeTabId] ?: emptyList()
    }
    
    fun updateActiveTabStack(newStack: List<StackRoute>): TabNavigationState {
        return copy(
            stacksByTab = stacksByTab.toMutableMap().apply {
                this[activeTabId] = newStack
            }
        )
    }
}

data class TabDefinition(
    val id: String,
    val label: TextId,
    val icon: IconId,
    val rootRoute: StackRoute
)

// Modified NavigationState:
data class NavigationState(
    // Either use tab navigation OR primary stack (not both)
    val usesTabs: Boolean = false,
    val primaryStack: List<StackRoute> = emptyList(),
    val tabNavigation: TabNavigationState? = null,
    val modalStack: List<ModalRoute> = emptyList()
) {
    val currentStack: List<StackRoute>
        get() = if (usesTabs) tabNavigation?.getActiveTabStack() ?: emptyList()
                else primaryStack
}
```

#### Tab Switching & Back Stack Preservation
```kotlin
// core/navigation/NavigationCoordinator.kt
class NavigationCoordinator {
    fun selectTab(tabId: String) {
        // Switch tabs, preserve each tab's stack
        val event = NavigationEvent.SelectTab(tabId)
        _navigationEvents.tryEmit(event)
    }
    
    fun navigateInCurrentTab(destination: Destination) {
        // Push to the current tab's stack
        val event = NavigationEvent.PushInTab(destination)
        _navigationEvents.tryEmit(event)
    }
    
    fun popInCurrentTab() {
        val event = NavigationEvent.PopInTab
        _navigationEvents.tryEmit(event)
    }
}

// core/navigation/NavigationReducer.kt
fun handleTabSelection(
    currentState: NavigationState,
    tabId: String
): NavigationState {
    return currentState.copy(
        tabNavigation = currentState.tabNavigation?.copy(
            activeTabId = tabId
            // Note: stacks are preserved in stacksByTab
        )
    )
}

fun handlePushInTab(
    currentState: NavigationState,
    destination: Destination,
    handlers: List<RouteHandler>
): NavigationState {
    val route = handlers.firstNotNullOfOrNull { h ->
        if (h.canHandle(destination)) h.destinationToRoute(destination) else null
    } as? StackRoute ?: return currentState
    
    val tabNavigation = currentState.tabNavigation ?: return currentState
    val activeTabId = tabNavigation.activeTabId
    val currentStack = tabNavigation.getActiveTabStack()
    
    val newStack = currentStack + route
    return currentState.copy(
        tabNavigation = tabNavigation.updateActiveTabStack(newStack)
    )
}
```

#### Android Implementation (Compose)
```kotlin
// androidApp/tabs/TabNavigation.kt
@Composable
fun TabNavigation(
    navigationState: NavigationState,
    coordinator: NavigationCoordinator,
    routeProviders: List<RouteProvider>
) {
    if (!navigationState.usesTabs) return
    
    val tabNavigation = navigationState.tabNavigation ?: return
    var selectedTabIndex by remember { 
        mutableStateOf(
            tabNavigation.tabDefinitions.indexOfFirst { it.id == tabNavigation.activeTabId }
        )
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabNavigation.tabDefinitions.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            coordinator.selectTab(tab.id)
                        },
                        icon = { Icon(painter = painterResource(tab.icon.resourceId), null) },
                        label = { Text(stringResource(tab.label.resourceId)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            val activeTabStack = tabNavigation.getActiveTabStack()
            
            // Render the active tab's stack
            if (activeTabStack.isNotEmpty()) {
                TabStackContent(
                    stack = activeTabStack,
                    coordinator = coordinator,
                    routeProviders = routeProviders
                )
            } else {
                // Show tab's root screen
                val rootRoute = tabNavigation.tabDefinitions[selectedTabIndex].rootRoute
                StackContent(
                    routes = listOf(rootRoute),
                    coordinator = coordinator,
                    routeProviders = routeProviders
                )
            }
        }
    }
}

@Composable
fun TabStackContent(
    stack: List<StackRoute>,
    coordinator: NavigationCoordinator,
    routeProviders: List<RouteProvider>
) {
    val navController = rememberNavController()
    
    // Build nav host for this specific tab's stack
    NavHost(
        navController = navController,
        startDestination = stack.first().key
    ) {
        stack.forEach { route ->
            val builders = routeProviders
                .flatMap { it.getRoutes() }
                .filterIsInstance<RouteComposableBuilder>()
            
            builders.forEach { builder ->
                builder.buildComposable(this, coordinator)
            }
        }
    }
}
```

#### iOS Implementation (SwiftUI)
```swift
// iosApp/navigation/TabNavigation.swift
struct TabNavigationView: View {
    @ObservedObject var navigationState: NavigationState
    let coordinator: NavigationCoordinator
    
    var body: some View {
        guard navigationState.usesTabs,
              let tabNav = navigationState.tabNavigation else {
            return AnyView(EmptyView())
        }
        
        return AnyView(
            TabView(selection: .constant(tabNav.activeTabId)) {
                ForEach(tabNav.tabDefinitions, id: \.id) { tab in
                    TabContentView(
                        tabId: tab.id,
                        stack: tabNav.stacksByTab[tab.id] ?? [tab.rootRoute],
                        coordinator: coordinator
                    )
                    .tabItem {
                        Image(systemName: tab.icon.systemName)
                        Text(LocalizedStringKey(tab.label.key))
                    }
                    .tag(tab.id)
                    .onTapGesture {
                        coordinator.selectTab(tab.id)
                    }
                }
            }
        )
    }
}

struct TabContentView: View {
    let tabId: String
    let stack: [StackRoute]
    let coordinator: NavigationCoordinator
    
    var body: some View {
        NavigationStack(path: .constant(stack.map { $0.key })) {
            ForEach(stack, id: \.key) { route in
                routeView(for: route)
            }
        }
    }
    
    @ViewBuilder
    private func routeView(for route: StackRoute) -> some View {
        switch route {
        case let restaurantList as RestaurantListRoute:
            RestaurantListView(coordinator: coordinator)
        case let detail as RestaurantDetailRoute:
            RestaurantDetailView(restaurantId: detail.restaurantId, coordinator: coordinator)
        default:
            EmptyView()
        }
    }
}
```

### 5.3 Deep Links

#### Parsing Strategy
```kotlin
// core/navigation/DeepLinkParser.kt
class DeepLinkParser(
    private val handlers: List<DeepLinkHandler>
) {
    fun parse(deepLink: String): DeepLinkResult {
        for (handler in handlers) {
            if (handler.canHandle(deepLink)) {
                return handler.parseDeepLink(deepLink)
            }
        }
        return DeepLinkResult.NotFound(deepLink)
    }
}

// Per-feature implementations:
// feature-restaurant/navigation/RestaurantDeepLinkHandler.kt
class RestaurantDeepLinkHandler : DeepLinkHandler {
    override fun canHandle(deepLink: String): Boolean {
        return deepLink.contains("restaurant") || deepLink.contains("munchies://restaurant")
    }
    
    override fun parseDeepLink(deepLink: String): DeepLinkResult {
        return try {
            val uri = Uri.parse(deepLink)
            
            // Parse restaurant ID
            val restaurantId = when {
                uri.host == "munchies.app" && uri.path?.startsWith("/restaurant/") == true -> {
                    uri.pathSegments.getOrNull(1)
                }
                uri.scheme == "munchies" && uri.host == "restaurant" -> {
                    uri.pathSegments.firstOrNull()
                }
                else -> null
            } ?: return DeepLinkResult.NotFound(deepLink)
            
            // Parse optional parameters
            val filter = uri.getQueryParameter("filter")
            val sort = uri.getQueryParameter("sort")
            
            DeepLinkResult.Success(
                navigationState = NavigationState(
                    stackRoutes = listOf(
                        RestaurantListRoute(
                            initialFilter = filter,
                            initialSort = sort
                        ),
                        RestaurantDetailRoute(restaurantId)
                    ),
                    modalStack = emptyList(),
                    originDeepLink = deepLink
                ),
                clearCurrentStack = true
            )
        } catch (e: Exception) {
            DeepLinkResult.Error(deepLink, e)
        }
    }
}

// Example: Reviews modal deep link
class ReviewsDeepLinkHandler : DeepLinkHandler {
    override fun canHandle(deepLink: String): Boolean {
        return deepLink.contains("/reviews")
    }
    
    override fun parseDeepLink(deepLink: String): DeepLinkResult {
        return try {
            val uri = Uri.parse(deepLink)
            val restaurantId = uri.pathSegments.getOrNull(1)
                ?: return DeepLinkResult.NotFound(deepLink)
            
            DeepLinkResult.Success(
                navigationState = NavigationState(
                    stackRoutes = listOf(
                        RestaurantListRoute(),
                        RestaurantDetailRoute(restaurantId)
                    ),
                    modalStack = listOf(
                        ReviewsModalRoute(restaurantId)
                    ),
                    originDeepLink = deepLink
                ),
                clearCurrentStack = true
            )
        } catch (e: Exception) {
            DeepLinkResult.Error(deepLink, e)
        }
    }
}
```

#### Android Deep Link Handling
```kotlin
// androidApp/navigation/DeepLinkProcessor.kt
class DeepLinkProcessor(
    private val parser: DeepLinkParser,
    private val coordinator: NavigationCoordinator
) {
    fun handleDeepLink(deepLink: String) {
        val result = parser.parse(deepLink)
        
        when (result) {
            is DeepLinkResult.Success -> {
                coordinator.applyNavigationState(result.navigationState)
            }
            is DeepLinkResult.Partial -> {
                // Partially parsed, log warning
                Log.w("DeepLink", "Partial match for $deepLink: ${result.failedSegments}")
                coordinator.applyNavigationState(result.navigationState)
            }
            is DeepLinkResult.NotFound -> {
                Log.e("DeepLink", "No handler for $deepLink, navigating to home")
                coordinator.navigateToRoot()
            }
            is DeepLinkResult.Error -> {
                Log.e("DeepLink", "Error parsing $deepLink", result.exception)
                coordinator.navigateToRoot()
            }
        }
    }
}

// In MainActivity:
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val deepLinkProcessor: DeepLinkProcessor by inject()
    private val coordinator: NavigationCoordinator by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle intent deep link
        intent?.data?.let { uri ->
            deepLinkProcessor.handleDeepLink(uri.toString())
        }
        
        setContent {
            val navigationState by coordinator.navigationState.collectAsState()
            AppNavigation(coordinator, navigationState)
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { uri ->
            deepLinkProcessor.handleDeepLink(uri.toString())
        }
    }
}

// AndroidManifest.xml configuration:
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    
    <!-- Deep link configuration -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="munchies"
            android:host="restaurant" />
    </intent-filter>
    
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="munchies.app"
            android:pathPrefix="/restaurant/" />
    </intent-filter>
</activity>
```

#### iOS Deep Link Handling
```swift
// iosApp/navigation/DeepLinkProcessor.swift
class DeepLinkProcessor: NSObject {
    let parser: DeepLinkParser
    let coordinator: NavigationCoordinator
    
    func handleDeepLink(_ url: URL) {
        let deepLink = url.absoluteString
        let result = parser.parse(deepLink: deepLink)
        
        switch result {
        case .success(let navigationState, let clearStack):
            coordinator.applyNavigationState(navigationState)
        case .partial(let navigationState, let failedSegments, let clearStack):
            print("Partial deep link match: \(failedSegments)")
            coordinator.applyNavigationState(navigationState)
        case .notFound(let link):
            print("Deep link not found: \(link)")
            coordinator.navigateToRoot()
        case .error(let link, let exception):
            print("Error parsing deep link \(link): \(exception)")
            coordinator.navigateToRoot()
        }
    }
}

// In SceneDelegate:
scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
    guard let url = URLContexts.first?.url else { return }
    let processor = DeepLinkProcessor(parser: ..., coordinator: ...)
    processor.handleDeepLink(url)
}
```

#### Universal Links Configuration
```xml
<!-- iOS: Associated Domains -->
<!-- entitlements file -->
<key>com.apple.developer.associated-domains</key>
<array>
    <string>applinks:munchies.app</string>
    <string>applinks:www.munchies.app</string>
</array>

<!-- android: AssetLinks -->
<!-- .well-known/assetlinks.json on your web server -->
{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "io.umain.munchies.android",
    "sha256_cert_fingerprints": ["<your_sha256_fingerprint>"]
  }
}
```

---

## Code Structure Examples

### 6.1 File Organization

```
core/src/commonMain/kotlin/io/umain/munchies/
├── navigation/
│   ├── /* Existing */
│   ├── RouteHandler.kt
│   ├── Route.kt
│   ├── Destination.kt
│   ├── RouteProvider.kt
│   ├── ScopedRouteHandler.kt
│   ├── RouteNavigationMapper.kt
│   │
│   ├── /* Extended/New Core Concepts */
│   ├── NavigationState.kt
│   ├── NavigationEvent.kt (extend with Tab/Modal events)
│   ├── NavigationCoordinator.kt (extends AppCoordinator)
│   ├── NavigationReducer.kt
│   │
│   ├── /* Modal Support */
│   ├── modals/
│   │   ├── ModalRoute.kt
│   │   ├── ModalDestination.kt
│   │   ├── ModalStateManager.kt
│   │   └── ModalPresentationStyle.kt
│   │
│   ├── /* Tab Support */
│   ├── tabs/
│   │   ├── TabNavigationState.kt
│   │   ├── TabDefinition.kt
│   │   ├── StackRoute.kt (marker interface)
│   │   └── TabStateManager.kt
│   │
│   └── /* Deep Link Support */
│       ├── deeplink/
│       │   ├── DeepLink.kt (value object)
│       │   ├── DeepLinkHandler.kt
│       │   ├── DeepLinkParser.kt
│       │   ├── DeepLinkResult.kt
│       │   └── DeepLinkRegistry.kt

androidApp/src/main/kotlin/io/umain/munchies/android/
├── navigation/
│   ├── AppNavigation.kt (modify)
│   ├── RouteRegistry.kt (existing)
│   │
│   ├── modals/
│   │   ├── ModalLayer.kt
│   │   ├── ModalComposable.kt
│   │   └── DialogFactory.kt
│   │
│   ├── tabs/
│   │   ├── TabNavigation.kt
│   │   ├── TabContent.kt
│   │   └── TabBar.kt
│   │
│   └── deeplink/
│       ├── DeepLinkProcessor.kt
│       └── IntentHandler.kt

feature-restaurant/src/commonMain/kotlin/
├── navigation/
│   ├── RestaurantDetailRouteHandler.kt
│   ├── RestaurantListRouteHandler.kt
│   └── deeplink/
│       ├── RestaurantDeepLinkHandler.kt
│       └── ReviewsDeepLinkHandler.kt

feature-restaurant/src/androidMain/kotlin/
└── navigation/
    ├── deeplink/
    │   └── RestaurantDeepLinkHandler.android.kt
    └── modals/
        └── ReviewsModalComposable.kt

feature-restaurant/src/iosMain/kotlin/
└── navigation/
    ├── deeplink/
    │   └── RestaurantDeepLinkHandler.ios.swift
    └── modals/
        └── ReviewsModalView.swift
```

### 6.2 Key Classes - Conceptual Examples

#### NavigationCoordinator (Extends AppCoordinator)
```kotlin
// core/navigation/NavigationCoordinator.kt
class NavigationCoordinator {
    // Current state of the entire navigation system
    private val _navigationState = MutableStateFlow(
        NavigationState(
            usesTabs = false,
            primaryStack = listOf(RestaurantListRoute())
        )
    )
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    // Events for platform layer consumption
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(
        extraBufferCapacity = 10
    )
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    // === SCREEN NAVIGATION ===
    fun navigateToScreen(destination: Destination) {
        _navigationEvents.tryEmit(NavigationEvent.Push(destination))
    }
    
    fun navigateBack() {
        _navigationEvents.tryEmit(NavigationEvent.Pop)
    }
    
    fun navigateToRoot() {
        _navigationEvents.tryEmit(NavigationEvent.PopToRoot)
    }
    
    // === MODAL NAVIGATION ===
    fun showModal(modalDestination: ModalDestination) {
        _navigationEvents.tryEmit(NavigationEvent.ShowModal(modalDestination))
    }
    
    fun dismissModal() {
        _navigationEvents.tryEmit(NavigationEvent.DismissModal)
    }
    
    fun dismissAllModals() {
        _navigationEvents.tryEmit(NavigationEvent.DismissAllModals)
    }
    
    // === TAB NAVIGATION ===
    fun selectTab(tabId: String) {
        _navigationEvents.tryEmit(NavigationEvent.SelectTab(tabId))
    }
    
    fun navigateInTab(destination: Destination) {
        _navigationEvents.tryEmit(NavigationEvent.PushInTab(destination))
    }
    
    // === STATE MANAGEMENT (Internal) ===
    fun applyNavigationState(newState: NavigationState) {
        _navigationState.value = newState
    }
    
    internal fun applyReducer(reducer: (NavigationState) -> NavigationState) {
        _navigationState.value = reducer(_navigationState.value)
    }
}
```

#### NavigationReducer
```kotlin
// core/navigation/NavigationReducer.kt
object NavigationReducer {
    fun reduce(
        currentState: NavigationState,
        event: NavigationEvent,
        routeHandlers: List<RouteHandler>
    ): NavigationState {
        return when (event) {
            is NavigationEvent.Push -> handlePush(currentState, event, routeHandlers)
            is NavigationEvent.Pop -> handlePop(currentState)
            is NavigationEvent.PopToRoot -> handlePopToRoot(currentState)
            
            is NavigationEvent.ShowModal -> handleShowModal(currentState, event, routeHandlers)
            is NavigationEvent.DismissModal -> handleDismissModal(currentState)
            is NavigationEvent.DismissAllModals -> handleDismissAllModals(currentState)
            
            is NavigationEvent.SelectTab -> handleSelectTab(currentState, event)
            is NavigationEvent.PushInTab -> handlePushInTab(currentState, event, routeHandlers)
            is NavigationEvent.PopInTab -> handlePopInTab(currentState)
        }
    }
    
    private fun handlePush(
        state: NavigationState,
        event: NavigationEvent.Push,
        handlers: List<RouteHandler>
    ): NavigationState {
        val route = handlers.firstNotNullOfOrNull { handler ->
            if (handler.canHandle(event.destination)) {
                handler.destinationToRoute(event.destination) as? StackRoute
            } else null
        } ?: return state
        
        return state.copy(
            primaryStack = state.primaryStack + route
        )
    }
    
    private fun handleShowModal(
        state: NavigationState,
        event: NavigationEvent.ShowModal,
        handlers: List<RouteHandler>
    ): NavigationState {
        val modalRoute = handlers.firstNotNullOfOrNull { handler ->
            if (handler.canHandle(event.destination)) {
                handler.destinationToRoute(event.destination) as? ModalRoute
            } else null
        } ?: return state
        
        return state.copy(
            modalStack = state.modalStack + modalRoute
        )
    }
    
    private fun handleSelectTab(
        state: NavigationState,
        event: NavigationEvent.SelectTab
    ): NavigationState {
        return state.copy(
            tabNavigation = state.tabNavigation?.copy(
                activeTabId = event.tabId
            )
        )
    }
    
    // ... other handlers ...
}
```

#### DeepLinkParser Registry
```kotlin
// core/navigation/deeplink/DeepLinkParser.kt
class DeepLinkParser(
    private val handlers: List<DeepLinkHandler>
) {
    fun parse(deepLink: String): DeepLinkResult {
        // Try each handler in order until one succeeds
        for (handler in handlers) {
            if (handler.canHandle(deepLink)) {
                return try {
                    handler.parseDeepLink(deepLink)
                } catch (e: Exception) {
                    DeepLinkResult.Error(deepLink, e)
                }
            }
        }
        return DeepLinkResult.NotFound(deepLink)
    }
}

// In core/di/KoinModule.kt
fun registerDeepLinkHandlers() {
    single {
        val handlers = get<List<DeepLinkHandler>>()
        DeepLinkParser(handlers)
    }
    
    // Will be registered by features
    // Example: RestaurantDeepLinkHandler in feature-restaurant
}
```

### 6.3 Data Flow Diagram

```
USER INTERACTION (UI)
        ↓
    Button Click / Deep Link / System Event
        ↓
NavigationCoordinator API
  • navigateToScreen(destination)
  • showModal(destination)
  • selectTab(id)
        ↓
NavigationEvent Emission
  (Push, Pop, ShowModal, SelectTab, etc.)
        ↓
Platform Layer (Android/iOS)
  • Receives NavigationEvent via Flow
  • Calls NavigationReducer
        ↓
NavigationReducer
  (Pure function: state + event → new state)
        ↓
NavigationState Update
  (StateFlow emission)
        ↓
RouteRegistry
  • Creates/cleanup scopes for new routes
  • Manages route lifetimes
        ↓
Platform Rendering
  • Android: NavHost recomposes
  • iOS: NavigationStack updates
  • Modal Layer renders overlays
  • Tab Layer renders active tab
        ↓
UI Update
  (User sees new screen/modal/tab)
```

---

## Integration Considerations

### 7.1 Platform-Specific Integration

#### Android Integration Points
```kotlin
// 1. MainActivity must handle Intent deep links
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val deepLinkProcessor: DeepLinkProcessor = inject()
    intent?.data?.let { uri ->
        deepLinkProcessor.handleDeepLink(uri.toString())
    }
}

// 2. AndroidManifest.xml must declare schemes
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="munchies" android:host="restaurant" />
</intent-filter>

// 3. Compose NavHost must be updated for tab structure
NavHost(navController, startDestination) {
    // If tabs, build nested NavHost per tab
    // If not, build single flat NavHost
}

// 4. LaunchedEffect must handle NavigationEvents
LaunchedEffect(navigationState.modalStack) {
    // Recompose when modal stack changes
}
```

#### iOS Integration Points
```swift
// 1. SceneDelegate/App must handle URL events
func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
    guard let url = URLContexts.first?.url else { return }
    deepLinkProcessor.handleDeepLink(url)
}

// 2. AppDelegate must handle remote notifications with deep links
func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    didReceive response: UNNotificationResponse,
    withCompletionHandler completionHandler: @escaping () -> Void
) {
    if let deepLink = response.notification.request.content.userInfo["deepLink"] as? String {
        deepLinkProcessor.handleDeepLink(deepLink)
    }
}

// 3. SwiftUI views must react to state changes
.onChange(of: navigationState.modalStack) { newStack in
    // Update modal presentation
}

// 4. TabView must preserve state per tab
TabView(selection: .constant(tabNavigation.activeTabId)) {
    // Each tab content is independent
}
```

### 7.2 Handling Platform-Specific UI Patterns

```kotlin
// ANDROID: ModalBottomSheet for filters
ModalBottomSheet(
    onDismissRequest = { coordinator.dismissModal() },
    scrimColor = Color.Black.copy(alpha = 0.32f),
    shape = RoundedCornerShape(16.dp),
    sheetContent = {
        FilterModalContent(...)
    }
)

// ANDROID: Dialog for confirmations
AlertDialog(
    onDismissRequest = { coordinator.dismissModal() },
    confirmButton = { ... },
    dismissButton = { ... }
)
```

```swift
// iOS: Sheet for filters
.sheet(isPresented: .constant(showFilter)) {
    FilterView()
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
}

// iOS: Alert for confirmations
.alert("Are you sure?", isPresented: .constant(showConfirm)) {
    Button("OK") { coordinator.dismissModal() }
}

// iOS: NavigationStack for tab content
NavigationStack(path: .constant(stack)) {
    // Content
}
```

### 7.3 Testing Strategy

#### Unit Testing - Navigation Reducer
```kotlin
// androidApp/src/test/kotlin/navigation/NavigationReducerTest.kt
class NavigationReducerTest {
    @Test
    fun `navigating adds route to stack`() {
        val initialState = NavigationState(
            primaryStack = listOf(RestaurantListRoute())
        )
        
        val event = NavigationEvent.Push(Destination.RestaurantDetail(id = "123"))
        val handlers = listOf(RestaurantDetailRouteHandler())
        
        val newState = NavigationReducer.reduce(initialState, event, handlers)
        
        assertEquals(2, newState.primaryStack.size)
        assertEquals(RestaurantDetailRoute("123"), newState.primaryStack.last())
    }
    
    @Test
    fun `showing modal doesn't affect primary stack`() {
        val initialState = NavigationState(
            primaryStack = listOf(RestaurantListRoute()),
            modalStack = emptyList()
        )
        
        val event = NavigationEvent.ShowModal(ModalDestination.Filter())
        val handlers = listOf(FilterModalRouteHandler())
        
        val newState = NavigationReducer.reduce(initialState, event, handlers)
        
        assertEquals(1, newState.primaryStack.size) // Unchanged
        assertEquals(1, newState.modalStack.size)   // New modal added
    }
    
    @Test
    fun `back button dismisses modal if present`() {
        val initialState = NavigationState(
            primaryStack = listOf(RestaurantDetailRoute("123")),
            modalStack = listOf(FilterModalRoute())
        )
        
        val event = NavigationEvent.Pop
        
        val newState = NavigationReducer.reduce(initialState, event, emptyList())
        
        // Modal dismissed, primary stack unchanged
        assertEquals(1, newState.primaryStack.size)
        assertEquals(0, newState.modalStack.size)
    }
    
    @Test
    fun `selecting tab updates active tab without losing stack`() {
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(
                TabDefinition("home", ...),
                TabDefinition("search", ...)
            ),
            activeTabId = "home",
            stacksByTab = mapOf(
                "home" to listOf(HomeRoute(), DetailRoute("1")),
                "search" to listOf(SearchRoute())
            )
        )
        
        val initialState = NavigationState(usesTabs = true, tabNavigation = tabNav)
        val event = NavigationEvent.SelectTab("search")
        
        val newState = NavigationReducer.reduce(initialState, event, emptyList())
        
        assertEquals("search", newState.tabNavigation?.activeTabId)
        // Home stack preserved!
        assertEquals(2, newState.tabNavigation?.stacksByTab?.get("home")?.size)
    }
}
```

#### Integration Testing - Deep Links
```kotlin
// androidApp/src/androidTest/kotlin/navigation/DeepLinkTest.kt
@RunWith(AndroidJUnit4::class)
class DeepLinkTest {
    private val parser: DeepLinkParser = DeepLinkParser(
        listOf(
            RestaurantDeepLinkHandler(),
            ReviewsDeepLinkHandler()
        )
    )
    
    @Test
    fun `parses restaurant detail deep link`() {
        val result = parser.parse("munchies://restaurant/123")
        
        assertTrue(result is DeepLinkResult.Success)
        val state = (result as DeepLinkResult.Success).navigationState
        assertEquals(2, state.primaryStack.size)
        assertEquals("RestaurantDetail_123", state.primaryStack.last().key)
    }
    
    @Test
    fun `parses restaurant with reviews modal`() {
        val result = parser.parse("munchies://restaurant/123/reviews")
        
        assertTrue(result is DeepLinkResult.Success)
        val state = (result as DeepLinkResult.Success).navigationState
        assertEquals(1, state.modalStack.size)
        assertEquals("ReviewsModal_123", state.modalStack.first().key)
    }
    
    @Test
    fun `returns NotFound for unknown link`() {
        val result = parser.parse("munchies://unknown/path")
        
        assertTrue(result is DeepLinkResult.NotFound)
    }
}
```

#### UI Testing - Modal Dismissal
```kotlin
// androidApp/src/androidTest/kotlin/navigation/ModalNavigationTest.kt
@RunWith(AndroidJUnit4::class)
class ModalNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()
    
    @Test
    fun `tapping background dismisses modal`() {
        val coordinator = NavigationCoordinator()
        
        composeRule.setContent {
            val state by coordinator.navigationState.collectAsState()
            AppNavigation(coordinator, state)
        }
        
        // Show modal
        coordinator.showModal(ModalDestination.Filter())
        composeRule.waitForIdle()
        
        // Verify modal is visible
        composeRule.onNodeWithTag("filter_modal").assertIsDisplayed()
        
        // Tap background (or dismiss button)
        composeRule.onNodeWithTag("modal_background").performClick()
        composeRule.waitForIdle()
        
        // Modal should be dismissed
        composeRule.onNodeWithTag("filter_modal").assertDoesNotExist()
    }
}
```

---

## Migration Path

### 8.1 Phase-Based Approach (Recommended)

#### Phase 0: Foundation (Week 1)
**Goal**: Extend core navigation without breaking existing system

**Tasks**:
1. ✅ Create new data classes (without modifying existing ones):
   - `StackRoute` interface (marker)
   - `ModalRoute` interface
   - `TabNavigationState` data class
   - `NavigationState` data class

2. ✅ Create `NavigationReducer` (pure, no side effects)

3. ✅ Extend `NavigationCoordinator` with new methods (keep old ones)
   - New: `showModal()`, `selectTab()`
   - Old: `navigateTo()`, `navigateBack()` (unchanged)

4. ✅ Keep existing platform layers working:
   - Android `AppNavigation.kt` still works for non-modal/non-tab scenarios
   - iOS navigation still works

**Success Criteria**:
- Existing navigation still works
- New classes compile
- No breaking changes to `RouteHandler` or `Destination`
- Zero impact on feature modules

#### Phase 1: Modals (Weeks 2-3)
**Goal**: Add modal support while keeping other features working

**Android-Specific**:
1. Create `ModalLayer.kt` composable
2. Create `ModalComposableBuilder` interface
3. Update `AppNavigation` to render `ModalLayer` on top of `NavHost`
4. Implement first modal (FilterModal)
5. Test modal dismiss behavior

**iOS-Specific**:
1. Create `ModalPresentation.swift`
2. Update navigation coordinator to emit modal events
3. Implement sheet/fullscreen modifiers
4. Test modal dismiss behavior

**Feature Work**:
1. Add `FilterModalRoute` to navigation
2. Implement handlers
3. Add composable/view builders for modal

**Verification**:
- Modals appear/dismiss correctly
- Back button on primary screen unaffected by modal
- No memory leaks when dismissing modals

#### Phase 2: Tabs (Weeks 4-5)
**Goal**: Add tab support

**Android-Specific**:
1. Create `TabNavigation.kt` composable
2. Create `TabContent.kt` for rendering tab stacks
3. Implement `BottomNavigationBar` with tab switching
4. Handle tab state preservation

**iOS-Specific**:
1. Create `TabNavigationView.swift`
2. Update `TabView` integration
3. Test state preservation per tab

**Feature Work**:
1. Add tab route definitions
2. Update reducers to handle tab events
3. Test switching between tabs with back stack

**Verification**:
- Switching tabs preserves history
- Each tab has independent back stack
- Modal works on top of tabs

#### Phase 3: Deep Links (Weeks 6-7)
**Goal**: Add deep link support

**Common**:
1. Create `DeepLinkHandler` interface
2. Create `DeepLinkParser` registry
3. Implement per-feature handlers

**Android-Specific**:
1. Create `DeepLinkProcessor`
2. Integrate with `MainActivity` intent handling
3. Update `AndroidManifest.xml`
4. Test URL scheme handling

**iOS-Specific**:
1. Create `DeepLinkProcessor`
2. Integrate with `SceneDelegate`
3. Configure universal links
4. Test deep link handling

**Feature Work**:
1. Each feature implements `DeepLinkHandler`
2. Register in Koin
3. Test parsing various URL formats

**Verification**:
- Deep links navigate to correct screen
- Modal opens if deep link specifies modal
- Back button works after deep link navigation
- Invalid links handle gracefully

### 8.2 Breaking Changes & Backward Compatibility

**Good News**: Your architecture supports incremental adoption!

**What Changes**:
- `NavigationEvent` sealed class gains new subtypes
  - ✅ Existing `Push`, `Pop`, `PopToRoot` unaffected
  - ✅ New: `ShowModal`, `SelectTab`, etc.

- `AppCoordinator` → `NavigationCoordinator`
  - ✅ Keep `AppCoordinator` as deprecated alias
  - ✅ Gradual migration over sprints

**What Stays the Same**:
- ✅ `Route` interface
- ✅ `Destination` sealed class
- ✅ `RouteHandler` interface
- ✅ Feature modules' route handlers
- ✅ `RouteProvider` pattern

**Migration Strategy for Features**:
```kotlin
// Step 1: Extend RouteHandler (backward compatible)
interface RouteHandler {
    val route: Route
    fun toRouteString(): String
    fun canHandle(destination: Destination): Boolean
    fun destinationToRoute(destination: Destination): Route?
    
    // NEW: Optional method features can implement
    fun canHandleModal(destination: Destination): Boolean = false
    fun destinationToModalRoute(destination: Destination): ModalRoute? = null
}

// Step 2: Features update handlers one at a time
class FilterModalRouteHandler : RouteHandler {
    override fun canHandleModal(destination: Destination): Boolean {
        return destination is ModalDestination.Filter
    }
    
    override fun destinationToModalRoute(destination: Destination): ModalRoute? {
        return (destination as? ModalDestination.Filter)?.let {
            FilterModalRoute(it.preSelectedFilters)
        }
    }
}

// Step 3: Existing route handlers keep working unchanged
class RestaurantDetailRouteHandler : RouteHandler {
    // No changes needed, still works
}
```

### 8.3 Feature-by-Feature Migration

**Order**: Features with minimal dependencies first

```
1. Design Tokens Module
   └─ Add modal styles (padding, colors, animations)

2. Core Module
   └─ Add new navigation classes (no dependencies)

3. Feature-Restaurant
   ├─ Add FilterModalRoute + handler
   ├─ Add ReviewsModalRoute + handler
   ├─ Add RestaurantDeepLinkHandler
   └─ Test all three working together

4. (Future) Feature-Profile
   └─ Add ProfileTabRoute + tab navigation
```

---

## Performance & Best Practices

### 9.1 Memory Management

#### Modal Stack Memory
**Problem**: Each modal in the stack could hold resources

**Solution**:
```kotlin
// Good: Clean up modals' ViewModels when dismissed
class ModalStateManager(private val registry: RouteRegistry) {
    fun dismissModal(modalRoute: ModalRoute) {
        registry.cleanup(listOf(modalRoute.key))
    }
}

// Bad: Keep modal ViewModels around
// (Resource leak if many modals are shown/dismissed)
```

#### Tab Stack Memory
**Problem**: Each tab's back stack holds multiple screens' states

**Solution**:
```kotlin
// Good: Lazy loading for tab content
@Composable
fun TabContent(tabId: String) {
    // Only create composable when tab is selected
    if (currentTabId == tabId) {
        StackContent(stack = stacks[tabId])
    }
}

// Careful: Deep stacks in multiple tabs
// Example: If 5 tabs each with 3-4 screens = 15-20 scopes in memory
// Mitigation: Consider max stack depth per tab, or navigation limits
```

#### Deep Link State
**Problem**: Deep link reconstruction creates temporary objects

**Solution**:
```kotlin
// Good: Immutable, small objects
data class DeepLinkResult(val state: NavigationState)
// Single allocation, then GC eligible

// Careful: Parsing large URLs repeatedly
// Mitigation: Cache parsed results if needed
```

### 9.2 Preventing Navigation Memory Leaks

```kotlin
// ANTI-PATTERN: Lambdas capturing coordinator
@Composable
fun Screen(coordinator: NavigationCoordinator) {
    val onClick = { coordinator.navigateToScreen(...) } // ✅ Correct
    
    // NOT THIS:
    // val onClick = { navigator.navigateToScreen(...) } // ✗ Wrong reference
}

// PATTERN: Use DisposableEffect to cleanup
@Composable
fun Screen(coordinator: NavigationCoordinator) {
    DisposableEffect(Unit) {
        val listener = object : NavigationListener {
            override fun onNavigate(...) { }
        }
        coordinator.addListener(listener)
        
        onDispose {
            coordinator.removeListener(listener) // ✅ Clean up
        }
    }
}

// KOIN SCOPE CLEANUP
// The RouteRegistry should automatically cleanup:
registry.cleanup(activeRoutes) // Call after every navigation
```

### 9.3 Handling Rapid Navigation

**Problem**: User taps button multiple times → multiple navigate calls

**Solution**:
```kotlin
// In NavigationCoordinator:
private var isNavigating = AtomicBoolean(false)

fun navigateToScreen(destination: Destination) {
    if (!isNavigating.compareAndSet(false, true)) {
        return // Already navigating, ignore
    }
    
    _navigationEvents.tryEmit(NavigationEvent.Push(destination))
    
    // Reset flag after short delay (allows animation to complete)
    viewModelScope.launch {
        delay(300) // Animation duration
        isNavigating.set(false)
    }
}
```

Or use Compose's built-in debouncing:
```kotlin
@Composable
fun Screen(coordinator: NavigationCoordinator) {
    var isNavigating by remember { mutableStateOf(false) }
    
    Button(
        onClick = {
            if (!isNavigating) {
                isNavigating = true
                coordinator.navigateToScreen(Destination.Detail)
                // Reset after animation
            }
        }
    ) {
        Text("Go to Detail")
    }
}
```

### 9.4 State Restoration on App Restart

**Problem**: User navigates deep into app, app crashes, need to restore state

**Solution**:
```kotlin
// Persist navigation state
class NavigationPersistence(
    private val context: Context,
    private val coordinator: NavigationCoordinator
) {
    private val preferencesKey = "navigation_state"
    
    fun saveState(state: NavigationState) {
        val json = Json.encodeToString(state)
        context.dataStore.edit { prefs ->
            prefs[preferencesKey] = json
        }
    }
    
    fun restoreState(): NavigationState? {
        return try {
            val json = context.dataStore.data.first()[preferencesKey] ?: return null
            Json.decodeFromString<NavigationState>(json)
        } catch (e: Exception) {
            Log.e("Navigation", "Failed to restore state", e)
            null
        }
    }
}

// In MainActivity:
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val persistence: NavigationPersistence = inject()
    val savedState = persistence.restoreState()
    
    setContent {
        val coordinator: NavigationCoordinator = inject()
        
        // Apply saved state if available
        LaunchedEffect(savedState) {
            savedState?.let { coordinator.applyNavigationState(it) }
        }
        
        val navigationState by coordinator.navigationState.collectAsState()
        AppNavigation(coordinator, navigationState)
    }
}
```

### 9.5 Build Performance Considerations

Your current modularization is excellent for build performance!

**Keep**:
- ✅ Separate `:feature-restaurant` module (good isolation)
- ✅ `:core` for shared code
- ✅ Platform-specific implementations in app layers

**For navigation specifically**:
```
Fast builds because:
1. Navigation is in `:core` (compiles once)
2. Features implement handlers (compile independently)
3. No circular dependencies between features
4. Minimal changes needed for navigation updates
```

**Compile times** (approximate):
- Current: ~45s clean build
- With new navigation: ~50-55s (minimal impact)
- Reason: New classes are small, in core module

---

## Implementation Checklist

### Phase 0: Foundation
- [ ] Create `StackRoute` marker interface
- [ ] Create `ModalRoute` interface
- [ ] Create `ModalDestination` sealed class
- [ ] Create `TabNavigationState` data class
- [ ] Create `NavigationState` data class
- [ ] Create `NavigationReducer` object
- [ ] Extend `NavigationCoordinator` with new methods
- [ ] Write reducer unit tests
- [ ] Verify existing navigation still works

### Phase 1: Modals (Android)
- [ ] Create `ModalLayer.kt` composable
- [ ] Create `ModalComposableBuilder` interface
- [ ] Update `AppNavigation` to render `ModalLayer`
- [ ] Implement `FilterModalRoute` + handler
- [ ] Create `FilterModalComposable`
- [ ] Test modal presentation & dismissal
- [ ] Test back button behavior with modals
- [ ] Write UI tests for modal flow

### Phase 1: Modals (iOS)
- [ ] Create `ModalPresentation.swift`
- [ ] Implement sheet/dialog modifiers
- [ ] Implement `FilterModalRoute` + handler
- [ ] Test modal presentation & dismissal
- [ ] Test back button behavior with modals

### Phase 2: Tabs (Android)
- [ ] Create `TabNavigation.kt` composable
- [ ] Implement `BottomNavigationBar`
- [ ] Create `TabContent.kt` for rendering stacks
- [ ] Test tab switching
- [ ] Test state preservation per tab
- [ ] Test back button in tabbed environment
- [ ] Test modal on top of tabs

### Phase 2: Tabs (iOS)
- [ ] Create `TabNavigationView.swift`
- [ ] Update `TabView` integration
- [ ] Test tab switching
- [ ] Test state preservation per tab

### Phase 3: Deep Links
- [ ] Create `DeepLinkHandler` interface
- [ ] Create `DeepLinkParser` registry
- [ ] Implement `RestaurantDeepLinkHandler`
- [ ] Implement `ReviewsDeepLinkHandler`
- [ ] Android: Create `DeepLinkProcessor`
- [ ] Android: Update `MainActivity` intent handling
- [ ] Android: Update `AndroidManifest.xml`
- [ ] iOS: Create `DeepLinkProcessor`
- [ ] iOS: Update `SceneDelegate`
- [ ] Write deep link parsing tests
- [ ] Write integration tests

### Testing
- [ ] Unit tests: Navigation reducer
- [ ] Unit tests: Deep link parser
- [ ] Integration tests: Modal flow
- [ ] Integration tests: Tab switching
- [ ] E2E tests: Complete user journeys
- [ ] Performance tests: Memory with modals/tabs

---

## FAQ & Troubleshooting

### Q: Should modals be separate from `StackRoute`?
**A**: Yes. Modals have different lifecycle semantics:
- Screens are **replaced** (pop + push)
- Modals are **overlaid** (separate dismiss)

Using separate `ModalRoute` enforces this contract at the type level.

### Q: How do I handle modal→screen communication?
**A**: Use Koin scopes + SharedFlow:
```kotlin
// In modal scope:
class FilterViewModel : ViewModel() {
    private val _filterSelected = MutableSharedFlow<List<String>>()
    val filterSelected = _filterSelected.asSharedFlow()
    
    fun applyFilters(selected: List<String>) {
        _filterSelected.tryEmit(selected)
    }
}

// In restaurant list screen:
@Composable
fun RestaurantListScreen(coordinator: NavigationCoordinator) {
    val filterVM: FilterViewModel = rememberKoinInject()
    
    LaunchedEffect(Unit) {
        filterVM.filterSelected.collect { filters ->
            // Update list with selected filters
        }
    }
}
```

### Q: What if a deep link is invalid?
**A**: Handle gracefully:
```kotlin
when (result) {
    is DeepLinkResult.NotFound -> {
        Log.w("DeepLink", "Invalid deep link: $link")
        // Show toast to user
        coordinator.navigateToRoot() // Fallback to home
    }
    is DeepLinkResult.Error -> {
        Log.e("DeepLink", "Error parsing: $link", result.exception)
        coordinator.navigateToRoot() // Fallback
    }
    // ...
}
```

### Q: How many modals can I stack?
**A**: Technically unlimited, but practically:
- **Recommended**: 1-2 modals at a time
- **Avoid**: More than 3 (confusing UX)
- **Implementation**: No hard limit, but consider UX implications

### Q: Does tabs prevent proper back button behavior?
**A**: No. Back button only affects current tab's stack:
```
User in Tab1: Home → Detail1 → Detail2
User in Tab2: Home → Detail3

User presses back in Tab2:
  → Navigate back in Tab2 (Home)
  
Switch to Tab1:
  → Still shows Detail2 (preserved)
```

---

## Conclusion

This architecture extends your solid current system incrementally:

1. **Phase 0**: Build foundation without breaking changes
2. **Phase 1**: Add modals (most requested feature)
3. **Phase 2**: Add tabs (for apps with multiple sections)
4. **Phase 3**: Add deep links (for notifications/external apps)

**Key Strengths**:
- ✅ Preserves existing `RouteHandler` pattern
- ✅ Maintains feature modularity
- ✅ Supports gradual adoption
- ✅ Platform differences handled cleanly
- ✅ Testable, pure state management

**Next Steps**:
1. Review this document with your team
2. Start Phase 0 (foundation) 
3. Get feedback on ModalRoute design
4. Implement FilterModal as proof of concept
5. Gather metrics: build time, test coverage

Good luck! 🚀
