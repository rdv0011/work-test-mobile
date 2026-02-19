# Scoped ViewModel vs Route Registry: Ownership & Lifecycle Analysis

**Status:** Analysis Complete

**Scope:** Clarifying responsibilities, ownership, and lifecycle management between Scoped ViewModels and Route Registry

**Final Conclusion:** No conceptual duplication. One mechanical overlap exists and must be corrected by centralizing lifetime ownership in the Route Registry while keeping Scoped ViewModels as factories.

<details>
<summary>Executive Summary</summary>

The architecture contains two cooperating layers that are often mistaken as duplicates:

*   **Scoped ViewModel System (Feature / DI layer)**
    *   Defines what state exists and how it is created.
*   **Route Registry System (App / Navigation layer)**
    *   Decides when that state lives and when it must be destroyed.

The earlier conclusion that these systems “duplicate lifecycle management” was partially incorrect.

**Corrected Finding:**

*   There is no duplication of responsibility.
*   There is mechanical overlap caused by shared scope retention.
*   The fix is not removal, but clear ownership boundaries.

Features define WHAT to create.
Registry defines WHEN it lives.
Koin wires dependencies, but does not own lifecycle.

**Mental Model (Correct)**

| Concern                 | Owner            |
| :---------------------- | :--------------- |
| Scope definition        | Feature module   |
| Parameter declaration   | Feature module   |
| ViewModel construction  | Feature module   |
| Scope lifetime          | Route Registry   |
| Cleanup timing          | Route Registry   |
| Platform lifecycle bridging | Route Registry   |

</details>

## Layer 1: Scoped ViewModel System (Feature / DI Layer)

### Purpose

The Scoped ViewModel system is a factory layer, not a lifecycle manager.

### Responsibilities

✅ Define scope identity (e.g. `RestaurantDetailScope(restaurantId)`)
✅ Declare how ViewModels are constructed
✅ Inject parameters in a type-safe way
✅ Remain platform-agnostic (`commonMain`)
✅ Enable feature isolation and testability

### What It Must NOT Own

❌ Route awareness
❌ Navigation state
❌ Lifetime decisions
❌ Long-lived scope retention

### Correct Role of `scopedViewModel()`

```kotlin
fun <VM : Any> scopedViewModel(
    vmClass: KClass<VM>,
    scopeId: KmpScopeId,
    params: List<Any?> = emptyList()
): ScopedViewModelHandle<VM>
```

This function should be understood as:

“Given a scope identity and parameters, construct or retrieve a ViewModel from a scope.”

It is **not**:

*   a lifecycle owner
*   a navigation-aware component
*   a cleanup authority

## Layer 2: Route Registry (Navigation / Lifecycle Layer)

### Purpose

The Route Registry is the single source of truth for lifetime ownership.
It answers:

*   Is this route currently active?
*   Should its state still exist?
*   When must cleanup happen?

### Responsibilities

✅ Track active routes
✅ Deduplicate route instances
✅ Own the scope reference
✅ Decide when cleanup occurs
✅ Bridge platform lifecycle differences (iOS / Android)

### What It Must Own (Exclusively)

If a component can close a scope, it must be the only component holding it.
Therefore, the Route Registry must:

*   retain the scope reference
*   close it deterministically
*   never share lifetime ownership with Koin caches or feature code

<details>
<summary>Where the Confusion Came From</summary>

### The Mechanical Overlap

Previously, both layers retained references to the same scope:

*   Koin cached the scope by ID
*   Registry cached a holder that also retained the scope

This created the illusion of duplication.

### The Actual Problem

*   Not duplicated responsibility — shared ownership.
*   That is a bug risk, not a design intent.

### Corrected Interpretation of the Timeline

**Previous Misinterpretation:**
“Koin still tracks the scope independently, so duplication may be harmless.”

**Correct Interpretation:**
If two systems can keep a scope alive independently, neither truly owns its lifecycle. This creates:

*   orphaned scopes
*   resurrected state
*   non-deterministic cleanup (especially on iOS ARC)

</details>

## Correct Architectural Boundary

**Scoped ViewModel System SHOULD:**

*   Define scope type
*   Define parameters
*   Define ViewModel factory
*   Be stateless beyond construction

**Route Registry SHOULD:**

*   Create the scope using feature factories
*   Retain the scope reference
*   Close the scope when route becomes inactive
*   Be the only lifetime owner

<details>
<summary>Revised Answer to Key Questions</summary>

❓ **Do Scoped ViewModels and Route Registry duplicate each other?**
❌ No — not conceptually
✅ They address orthogonal concerns:
    *   Definition vs lifetime
    *   Construction vs timing

❓ **Is one of them removable?**
❌ No
Removing either breaks correctness:
    *   Without Scoped ViewModels → no parameterized, isolated state
    *   Without Registry → no deterministic cleanup

❓ **What actually needs to change?**
Ownership, not existence.

</details>

## Corrected Options Analysis

*   **Option A: Only Route Registry** ❌
    *   Breaks feature isolation and DI boundaries.
*   **Option C: Only Scoped ViewModels** ❌
    *   No route awareness → leaks and premature deallocation.
*   **Option B: Registry owns lifetime, features provide factories** ✅ Correct

## Corrected Recommendation

**Keep (Unchanged)**

*   `RestaurantDetailScope`
*   Feature-level scope definitions
*   Scoped ViewModel factory pattern
*   Route Registry

**Change (Important)**

*   Treat Scoped ViewModels as pure factories
*   Ensure only the Route Registry retains scope references
*   Remove the assumption that Koin’s internal cache is a lifecycle owner

**Simplify (Optional)**

*   Replace per-route holder classes with a generic `RouteLifetime`
*   Keep Android `ViewModelStoreOwner` inside that lifetime
*   iOS holders become thin lifetime anchors (or implicit via registry)

## Minimal Correct Design

### Registry-Owned Lifetime

```kotlin
class RouteLifetime(
    val scope: Scope
) {
    fun clear() {
        scope.close()
    }
}
```

### Registry Owns Lifetimes

```kotlin
class RouteRegistry {
    private val lifetimes = mutableMapOf<String, RouteLifetime>()

    fun lifetimeFor(route: Route, factory: () -> Scope): RouteLifetime =
        lifetimes.getOrPut(route.key) {
            RouteLifetime(factory())
        }

    fun cleanup(activeRoutes: Set<String>) {
        val inactive = lifetimes.keys - activeRoutes
        inactive.forEach { key ->
            lifetimes.remove(key)?.clear()
        }
    }
}
```

### Feature Provides Factory Only

```kotlin
fun restaurantDetailScopeFactory(route: RestaurantDetailRoute): Scope {
    return scopedViewModel(
        RestaurantDetailViewModel::class,
        RestaurantDetailScope(route.restaurantId),
        listOf(route.restaurantId)
    ).scope
}
```

## Final Corrected Conclusion

Scoped ViewModels are not lifecycle owners.
Route Registry is the single lifetime owner.
There is no duplication of responsibility.
The only fix required is centralizing scope ownership.

Features define WHAT exists.
Registry defines WHEN it exists.

That separation is correct — and scalable.

**One Rule to Preserve This Architecture:**

If two systems both think they own a scope, neither does.
Ownership must be singular, explicit, and route-driven.

---

# Implementation Plan

## Phase 1: Audit & Understanding

### 1.1 Current State Inventory
- [ ] Map all existing `RestaurantDetailScope`, `RestaurantListScope`, filter scopes
- [ ] Identify all scope holders (feature modules, screens, ViewModels)
- [ ] Document Koin cache retention points
- [ ] Find RouteRegistry implementation and scope cleanup logic
- [ ] Verify Android `ViewModelStoreOwner` integration points
- [ ] Check iOS lifetime management (ARC, property retention)

**Output:** Spreadsheet mapping scope type → current owner(s) → cleanup point

### 1.2 Lifecycle Trace (Following a route)
- [ ] Trace route creation → scope creation → ViewModel access → route destruction
- [ ] Document where Koin cache entry persists after screen dismissal
- [ ] Identify if Route Registry is actually called on cleanup
- [ ] Check for orphaned scopes not closed by Registry

**Output:** Sequence diagram showing the full lifecycle

---

## Phase 2: Refactor Route Registry as Single Lifetime Owner

### 2.1 Create Generic `RouteLifetime` Holder

**File:** `commonMain/navigation/RouteLifetime.kt`

```kotlin
/**
 * Single ownership point for route-scoped state.
 * Every active route has exactly one RouteLifetime.
 */
class RouteLifetime(
    private val routeId: String,
    val scope: Scope
) {
    fun close() {
        scope.close()
    }
    
    override fun toString() = "RouteLifetime($routeId)"
}
```

**Why:**
- Explicit ownership marker
- Platform-agnostic
- Single responsibility: hold scope, provide cleanup

### 2.2 Refactor Route Registry

**File:** `commonMain/navigation/RouteRegistry.kt`

```kotlin
class RouteRegistry {
    private val _lifetimes = mutableMapOf<String, RouteLifetime>()
    val lifetimes: Map<String, RouteLifetime> = _lifetimes
    
    fun lifetimeFor(
        routeId: String,
        factory: () -> Scope
    ): RouteLifetime {
        return _lifetimes.getOrPut(routeId) {
            RouteLifetime(routeId, factory())
        }
    }
    
    fun cleanup(activeRouteIds: Set<String>) {
        val toRemove = _lifetimes.keys - activeRouteIds
        toRemove.forEach { key ->
            _lifetimes.remove(key)?.close()
        }
    }
    
    fun clearAll() {
        _lifetimes.values.forEach { it.close() }
        _lifetimes.clear()
    }
}
```

**Responsibilities:**
- ✅ Retain scope references
- ✅ Close scopes deterministically
- ✅ Own lifetime decisions
- ❌ NOT defining scopes (features do that)

**Tests:**
- [ ] Verify scope NOT closed while route is active
- [ ] Verify scope IS closed when route becomes inactive
- [ ] Verify calling cleanup with empty set closes all
- [ ] Verify second access to same route reuses scope

### 2.3 Hook Registry Into Platform Lifecycle

**Android (Jetpack Compose Navigation):**

```kotlin
// In NavHost or similar
LaunchedEffect(navBackStackEntry) {
    val routeId = currentRoute.id
    val factory = { createScopeForRoute(currentRoute) }
    val lifetime = routeRegistry.lifetimeFor(routeId, factory)
    
    onDispose {
        routeRegistry.cleanup(calculateActiveRoutes())
    }
}
```

**iOS (SwiftUI Navigation):**

```swift
// In NavigationStack / NavigationPath observer
.onDisappear {
    routeRegistry.cleanup(activeRoutes: calculateActiveRoutes())
}
```

**Tests:**
- [ ] Verify cleanup called on Android back press
- [ ] Verify cleanup called on iOS pop/dismiss
- [ ] Verify no scope remains after full navigation stack clear

---

## Phase 3: Retrofit Features as Scope Factories

### 3.1 Establish Feature Factory Pattern

**File:** `commonMain/feature/restaurant/detail/RestaurantDetailScopeFactory.kt`

```kotlin
object RestaurantDetailScopeFactory {
    fun createScope(restaurantId: String): Scope {
        return koinScope {
            scopedModule {
                single { restaurantId }
                single { RestaurantDetailViewModel(...) }
                // other dependencies
            }
        }
    }
}
```

**Why:**
- Features remain stateless
- Scopes created on demand (by Registry only)
- No scope retention in feature module
- Testable in isolation

### 3.2 Update Screen Composables to Use Registry

**Before (Feature owns scope):**

```kotlin
@Composable
fun RestaurantDetailScreen(restaurantId: String) {
    val viewModel = getViewModelForRoute(restaurantId)  // ❌ unclear ownership
    // ...
}
```

**After (Registry owns lifetime):**

```kotlin
@Composable
fun RestaurantDetailScreen(restaurantId: String) {
    val registry = LocalRouteRegistry.current
    val routeId = "detail-$restaurantId"
    
    val lifetime = registry.lifetimeFor(routeId) {
        RestaurantDetailScopeFactory.createScope(restaurantId)
    }
    
    val viewModel = lifetime.scope.get<RestaurantDetailViewModel>()
    // ...
}
```

**Or (Using composition local for cleaner API):**

```kotlin
@Composable
fun RestaurantDetailScreen(restaurantId: String) {
    val viewModel = rememberScopedViewModel(
        routeId = "detail-$restaurantId",
        vmClass = RestaurantDetailViewModel::class,
        factory = { RestaurantDetailScopeFactory.createScope(restaurantId) }
    )
    // ...
}
```

**Tests:**
- [ ] Verify ViewModel re-retrieved from same lifetime
- [ ] Verify new route creation creates new ViewModel
- [ ] Verify state survives screen recomposition

### 3.3 Remove Per-Feature Holders

- [ ] Delete `RestaurantDetailHolder`
- [ ] Delete `RestaurantListHolder`
- [ ] Delete any `*ViewModel` holder classes
- [ ] Update imports in all consumers

**What stays:**
- `RestaurantDetailScope` (type definition)
- `RestaurantDetailViewModel` (just the ViewModel)
- Scope factory functions

---

## Phase 4: Correct Scoped ViewModel Utilities

### 4.1 Clarify `scopedViewModel()` Intent

**Current problem:** Is it a lifecycle owner or a factory accessor?

**New role:** Pure accessor, assumes scope already exists.

```kotlin
/**
 * Access or construct a ViewModel from an existing scope.
 * 
 * IMPORTANT: The scope must be created and held by the caller (Route Registry).
 * This function does NOT own or manage the scope lifetime.
 */
fun <VM : Any> scopedViewModel(
    vmClass: KClass<VM>,
    scopeId: KmpScopeId,
    params: List<Any?> = emptyList()
): ScopedViewModelHandle<VM> {
    val scope = Koin.getInstance().getScopeOrNull(scopeId)
        ?: error("Scope $scopeId not found. Route Registry must create it first.")
    return ScopedViewModelHandle(scope.get(vmClass, parameters = params))
}
```

**Key changes:**
- Error on missing scope (don't create implicitly)
- Document that Registry must own scope
- Make it clear this is a getter, not an owner

### 4.2 Remove Auto-Creation from Koin Cache

**If present:** Remove any mechanism that lets `scopedViewModel()` create scopes.

**Current risk:** `scopedViewModel()` creates scope, Registry doesn't know about it.

**Fix:** Always error when scope missing. Registry must create first.

---

## Phase 5: Verify Cleanup Determinism

### 5.1 Android Backstack Integration

- [ ] Hook Route Registry into NavBackStackEntry lifecycle
- [ ] Verify scope closed exactly when screen removed from backstack
- [ ] Test back button closes current scope
- [ ] Test up navigation closes current scope
- [ ] Test process death doesn't leave unclosed scopes

### 5.2 iOS Navigation Integration

- [ ] Hook Route Registry into NavigationStack dismiss
- [ ] Verify scope closed when view is popped
- [ ] Test that ARC doesn't keep scope alive unexpectedly
- [ ] Test state is truly released (memory profiler)

### 5.3 Lifecycle Bridge Tests

**Test case: Open Detail → Open Sub-detail → Back → Back**

- [ ] DetailScope created when Detail opened
- [ ] SubDetailScope created when SubDetail opened
- [ ] DetailScope NOT closed when SubDetail created (still active)
- [ ] SubDetailScope closed when SubDetail popped
- [ ] DetailScope still exists after SubDetail cleanup
- [ ] DetailScope closed when Detail popped

---

## Phase 6: Remove Koin Cache as Lifetime Owner

### 6.1 Audit Koin Scope Cache

- [ ] Find where Koin holds scope references
- [ ] Verify Registry is the only long-term holder
- [ ] Check if Koin cache can keep orphaned scopes alive

### 6.2 Ensure Registry ↔ Koin Coordination

- [ ] When Registry closes a scope, Koin removes it from cache
- [ ] When Registry creates a scope, Koin stores it (temporary)
- [ ] When Registry cleanup runs, Koin cache is emptied for those scopes

**Pattern:**

```kotlin
fun lifetimeFor(routeId: String, factory: () -> Scope): RouteLifetime {
    return _lifetimes.getOrPut(routeId) {
        val scope = factory()
        // At this point, Koin might cache it
        // We are the owners now. Koin is just holding it on our behalf.
        RouteLifetime(routeId, scope)
    }
}

fun cleanup(activeRouteIds: Set<String>) {
    val toRemove = _lifetimes.keys - activeRouteIds
    toRemove.forEach { key ->
        _lifetimes.remove(key)?.let { lifetime ->
            lifetime.close()
            Koin.getInstance().deleteScope(key)  // Also remove from Koin
        }
    }
}
```

---

## Phase 7: Documentation & Team Alignment

### 7.1 Update Architecture Docs

- [ ] Add "Ownership Model" section to architecture guide
- [ ] Document Route Registry as single lifetime owner
- [ ] Document feature factories as stateless scope creators
- [ ] Add sequence diagrams: route creation → scope creation → cleanup
- [ ] Include lifecycle timing examples (Android vs iOS)

### 7.2 Code Comments

- [ ] Add comments to Route Registry explaining ownership
- [ ] Add comments to scope factories explaining they don't own lifetimes
- [ ] Add comments to screens showing Registry usage pattern
- [ ] Add MUST/MUST NOT lists in key functions

### 7.3 ADR (Architecture Decision Record)

**File:** `ADR-002-ROUTE-REGISTRY-OWNERSHIP.md`

```markdown
# ADR-002: Route Registry as Single Lifetime Owner

## Status: Accepted

## Context
Previously, both Koin cache and Route Registry retained scope references, creating potential for orphaned scopes and non-deterministic cleanup.

## Decision
Route Registry is the single, explicit owner of route-scoped state.
Features provide stateless scope factories only.
Koin becomes a tool for construction, not an owner.

## Consequences
- Cleanup is deterministic and route-driven
- Feature modules cannot accidentally leak scopes
- Easier to reason about state lifecycle
- Requires explicit Registry setup in each navigation layer
```

---

## Phase 8: Testing & Verification

### 8.1 Unit Tests

- [ ] RouteRegistry lifetime management
- [ ] RouteRegistry cleanup behavior
- [ ] Factory functions create correct ViewModels
- [ ] Scope closure callbacks fire

### 8.2 Integration Tests

- [ ] Route creation → ViewModel access → cleanup flow
- [ ] Multiple active routes don't interfere
- [ ] Backstack operations close correct scopes
- [ ] No orphaned scopes after full navigation clear

### 8.3 Manual Testing

- [ ] Open restaurant list → open detail → go back → check no scope retained
- [ ] Open detail → filter applied → detail remains open → check state survived
- [ ] Open multiple details rapidly → verify each has own scope
- [ ] Open detail on low memory → verify cleanup happens quickly

### 8.4 Memory Profiling

- [ ] Baseline: open/close restaurant detail 10x
- [ ] Verify scope memory released on back
- [ ] Compare Android vs iOS retention
- [ ] Check for unexpected View/ViewModel refs

---

## Rollout Plan

### Step 1: Foundation (no behavior change)
- Create RouteLifetime
- Refactor RouteRegistry
- Add tests
- Ship without changing screens

### Step 2: Factory Migration (per-feature)
- Create RestaurantDetailScopeFactory
- Update RestaurantDetailScreen
- Verify behavior identical
- Remove old holder
- Repeat for RestaurantListScope, FilterScope

### Step 3: Platform Integration
- Hook Registry into Android NavBackStackEntry
- Hook Registry into iOS NavigationStack
- Verify cleanup fires at correct time
- Monitor for regressions

### Step 4: Cleanup & Documentation
- Remove any remaining Koin cache assumptions
- Update all architecture docs
- Add ADR
- Team review & alignment

---

## Success Criteria

✅ Route Registry is the only long-term scope holder
✅ Features define scopes but don't own them
✅ Cleanup is deterministic and route-driven
✅ No orphaned scopes after any navigation sequence
✅ Memory profiler shows clean scope release
✅ All tests pass (unit + integration + manual)
✅ Team understands ownership model
✅ Architecture docs reflect actual implementation