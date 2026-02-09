# Scoped ViewModel vs Route Holder Registry: Duplication Analysis

**Status**: Analysis Complete  
**Scope**: Determining if scoped ViewModels duplicate route holder registry functionality  
**Conclusion**: YES - Significant duplication. Scoped ViewModels can likely be removed/simplified.

---

## Executive Summary

The current architecture has **two layers managing the same lifecycle concern**:

1. **Scoped ViewModel Layer** (Koin DI + ScopedViewModelHandle)
   - Creates Koin scopes based on `RestaurantDetailScope(restaurantId)`
   - Stores scope reference in `ScopedViewModelHandle`
   - Lifecycle managed manually in ViewModelHolder classes

2. **Route Holder Registry Layer** (iOS/Android)
   - Creates ViewModelHolder objects (iOS) or ScopedViewModelOwner (Android)
   - Stores holders in a dictionary/map
   - Triggers cleanup based on active routes

**Result**: Both systems manage scope creation, caching, and cleanup independently, creating redundancy.

---

## Detailed Analysis

### Layer 1: Scoped ViewModel System (Feature Module)

**Files Involved**:
- `feature-restaurant/RestaurantDetailScope.kt` - Scope identifier
- `feature-restaurant/FeatureRestaurantModule.kt` - Koin scope definition
- `core/ScopedViewModelFactory.kt` - Scope creation helper
- `core/ScopedViewModelHandle.kt` - Holder for scope + ViewModel
- `iosApp/FeatureRestaurantIos.kt` - iOS bridge

**Flow**:

```
Request for Detail ViewModel
         ↓
FeatureRestaurantIos.getRestaurantDetailViewModel(scopeId, restaurantId)
         ↓
ScopedViewModelFactory.scopedViewModel()
         ↓
Koin: getScopeOrNull(scopeId.value)  // "RestaurantDetail_123"
         ↓
IF NOT EXISTS: createScope(qualifier="RestaurantDetailScope")
         ↓
scope.get(RestaurantDetailViewModel::class, params=[restaurantId])
         ↓
NEW RestaurantDetailViewModel(restaurantId, repository)
         ↓
Return: ScopedViewModelHandle(scope, viewModel)
```

**What It Does**:
- ✅ Creates scope per route instance (per restaurantId)
- ✅ Caches scope in Koin's scope map (key: "RestaurantDetail_restaurantId")
- ✅ Passes parameters to ViewModel constructor
- ✅ Returns both scope and viewModel

**What It DOESN'T Do**:
- ❌ Doesn't track which scopes are active
- ❌ Doesn't cleanup based on navigation state
- ❌ Doesn't handle removal from the DI container

---

### Layer 2: Route Holder Registry System (App Module)

**Files Involved**:
- `iosApp/RouteHolderRegistry.swift` - iOS holder cache
- `iosApp/RestaurantDetailViewModelHolder.swift` - iOS holder wrapper
- `androidApp/RouteRegistry.kt` - Android holder cache
- `androidApp/ScopedViewModelOwner.kt` - Android holder wrapper
- `iosApp/NavigationCoordinator.swift` - Cleanup trigger
- `androidApp/AppNavigation.kt` - Cleanup trigger

**Flow**:

**iOS**:
```
Navigation to Detail
         ↓
NavigationCoordinator.handlePush(Destination.RestaurantDetail)
         ↓
router.restaurantDetailHolder(restaurantId)
         ↓
RouteHolderRegistry lookup by key="RestaurantDetail_123"
         ↓
IF NOT EXISTS: Create RestaurantDetailViewModelHolder(restaurantId)
         ↓
RestaurantDetailViewModelHolder INIT:
  - Calls FeatureRestaurantIos.getRestaurantDetailViewModel()
  - Gets ScopedViewModelHandle from Koin
  - Stores scope reference
  - Returns viewModel
         ↓
Store holder in registry: holders[key] = holder
         ↓
(holder stored in view)
         ↓
When route removed: registry.cleanup(activeRoutes)
         ↓
Remove holder: holders[key] = nil
         ↓
RestaurantDetailViewModelHolder.deinit fires
         ↓
scope.close() + viewModel.close()
```

**Android**:
```
Navigation to Detail
         ↓
handleNavigationEvent(Push, Destination.RestaurantDetail)
         ↓
registry.ownerFor(RestaurantDetailRoute)
         ↓
RouteRegistry lookup by key="RestaurantDetail_123"
         ↓
IF NOT EXISTS: createOwner()
  - Creates scope via Koin
  - Gets ViewModel from scope
  - Wraps in ScopedViewModelOwner
         ↓
Store owner: holders[key] = owner
         ↓
When route removed: registry.cleanup(activeRoutes)
         ↓
Remove owner: holders[key]?.close()
         ↓
ScopedViewModelOwner.close():
  - store.clear()
  - scope.close()
```

**What It Does**:
- ✅ Caches holders by route key
- ✅ Reuses existing holders when navigating to same route
- ✅ Tracks which routes are active (in navigation stack)
- ✅ Triggers cleanup when route removed
- ✅ Properly deallocates ViewModels when no longer needed

**What It DOESN'T Do**:
- ❌ Doesn't know about Koin scope metadata
- ❌ Duplicates scope creation logic already in ScopedViewModelFactory

---

## The Duplication

### What's Duplicated?

Both systems manage:

| Concern | Scoped ViewModel | Route Holder Registry |
|---------|---|---|
| **Scope Creation** | ✅ Creates Koin scope | ✅ Also creates Koin scope |
| **Parameter Passing** | ✅ Passes restaurantId to ViewModel | ✅ Passes restaurantId when creating scope |
| **Instance Caching** | ✅ Koin caches scope internally | ✅ Registry caches holder |
| **Lifecycle Binding** | ❌ Manual (scope stored in holder) | ✅ Bound to route active state |

### The Problem

**iOS Path** (what happens now):
```
1. ScopedViewModelFactory creates scope & ViewModel
2. RestaurantDetailViewModelHolder wraps both
3. RouteHolderRegistry caches holder
4. When holder deallocated → scope closed

Result: TWO cache layers - Koin + Registry
```

**Android Path** (what happens now):
```
1. ScopedViewModelFactory creates scope & ViewModel
2. RouteRegistry.createOwner() creates ScopedViewModelOwner
3. ScopedViewModelOwner wraps scope & ViewModelStore
4. RouteRegistry caches owner
5. When owner.close() → scope closed

Result: TWO cache layers - Koin + Registry
```

**The Issue**: 
- Koin already caches scopes by ID
- RouteRegistry also caches the same scopes (via holders)
- Two different systems managing the same lifecycle

---

## What Each System Provides

### Scoped ViewModel System PROVIDES:
- ✅ Type-safe scope definition (`RestaurantDetailScope`)
- ✅ Automatic Koin scope creation
- ✅ Parameter injection into ViewModel
- ✅ Portable across platforms (iOS + Android)
- ✅ Testable scope/ViewModel unit

### Scoped ViewModel System LACKS:
- ❌ Active route tracking
- ❌ Cleanup triggers based on navigation state
- ❌ Instance caching beyond Koin's scope cache
- ❌ Multi-instance deduplication (can't prevent duplicate holders for same route)

### Route Holder Registry PROVIDES:
- ✅ Route-aware lifecycle (knows when route is active/inactive)
- ✅ Active route tracking Set<String>
- ✅ Smart cleanup based on navigation state
- ✅ Deduplication (one holder per route key)
- ✅ Platform-specific lifecycle management

### Route Holder Registry LACKS:
- ❌ Scope metadata knowledge
- ❌ Parameter injection
- ❌ Feature module isolation (couples app to feature details)

---

## Lifecycle Comparison

### Current iOS Lifecycle

```
Time    | Koin Scope | Holder | ViewModel | Route Active
--------|------------|--------|-----------|---------------
T0      | —          | —      | —         | No
T1      | CREATE     | CREATE | CREATE    | Yes (Route in stack)
T2      | EXISTS     | EXISTS | EXISTS    | Yes (Same route used)
T3      | EXISTS     | REMOVED| CLOSE     | No (Route popped)
        | (orphaned) |        |           |
T4      | N/A        | —      | —         | No
```

**Problem at T3**: 
- Holder deallocates and closes scope ✓
- But if another view holds reference to same scope key, Koin still has it in cache
- Could cause issues if app tries to recreate with same key

---

## Can Scoped ViewModels Be Removed?

### Answer: YES, mostly - But with architectural restructuring

**Option A: Keep Only Route Holder Registry** ❌ NOT VIABLE
- Registry would need to become a generic holder factory
- Would require moving DI logic into app module
- Loss of feature module isolation
- Complex refactoring

**Option B: Enhance Route Holder Registry** ✅ VIABLE (Best)
- Have registry delegate scope creation to feature modules
- Feature provides `ScopeFactory` interface instead of manual creation
- Registry becomes thin lifecycle manager
- Keeps feature isolation
- Eliminates redundant caching

**Option C: Eliminate Route Registry, Use Only Scoped ViewModels** ✅ PARTIALLY VIABLE
- Rely on Koin's scope cache directly
- Problem: No way to track active routes
- Koin scopes would leak over time
- Would need external cleanup trigger

### Recommendation: Option B

**What to do**:

1. **Keep** the `RestaurantDetailScope` and `ScopedViewModelFactory` pattern
2. **Remove** the `RestaurantDetailViewModelHolder` wrapper
3. **Enhance** `RouteHolderRegistry`/`RouteRegistry` to:
   - Accept a scope ID and parameters
   - Call feature module's scope factory
   - Get ViewModel from returned scope
   - Just manage lifecycle, not scope creation

**New Flow**:

```
iOS Before:
restaurantDetailHolder(id)
  → RouteHolderRegistry creates holder
  → Holder calls FeatureRestaurantIos.getRestaurantDetailViewModel()
  → Holder stores scope reference
  → Returns holder to view

iOS After:
Let registry = RouteHolderRegistry()
scope = registry.ownerFor(
  Route.restaurantDetail(id),
  scopeFactory = { RestaurantDetailScope(it) },
  vmBuilder = { FeatureRestaurantIos.getRestaurantDetailViewModel(...) }
)
viewModel = scope.viewModel
```

**Benefit**:
- Removes holder wrapper class (per route type)
- Keeps feature module isolation
- Registry focused on lifecycle only
- Single source of truth for scope management

---

## Current Inefficiencies

### Duplication List

| # | What | Scoped VM | Route Holder | Can Remove? |
|---|------|-----------|--------------|-------------|
| 1 | Scope creation | RestaurantDetailScope + factory | RouteRegistry.createOwner() | 🟡 Keep factory in feature, remove from registry |
| 2 | Parameter passing | Params list in handle | Registry passes to createOwner | 🟡 Keep in factory |
| 3 | Instance caching | Koin scope cache | holders[key] dictionary | 🔴 Can't remove both (need one) |
| 4 | Lifecycle binding | Manual in holder | Route active tracking | 🟢 Keep route tracking only |
| 5 | Holder wrapper | RestaurantDetailViewModelHolder | ScopedViewModelOwner | 🟢 **CAN REMOVE** |

### Classes That Could Be Removed/Simplified

**iOS** (can potentially remove):
- ❌ `RestaurantListViewModelHolder` - Just call `FeatureRestaurantIos.getRestaurantListViewModelIos()` directly
- ❌ `RestaurantDetailViewModelHolder` - Registry should handle this
- ✅ Keep: `FeatureRestaurantIos` (bridge/factory)
- ✅ Keep: `ScopedViewModelFactory` (scope creation)

**Android** (minimal removal needed):
- ✅ Keep: `RouteRegistry` (lifecycle manager) 
- ✅ Keep: `ScopedViewModelOwner` (lifecycle wrapper)
- 🟡 Simplify: `createOwner()` to delegate to feature factories

**Feature** (keep as-is):
- ✅ Keep: `RestaurantDetailScope`
- ✅ Keep: `FeatureRestaurantModule`
- ✅ Keep: `ScopedViewModelFactory`

---

## Complexity Assessment

### Current Lines of Code

```
iOS Holder Pattern:
- RestaurantListViewModelHolder.swift      (11 lines)
- RestaurantDetailViewModelHolder.swift    (27 lines)
- RouteHolderRegistry.swift                (50 lines)
  Subtotal: 88 lines of iOS-specific holder management

Android Owner Pattern:
- ScopedViewModelOwner.kt                  (22 lines)
- RouteRegistry.kt                         (72 lines)
  Subtotal: 94 lines of Android-specific registry logic

Feature Module:
- RestaurantDetailScope.kt                 (10 lines)
- FeatureRestaurantModule.kt               (35 lines)
- ScopedViewModelFactory.kt                (41 lines)
- ScopedViewModelHandle.kt                 (8 lines)
  Subtotal: 94 lines of shared scope pattern

TOTAL: 276 lines of code managing ViewModels + Scopes
```

### Potential Reduction

If we eliminate holder wrappers and consolidate:
```
iOS: -38 lines (remove both holders)
Android: -22 lines (simplify owner)
Feature: -15 lines (simplify factory)

New Total: ~201 lines (-27% reduction)
```

---

## Conclusion

### Key Findings

1. **YES, there IS duplication**
   - Scoped ViewModels create scopes + cache in Koin
   - Route Holder Registry creates holders + caches in registry
   - Both manage same lifecycle concern from different angles

2. **Scoped ViewModels CANNOT be removed entirely**
   - They provide essential scope definition and parameter injection
   - Core abstraction that features need to follow

3. **Holder Wrappers CAN be removed/simplified** ✅
   - `RestaurantDetailViewModelHolder` is redundant wrapper
   - Could be replaced with direct delegate to `scopedViewModel()`
   - Would reduce boilerplate by ~38 lines on iOS

4. **Route Registry is NECESSARY**
   - Provides active route tracking (essential for cleanup)
   - Manages lifecycle based on navigation state
   - Cannot be replaced by Koin scope cache alone

5. **Best Path Forward**
   - Keep: Scoped ViewModel pattern in features
   - Keep: Route Registry for lifecycle management
   - Remove: Holder wrapper classes (save code)
   - Enhance: Registry to be generic over scope factories

### Final Assessment

**Can scoped ViewModels be removed?**

❌ **NO** - They are necessary for:
- Type-safe scope definitions
- Parameter injection
- Feature module isolation

**Should they be simplified?**

✅ **YES** - Remove the holder wrapper layer:
- Eliminate per-route-type holder classes
- Have registry call scope factory directly
- Keep feature isolation + route tracking
- Result: Cleaner architecture, less boilerplate

---

## Recommendation for Implementation

**Phase 1: Enhance Route Registry to Accept Scope Factories**

```kotlin
// Android
class RouteRegistry(
    private val scopeFactories: Map<String, ScopeFactory>
) {
    fun ownerFor(route: Route): ScopedViewModelOwner {
        val factory = scopeFactories[route.javaClass.simpleName]
            ?: throw IllegalArgumentException("No factory for ${route.javaClass}")
        val scope = factory.createScope(route)
        return ScopedViewModelOwner(scope)
    }
}
```

**Phase 2: Remove Holder Wrapper Classes**

```swift
// iOS - BEFORE
let holder = navigator.restaurantDetailHolder(restaurantId: restaurantId)
RestaurantDetailView(viewModel: holder.viewModel, holder: holder)

// iOS - AFTER
let scopeHandle = navigator.getScopeFor(Route.restaurantDetail(restaurantId))
RestaurantDetailView(viewModel: scopeHandle.viewModel, scopeHandle: scopeHandle)
```

**Phase 3: Update Feature Module to Provide Factories**

```kotlin
// Feature module provides factory
object RestaurantScopeFactories {
    val detail: (RestaurantDetailRoute) -> Scope = { route ->
        scopedViewModel(
            RestaurantDetailViewModel::class,
            RestaurantDetailScope(route.restaurantId),
            listOf(route.restaurantId)
        ).scope
    }
}
```

This approach:
- ✅ Eliminates holder wrapper duplication
- ✅ Keeps feature isolation
- ✅ Maintains route-aware cleanup
- ✅ Reduces complexity

---

## CRITICAL FINDING: Registry Holder Does NOT Replace Scoped ViewModel System

### The Question
**Do registry holders serve the same lifecycle management function as scoped ViewModels, or are they complementary?**

### The Answer
**They are COMPLEMENTARY but DISTINCT in function:**

#### Scoped ViewModel System (Koin DI Layer) — **Core Responsibility: State Ownership**

The scoped ViewModel system's purpose is to:
1. **Own state across different lifecycles** — A KMP ViewModel instance should NOT be tied to iOS `@StateObject` lifecycle or Android `ViewModelStore` lifecycle
2. **Define scope boundaries** — `RestaurantDetailScope(restaurantId)` says "create one scope per restaurant, parameterized by ID"
3. **Inject parameters** — The scope knows how to construct ViewModels with specific parameters (restaurantId, userId, etc.)
4. **Be platform-agnostic** — The scope definition lives in `core/commonMain/` and works identically on iOS and Android

**Key Code:**
```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/core/viewmodel/ScopedViewModelFactory.kt
fun <VM : ScopedViewModel> scopedViewModel(
    vmClass: KClass<VM>,
    scopeId: KmpScopeId,
    params: List<Any?> = emptyList()
): ScopedViewModelHandle<VM> {
    val koin = KoinPlatform.getKoin()
    
    // KOIN OWNS THIS SCOPE — tied to scope ID, not to any platform lifecycle
    val scope = koin.getScopeOrNull(scopeId.value)
        ?: koin.createScope(
            scopeId = scopeId.value,
            qualifier = named(scopeId.qualifierName)
        )
    
    val viewModel: VM = scope.get(vmClass) {
        parametersOf(*params.toTypedArray())
    }
    
    return ScopedViewModelHandle(
        scope = scope,
        viewModel = viewModel
    )
}
```

#### Registry Holder System (Platform Navigation Layer) — **Core Responsibility: Lifecycle Tracking**

The registry holder system's purpose is to:
1. **Bridge KMP to platform navigation** — Connect route/navigation state to scope lifecycle
2. **Track active routes** — Maintain a set of which routes are currently in the navigation stack
3. **Trigger cleanup** — When a route is popped, signal to the registry to close the associated scope
4. **Survive configuration changes** — Hold references so ViewModels don't get deallocated during iOS rotation or Android config changes

**Key Code:**

**iOS:**
```swift
// iosApp/iosApp/Navigation/NavigationCoordinator.swift
func handle(event: NavigationEvent) {
    switch event {
    case is NavigationEvent.Pop:
        if !path.isEmpty { path.removeLast() }
        if !routeStack.isEmpty { routeStack.removeLast() }
        updateActiveRoutes()
        syncCleanup()  // ← TRIGGER CLEANUP
    case is NavigationEvent.PopToRoot:
        path = NavigationPath()
        routeStack.removeAll()
        activeRoutes.removeAll()
        registry.cleanup(activeRoutes: [])  // ← TRIGGER CLEANUP
    }
}

// When activeRoutes changes, registry compares with stored holders
registry.cleanup(activeRoutes: activeRoutes)
// Holder deinit calls: scope.close() + viewModel.close()
```

**Android:**
```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/navigation/AppNavigation.kt
LaunchedEffect(navController) {
    navController.addOnDestinationChangedListener { _, destination, _ ->
        registry.cleanup(trackedRoutes.value)  // ← TRIGGER CLEANUP
    }
}

// When trackedRoutes changes, registry calculates inactiveKeys
registry.cleanup(trackedRoutes.value)
// Inactive owners call: store.clear() + scope.close()
```

### Why Both Are Necessary

#### Problem Without Scoped ViewModels (Koin alone):
```
// If we only had Koin without scoped ViewModels
val viewModel = koin.get<RestaurantDetailViewModel>()  // ERROR!
// How do we know which restaurantId this ViewModel should use?
// We need a scope to inject parameters per-instance.
// Raw Koin.get() would return a singleton.
```

#### Problem Without Registry Holders (iOS ARC issue):
```swift
// If we only had Koin scopes without registry holders
let holder = navigator.restaurantDetailHolder(restaurantId: restaurantId)
RestaurantDetailView(
    viewModel: holder.viewModel
    // holder is NOT STORED anywhere
)

// What happens:
// 1. holder variable goes out of scope in destinationView()
// 2. Swift ARC immediately calls holder.deinit
// 3. deinit calls scope.close()
// 4. ViewModel is deallocated BEFORE RestaurantDetailView can use it!
// 5. View tries to access viewModel → CRASH
```

#### Problem Without Registry Holders (Android lifecycle issue):
```kotlin
// If we only had Koin scopes without registry
val owner = registry.ownerFor(route)
RestaurantDetailScreen(restaurantId, coordinator)

// What happens:
// 1. Configuration change (rotation)
// 2. Composable recomposes
// 3. registry.ownerFor() is called again
// 4. Koin scope is recreated with new instances
// 5. ViewModel loses state mid-action
```

### Lifecycle Diagram: How Both Layers Work Together

```
┌─────────────────────────────────────────────────────────────────┐
│ ROUTE LAYER (Platform Navigation)                               │
│                                                                   │
│ NavigationCoordinator / AppNavigation                            │
│   ↓                                                              │
│   User taps Detail route                                        │
│   ↓                                                              │
│   routeStack.append(Route.restaurantDetail("123"))              │
│   activeRoutes.insert("RestaurantDetail_123")                   │
└─────────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│ REGISTRY LAYER (Holder Management)                               │
│                                                                   │
│ RouteHolderRegistry / ScopedViewModelOwner                       │
│   ↓                                                              │
│   registry.restaurantDetailHolder("123")                        │
│   ↓                                                              │
│   IF holder not cached:                                          │
│     Create new holder                                            │
└─────────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│ SCOPED VIEWMODEL LAYER (Koin DI)                                │
│                                                                   │
│ RestaurantDetailViewModelHolder.init()                          │
│   ↓                                                              │
│   FeatureRestaurantIosKt.getRestaurantDetailViewModel()         │
│   ↓                                                              │
│   scopedViewModel(                                              │
│     vmClass = RestaurantDetailViewModel::class,                 │
│     scopeId = RestaurantDetailScope("123"),  ← SCOPES OWNS ID   │
│     params = ["123"]                                            │
│   )                                                              │
│   ↓                                                              │
│   Koin: getScopeOrNull("RestaurantDetail_123")                  │
│     ↓ Not found, create scope                                  │
│     createScope(                                                │
│       scopeId = "RestaurantDetail_123",                         │
│       qualifier = "RestaurantDetailScope"                       │
│     )                                                            │
│   ↓                                                              │
│   scope.get(RestaurantDetailViewModel::class) {                 │
│     parametersOf("123")  ← PARAMETERS INJECTED                  │
│   }                                                              │
│   ↓                                                              │
│   Return ScopedViewModelHandle(scope, viewModel)                │
└─────────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│ VIEW LAYER (Uses ViewModel)                                      │
│                                                                   │
│ RestaurantDetailView                                             │
│   @State private var uiState: RestaurantDetailUiState          │
│   let holder: RestaurantDetailViewModelHolder  ← KEEPS ALIVE    │
│   let viewModel: RestaurantDetailViewModel                      │
│   ↓                                                              │
│   .task { await observe(viewModel) }                            │
└─────────────────────────────────────────────────────────────────┘
                         ↓ (user navigates back)
┌─────────────────────────────────────────────────────────────────┐
│ CLEANUP PHASE                                                    │
│                                                                   │
│ NavigationCoordinator.handle(NavigationEvent.Pop)               │
│   ↓                                                              │
│   routeStack.removeLast()  ← Route removed                      │
│   updateActiveRoutes()     ← activeRoutes recalculated         │
│   syncCleanup()                                                  │
│   ↓                                                              │
│ RouteHolderRegistry.cleanup(activeRoutes: Set)                  │
│   ↓                                                              │
│   inactiveKeys = holders.keys - activeRoutes  ← "RestaurantDetail_123" inactive
│   ↓                                                              │
│   holders["RestaurantDetail_123"] = nil  ← HOLDER RELEASED     │
│   ↓                                                              │
│ Swift/Kotlin calls RestaurantDetailViewModelHolder.deinit()     │
│   ↓                                                              │
│   scope.close()     ← Koin scope destroyed                      │
│   viewModel.close() ← ViewModel destroyed                       │
│   ↓                                                              │
│   Koin scope for "RestaurantDetail_123" destroyed              │
└─────────────────────────────────────────────────────────────────┘
```

### Conclusion

**Scoped ViewModels and Registry Holders are NOT duplicates — they handle DIFFERENT concerns:**

| System | Owns | Manages | Tied To | Purpose |
|--------|------|---------|---------|---------|
| **Scoped ViewModel** | ViewModel instance | Parameter injection | Koin scope ID | Define what state to create and how to initialize it |
| **Registry Holder** | Holder reference | Lifecycle timing | Navigation stack | Decide WHEN to create and destroy that state |

**Why we CANNOT remove Scoped ViewModels:**
- They define HOW to create a ViewModel (parameters, scope definition)
- They separate concern of "what a ViewModel needs" from "when does it live"
- They're platform-agnostic (live in `commonMain`)

**Why we CANNOT remove Registry Holders:**
- They decide WHEN to create/destroy holders based on navigation
- They prevent premature deallocation (iOS ARC issue)
- They track active routes for cleanup signals
- They're platform-specific (iOS/Android have different ARC/lifecycle semantics)

**The "duplication" mentioned earlier was a MISDIAGNOSIS:**
- It's not duplication of responsibility — it's separation of concerns
- Each layer has a distinct, irreplaceable job
- Trying to merge them would create a single system doing two things poorly

---

## Updated Recommendation

**REVERSE the previous conclusion:**

❌ **DO NOT remove scoped ViewModels** — they are essential and serve a different purpose than registry holders.

✅ **INSTEAD, clarify the architecture in documentation** to explain why both exist and what each does.

✅ **Optional future improvement:** The only minor optimization would be to make registry holders more generic/template-based rather than per-route-type, but this is code organization, not a correctness issue.

### Why This Matters

Understanding that these are **complementary, not redundant** helps the team:
1. **Avoid premature optimization** — Trying to "merge" them would break things
2. **Understand the design rationale** — KMP owns state definition, platforms own lifecycle timing
3. **Make future architecture decisions** — When adding new routes, both layers stay
4. **Debug lifecycle issues** — Know which layer owns which problem

This is actually **good architecture** — separation of concerns working as intended.

