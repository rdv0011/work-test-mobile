# KMM Type Access & Visibility Analysis

## Executive Summary

The project recently implemented fixes (commit `06c9b15`) to address iOS type export issues. The current approach uses:
1. **ObjCName + ExperimentalObjCName** annotations for runtime naming control
2. **Explicit wrapper functions** that force Kotlin Native to include types in the framework
3. **Platform-specific export files** (`*.ios.kt`) that centralize type declarations
4. **Direct type imports** through iOS aggregator module

This analysis examines these fixes against KMM industry standards and identifies opportunities for alignment.

---

## Current Implementation Analysis

### 1. Export Strategy (Current)

**Location:** `ios-aggregator/src/iosMain/kotlin/io/umain/munchies/aggregator/IosAggregatorExports.kt`

**Pattern:**
```kotlin
@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantListUiStateSuccess")
fun createRestaurantListUiStateSuccess(...): RestaurantListUiState = 
    RestaurantListUiState.Success(...)
```

**What it does:**
- Creates public top-level functions as "export points"
- Uses `@ObjCName` to control Objective-C/Swift naming
- Each type that needs Swift access gets explicit wrapper function
- Aggregates all exports in single file

**Issues with current approach:**
1. ✗ **Scatter-gun export pattern** - Every type needing export gets custom wrapper
2. ✗ **No architectural clarity** - Doesn't distinguish public API from implementation
3. ✗ **Manual burden** - Adding new types requires creating new export functions
4. ✗ **Naming overrides** - Relies on `@ObjCName` to fix naming rather than proper module organization
5. ✗ **Single point of failure** - All exports centralized in one file

---

### 2. Type Organization (Current)

**State Types:** Located in `core/src/iosMain/kotlin/io/umain/munchies/core/state/StateExports.ios.kt`
```kotlin
fun _exportViewStateType(state: ViewState): ViewState = state
fun <S : ViewState> _exportViewModelStateType(vm: ViewModelState<S>): ViewModelState<S> = vm
```

**Navigation Types:** Located in `core/src/iosMain/kotlin/io/umain/munchies/navigation/NavigationExports.ios.kt`
```kotlin
fun _exportDeepLinkProcessorType(processor: DeepLinkProcessor): DeepLinkProcessor = processor
fun _exportRouteConstantsType(): String = RouteConstants.ROUTE_RESTAURANT_LIST
```

**Issues:**
1. ✗ **Inconsistent patterns** - Export files use different patterns (private functions vs public wrappers)
2. ✗ **Unclear intent** - Naming with `_export` prefix doesn't signal API boundary
3. ✗ **Mixed responsibilities** - State exports and navigation exports scattered across modules

---

### 3. Visibility Modifiers (Current)

**Observed:** No explicit visibility modifiers used in export files
- Everything defaults to `public` in Kotlin
- No distinction between internal implementation and public API
- No use of `internal` keyword to hide implementation details

**Problem:**
```kotlin
// Currently ALL of these are public:
fun _exportViewStateType(state: ViewState): ViewState = state  // Should be implementation detail?
fun getStringResourcesObject(): Any = StringResources          // Definitely public
fun createRestaurantListScope(): Scope = createRestaurantListScopeIos()  // DI factory - public
```

---

## KMM Industry Standards

### 1. Access Control Best Practices

**Standard Pattern:**
```kotlin
// ✓ GOOD: Clear public API
@HiddenFromObjC  // Hide implementation details
internal fun _createRestaurantListScopeInternal(): Scope = ...

public fun createRestaurantListScope(): Scope = _createRestaurantListScopeInternal()
```

**Rationale:**
- Use `internal` modifier to hide implementation from all platforms
- Use `@HiddenFromObjC` to explicitly hide from Objective-C/Swift
- Public functions form the official API contract

### 2. Framework Module Organization

**Standard (Jetbrains/Kotlin ecosystem):**
```
core/
  src/
    commonMain/
      - Core interfaces, sealed classes
      - Platform-independent logic
    iosMain/
      - Explicit "public" adapter layer
      - Wraps and exposes types
      - Single responsibility: iOS interop
```

**Key principle:** iOS main should ONLY contain exports, adapters, and platform-specific code. Implementation stays in commonMain.

### 3. Type Aliasing Patterns

**Standard approach for naming conflicts:**
```kotlin
// In TypeAliases.swift or similar
typealias RestaurantListUiStateSuccess = Feature_restaurantRestaurantListUiStateSuccessState
typealias ViewModelHolder = Feature_restaurantRestaurantDetailViewModelHolder
```

**Why:** Kotlin Native auto-generates prefixed names to avoid conflicts. Instead of fighting it with `@ObjCName`, embrace it with typealias.

### 4. Sealed Classes & Expect/Actual

**Standard pattern for platform variants:**
```kotlin
// commonMain
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<String>()
}

// iosMain - no override needed for sealed classes
// Kotlin Native automatically handles
```

**Key:** Don't use `expect/actual` for sealed classes. Use inheritance instead.

### 5. Scope & DI Export Pattern

**Industry standard (Koin multiplatform):**
```kotlin
// commonMain
object KoinModule {
    val modules = listOf(coreModule, featureModule)
}

// iosMain - single entry point
public object IosKoinSetup {
    public fun initializeKoin() {
        startKoin {
            modules(KoinModule.modules)
        }
    }
}
```

**Current project issue:** Scope operations scattered across multiple files instead of centralized.

---

## Gap Analysis: Current vs. Standard

| Aspect | Current | Standard | Gap |
|--------|---------|----------|-----|
| **Visibility Control** | No `internal` modifier | Explicit `internal` + `@HiddenFromObjC` | ✗ Missing explicit boundaries |
| **Export Location** | Scattered across 3 files | Single coherent API surface | ✗ Not clearly defined |
| **Type Aliasing** | Using `@ObjCName` override | Using Swift typealias | ✗ Fighting name mangling instead of embracing it |
| **Scope Management** | Functions across 4+ locations | Centralized factory | ✗ Decentralized |
| **Access Modifiers** | All public (implicit) | Mix of internal/public (explicit) | ✗ No encapsulation |
| **Documentation** | Comment-based exports | Clear API contracts | ✗ Implicit rather than explicit |
| **Naming Convention** | `_export` + `@ObjCName` | No prefix + `typealias` | ✗ Inconsistent |

---

## Recommendations for Industry Alignment

### ✓ KEEP (Working Well)

1. **Platform-specific export files** (`*.ios.kt`)
   - Pattern is sound, just needs organization
   
2. **Wrapper function approach**
   - Gives fine-grained control over what's exposed
   
3. **ObjCName for factory methods**
   - Good for creating constructors: `@ObjCName("createSettingsScope")`

### 🔄 REFACTOR (Needs Alignment)

#### 1. **Consolidate Access Control**

**Current:**
```kotlin
// core/src/iosMain/kotlin/io/umain/munchies/core/state/StateExports.ios.kt
fun _exportViewStateType(state: ViewState): ViewState = state

// ios-aggregator/src/iosMain/kotlin/io/umain/munchies/aggregator/IosAggregatorExports.kt
@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantListUiStateSuccess")
fun createRestaurantListUiStateSuccess(...): RestaurantListUiState = ...
```

**Standard:**
```kotlin
// ios-aggregator/src/iosMain/kotlin/io/umain/munchies/aggregator/IosPublicApi.kt
/**
 * Public API for iOS/Swift interop.
 * Only types in this file are exported to the generated framework.
 */

// ============================================================================
// State Types (force Kotlin Native to include in framework)
// ============================================================================
@OptIn(ExperimentalObjCName::class)
@HiddenFromObjC
internal fun _forceExportViewState(state: ViewState): ViewState = state

// ============================================================================
// UI State Constructors (public factory pattern)
// ============================================================================
@OptIn(ExperimentalObjCName::class)
@ObjCName("RestaurantListUiStateSuccess")
public fun createRestaurantListUiStateSuccess(...): RestaurantListUiState = 
    RestaurantListUiState.Success(...)
```

**Why:** 
- Clear separation: internal force-exports vs. public factories
- `@HiddenFromObjC` explicitly marks implementation details
- Single source of truth for iOS API surface

#### 2. **Use Explicit Access Modifiers**

**Current:**
```kotlin
// Implicitly public
fun getRestaurantListViewModelFromFramework(): RestaurantListViewModel = 
    getRestaurantListViewModelIos()
```

**Standard:**
```kotlin
/**
 * Public factory for restaurant list ViewModel.
 * Called from iOS NavigationCoordinator.restaurantListHolder().
 */
public fun getRestaurantListViewModelFromFramework(): RestaurantListViewModel = 
    getRestaurantListViewModelIos()

/**
 * Internal implementation - not part of public API.
 * Hides platform-specific scope creation logic.
 */
@HiddenFromObjC
internal fun getRestaurantListViewModelIos(): RestaurantListViewModel = 
    scope.get()
```

**Why:**
- Explicit intent reduces confusion
- `@HiddenFromObjC` prevents accidental Swift access
- Kdoc clarifies purpose of each export

#### 3. **Adopt Type Alias Pattern in Swift** (Minimal SwiftUI Changes)

**Current pattern (in Swift):**
```swift
// Multiple type references with full prefixes
let navigationViewModel: Feature_restaurantRestaurantNavigationViewModel
let viewModel: Feature_restaurantFeature_restaurantRestaurantDetailViewModel
```

**Create once in TypeAliases.swift:**
```swift
// iosApp/iosApp/Core/Bridges/TypeAliases.swift
import shared

// Navigation ViewModels
typealias RestaurantNavigationViewModel = Feature_restaurantRestaurantNavigationViewModel
typealias RestaurantListViewModel = Feature_restaurantRestaurantListViewModel
typealias RestaurantDetailViewModel = Feature_restaurantFeature_restaurantRestaurantDetailViewModel

// ViewModelHolders
typealias RestaurantListHolder = RestaurantListViewModelHolder
typealias RestaurantDetailHolder = Feature_restaurantRestaurantDetailViewModelHolder

// State Types
typealias RestaurantListUiStateSuccess = Feature_restaurantRestaurantListUiStateSuccess
typealias RestaurantListUiState = Feature_restaurantRestaurantListUiState
```

**Then use in Views (NO CHANGES needed to current usage):**
```swift
struct RestaurantListView: View {
    let navigationViewModel: RestaurantNavigationViewModel  // ✓ Already works with typealias
    let viewModel: RestaurantListViewModel                   // ✓ Already works
}
```

**Why:**
- ✓ Zero changes to view code
- ✓ Cleaner Swift files
- ✓ Easier maintenance
- ✓ Follows Swift naming conventions

#### 4. **Module Organization for Exports**

**Recommended structure:**

```
ios-aggregator/src/iosMain/kotlin/io/umain/munchies/aggregator/
├── IosPublicApi.kt           # ← Single public API surface
├── IosStateExports.kt        # ← Internal state type forcing
├── IosNavigationExports.kt   # ← Internal nav type forcing
└── IosConstantsExports.kt    # ← Public constant helpers
```

**Why:**
- Clear separation of concerns
- Each file has single responsibility
- Easier to review what's public vs. internal
- Scales better as project grows

#### 5. **Scope Management Consolidation**

**Current:**
```kotlin
// Scattered across multiple files
fun createRestaurantListScopeIos(): Scope = ...  // in feature-restaurant
fun createSettingsScopeIos(): Scope = ...        // in feature-settings
fun createRestaurantListScope(): Scope = createRestaurantListScopeIos()  // in aggregator
```

**Standard - single entry point:**
```kotlin
// ios-aggregator/src/iosMain/kotlin/IosPublicApi.kt
public object IosFrameworkScope {
    public fun createRestaurantListScope(): Scope = createRestaurantListScopeIos()
    public fun createRestaurantDetailScope(id: String): Scope = createRestaurantDetailScopeIos(id)
    public fun createSettingsScope(): Scope = createSettingsScopeIos()
}
```

**Swift usage:**
```swift
let scope = IosFrameworkScopeKt.createRestaurantListScope()
```

**Why:**
- Single namespace for all scope creation
- Clear contract for scope lifecycle
- Easier to audit what's exported
- Better mirrors Android pattern (if any)

---

## Implementation Priority

### 🔴 HIGH (Do Now)
1. Add `@HiddenFromObjC` to internal force-export functions
2. Add explicit `public` to actual public API functions
3. Create centralized `TypeAliases.swift` in Swift (minimal changes)

### 🟡 MEDIUM (Do Next)
1. Reorganize export files by type (state, navigation, constants)
2. Add comprehensive Kdoc to all public exports
3. Create `IosPublicApi.kt` as single source of truth

### 🟢 LOW (Nice to Have)
1. Create feature-specific export modules (less centralization)
2. Implement strict linting for `@HiddenFromObjC` usage
3. Document the iOS export pattern in BUILD.md

---

## SwiftUI View Impact Analysis

### Current Changes Required (Pre-Analysis)
The commit `06c9b15` already includes SwiftUI changes:
- Type names updated with full prefixes (e.g., `Feature_restaurantRestaurantListViewModel`)
- ViewModelHolder signatures modified
- Navigation coordinator simplified

### Impact of Proposed Changes
**Minimal additional changes required:**

1. **TypeAliases.swift creation** → ONE file to add
   - No changes to existing view files
   - Optional cleanup of long type names

2. **Access modifier additions** → ZERO view changes
   - Kotlin-only changes
   - No Swift visible impact

3. **Module reorganization** → ZERO view changes
   - Internal Kotlin refactoring
   - No Swift API changes

**Conclusion:** Aligning with standards requires **no additional SwiftUI modifications** beyond what's already done.

---

## Code Examples

### Example 1: Proper Export Pattern

```kotlin
// ✓ ALIGNED: ios-aggregator/src/iosMain/kotlin/IosPublicApi.kt

package io.umain.munchies.aggregator

import kotlin.native.ObjCName
import kotlin.experimental.ExperimentalObjCName
import org.koin.core.scope.Scope

// ============================================================================
// TYPE FORCING (Internal - for framework generation only)
// ============================================================================

/**
 * Internal force-export functions.
 * Not called by iOS app, purely for Kotlin Native compilation.
 * Ensures sealed classes and complex types are included in the framework.
 */
@OptIn(ExperimentalObjCName::class)
@HiddenFromObjC
internal fun _forceExportStateTypes(
    state: ViewState,
    modelState: ViewModelState<ViewState>
): Pair<ViewState, ViewModelState<ViewState>> = state to modelState

// ============================================================================
// PUBLIC API (called from iOS)
// ============================================================================

/**
 * Creates a scope for the restaurant list route.
 * The scope is owned by the RouteRegistry until cleanup() is called.
 * 
 * @return A Koin scope configured for restaurant list operations
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("createRestaurantListScope")
public fun createRestaurantListScope(): Scope = 
    createRestaurantListScopeIos()

/**
 * Creates a ViewModel for the restaurant list.
 * Must be called after createRestaurantListScope().
 * 
 * @return The configured ViewModel
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("getRestaurantListViewModelFromFramework")
public fun getRestaurantListViewModelFromFramework(): RestaurantListViewModel = 
    getRestaurantListViewModelIos()
```

**Why this works:**
- ✓ `@HiddenFromObjC` + `internal` hides implementation
- ✓ `@ObjCName` controls exact Swift naming
- ✓ Kdoc clarifies contract
- ✓ Clear public vs. internal separation

### Example 2: Type Aliasing (Swift)

**Add once in** `iosApp/iosApp/Core/Bridges/TypeAliases.swift`:
```swift
import shared

// Restaurant Feature
typealias RestaurantNavigationViewModel = Feature_restaurantRestaurantNavigationViewModel
typealias RestaurantListViewModel = Feature_restaurantRestaurantListViewModel
typealias RestaurantDetailViewModel = Feature_restaurantFeature_restaurantRestaurantDetailViewModel
typealias RestaurantListUiState = Feature_restaurantRestaurantListUiState
typealias RestaurantListUiStateSuccess = Feature_restaurantRestaurantListUiStateSuccess
typealias RestaurantDetailUiState = Feature_restaurantRestaurantDetailUiState
typealias RestaurantListViewModelHolder = RestaurantListViewModelHolder

// Settings Feature  
typealias SettingsViewModel = Feature_settingsSettingsViewModel
typealias SettingsNavigationViewModel = Feature_settingsSettingsNavigationViewModel
typealias SettingsViewModelHolder = Feature_settingsSettingsViewModelHolder
```

**Then existing views work unchanged:**
```swift
// This already works and looks clean
struct RestaurantListView: View {
    let navigationViewModel: RestaurantNavigationViewModel
    let viewModel: RestaurantListViewModel
    
    // No changes needed - typealias handles the long names
}
```

---

## Summary & Recommendations

### Current State
✓ **Working:** The recent fix (06c9b15) successfully compiles and functions  
✗ **Not aligned:** Lacks explicit access control and industry standard patterns

### To Achieve Alignment (Minimal Effort)

| Change | Effort | SwiftUI Impact | Timeline |
|--------|--------|----------------|----------|
| Add `@HiddenFromObjC` to internal exports | 5 min | None | Now |
| Add explicit `public` modifier | 5 min | None | Now |
| Create TypeAliases.swift | 10 min | Optional cleanup | Next |
| Reorganize export files | 15 min | None | Next |
| Add Kdoc to exports | 20 min | None | Later |

### Minimal SwiftUI Changes Needed
- TypeAliases.swift is optional (just cleanliness)
- All view code currently compiles and works
- No breaking changes to adopt standards

### Key Takeaway
The project has **good foundations** but needs:
1. Explicit access control (keywords)
2. Explicit visibility marking (annotations)
3. Clear API boundaries (documentation)

These are low-cost, high-clarity improvements that follow Kotlin/KMM best practices.

---

## Appendix: Event Propagation Bridge (StateFlow → AsyncStream)

### Issue: Extension Functions with Closures Don't Bridge to Swift

**Problem identified:** RestaurantListView and SettingsView were stuck in Loading state because StateFlow emissions weren't being received by the Swift UI layer.

**Root cause:** Kotlin extension functions with closure parameters don't translate properly to Objective-C/Swift runtime:

```kotlin
// ❌ BROKEN: Extension function with closure parameter
fun <T> StateFlow<T>.subscribeState(onEach: (T) -> Unit): Job =
    scope.launch { collect { onEach(it) } }

// Swift can't properly call this:
let job = (viewModel as? LifecycleOwner)?.subscribeState(viewModel.stateFlow) { value in
    continuation.yield(value)  // ← Closure capture fails
}
```

**Why it fails:**
1. Extension functions don't export cleanly to Objective-C
2. Closure parameters require specific capture semantics that the Objective-C bridge doesn't support
3. StateFlow is a Kotlin interface, making the extension function binding ambiguous to the runtime

**Impact:** 
- ✗ Initial state (Loading) might emit synchronously
- ✗ Subsequent state transitions NEVER trigger the closure
- ✗ Views stuck on Loading indefinitely

### Solution: Explicit Regular Function with Proper Bridging

**Fixed approach (LifecycleExports.ios.kt):**

```kotlin
// ✓ WORKING: Explicit regular function (not extension) with clear parameter passing
@OptIn(ExperimentalObjCName::class)
@ObjCName("subscribeToStateFlow")
fun <T> subscribeToStateFlow(
    lifecycle: LifecycleOwner,
    stateFlow: StateFlow<T>,
    onStateChanged: (T) -> Unit
): Job {
    return lifecycle.scope.launch {
        stateFlow.collect { onStateChanged(it) }
    }
}
```

**Swift integration (AsyncStream+StateFlow.swift):**

```swift
// ✓ WORKING: Direct function call with explicit parameters
let job = IosAggregatorExportsKt.subscribeToStateFlow(
    lifecycle: viewModel as! LifecycleOwner,
    stateFlow: viewModel.stateFlow,
    onStateChanged: { value in
        if let typed = value as? S {
            continuation.yield(typed)
        }
    }
)
```

### Why This Works

1. **Regular function** - Not an extension, so Objective-C runtime can properly bind it
2. **Explicit parameters** - Swift knows exactly what to pass
3. **Closure as parameter** - Direct parameter passing works, unlike extension function closures
4. **Proper scope access** - `lifecycle.scope` accessed before launch, not through extension

### Affected Views

- ✓ RestaurantListView - now receives all state transitions
- ✓ SettingsView - now receives all state transitions
- ✓ Any future views using `asyncStateStream<S, VM>()` pattern

### Future Improvement: Swift Concurrency Native AsyncStream

**Note:** This manual workaround will become unnecessary once Swift Export reaches stable release in Kotlin Multiplatform. When available, Swift can natively consume Kotlin Flow types without this bridge:

```kotlin
// Future: Native Swift Concurrency integration (not yet available)
@OptIn(ExperimentalObjCName::class)
fun <T> subscribeToStateFlow(stateFlow: StateFlow<T>): AsyncSequence<T> {
    // Direct AsyncSequence support - no manual bridge needed
}
```

**Timeline:** Monitor Kotlin Multiplatform roadmap for Swift Export release. Once stable, replace manual `asyncStateStream()` pattern with native interop.

**Why wait:** Swift Export is still experimental (as of KMM 1.x). Production adoption requires stability guarantees. Current bridge is solid and will serve as foundation for migration.

---

## Appendix A: Unused Constants Re-Integration Strategy

### Problem Statement

Three constant objects are currently defined but not actively used in iOS:
1. **StringResources** - Centralized localization keys (44 constants)
2. **DeepLinkConstants** - Deep link handling constants (50 constants)
3. **DesignTokens** - Design system tokens (PARTIALLY USED - via extensions only)

**Current situation:**
- String keys are hardcoded directly in SwiftUI files (e.g., `stringResource(key: "tab_restaurants")`)
- DeepLink constants are never referenced in iOS code
- DesignTokens ARE properly integrated via Swift extensions, BUT the Kotlin object isn't directly accessible

**Why this matters:**
- ✗ Type safety lost - hardcoded strings can have typos
- ✗ No single source of truth - inconsistent string usage across files
- ✗ No compile-time verification - runtime errors if key doesn't exist
- ✗ Maintenance burden - changing a key requires searching files
- ✗ Incomplete KMM pattern - breaking code sharing principle

### Root Cause Analysis

**Why constants aren't used:**
1. **iOS strings use runtime lookup** (`stringResource(key: "...")` pattern)
   - Swift can't directly use Kotlin const values at compile time
   - Would require Kotlin to Swift code generation (not currently done)
   
2. **DeepLinkConstants** only needed for deep link parsing
   - iOS uses SwiftUI routing, not URL scheme handling
   - Android handles deep links, iOS handles navigation stack directly
   
3. **DesignTokens** partially exported
   - Kotlin DesignTokens object exists but iOS uses Swift extensions instead
   - Extensions work but create parallel type definitions

### Re-Integration Strategy

#### ✓ HIGH PRIORITY: StringResources via Constants Export

**Current:**
```swift
// iosApp/iosApp/Navigation/TabNavigationView.swift
Label(stringResource(key: "tab_restaurants"), systemImage: "house.fill")
Label(stringResource(key: "tab_settings"), systemImage: "gear")
```

**Recommended:**
```swift
// iosApp/iosApp/Core/Localization/StringConstants.swift
struct StringConstants {
    static let appTitle = IosAggregatorExportsKt.getStringKey_appTitle()
    static let tabRestaurants = IosAggregatorExportsKt.getStringKey_tabRestaurants()
    static let tabSettings = IosAggregatorExportsKt.getStringKey_tabSettings()
    static let restaurantListTitle = IosAggregatorExportsKt.getStringKey_restaurantListTitle()
    // ... etc
}

// Usage:
Label(stringResource(key: StringConstants.tabRestaurants), systemImage: "house.fill")
```

**Kotlin exports needed:**
```kotlin
// ios-aggregator/src/iosMain/kotlin/io/umain/munchies/aggregator/IosAggregatorExports.kt
@OptIn(ExperimentalObjCName::class)
@ObjCName("getStringKey_appTitle")
public fun getStringKeyAppTitle(): String = StringResources.app_title

@OptIn(ExperimentalObjCName::class)
@ObjCName("getStringKey_tabRestaurants")
public fun getStringKeyTabRestaurants(): String = StringResources.tab_restaurants

@OptIn(ExperimentalObjCName::class)
@ObjCName("getStringKey_tabSettings")
public fun getStringKeyTabSettings(): String = StringResources.tab_settings

// ... one for each constant
```

**Benefits:**
- ✓ Compile-time verification of key existence
- ✓ Single source of truth
- ✓ Type-safe string references
- ✓ Easy refactoring (find usages)
- ✓ Reduces hardcoded string duplication

**Implementation effort:** ~30 minutes

---

#### 🟡 MEDIUM PRIORITY: DeepLinkConstants for Deep Link Processing

**Current state:**
- iOS doesn't handle URL schemes (SwiftUI navigation does)
- DeepLink parsing only on Android
- Constants unused in iOS

**Re-integration rationale:**
- Potential future iOS support for universal links
- Ensures consistency with Android implementation
- Documents deep link contract for iOS

**Recommended approach:**

```kotlin
// ios-aggregator/src/iosMain/kotlin/io/umain/munchies/aggregator/IosAggregatorExports.kt
public object IosDeepLinkConfig {
    @ObjCName("deepLinkScheme")
    public fun scheme(): String = DeepLinkConstants.SCHEME
    
    @ObjCName("deepLinkHostRestaurants")
    public fun hostRestaurants(): String = DeepLinkConstants.HOST_RESTAURANTS
    
    @ObjCName("deepLinkHostSettings")
    public fun hostSettings(): String = DeepLinkConstants.HOST_SETTINGS
    
    @ObjCName("deepLinkPathFilter")
    public fun pathFilter(): String = DeepLinkConstants.PATH_FILTER
    
    @ObjCName("deepLinkTabIdRestaurants")
    public fun tabIdRestaurants(): String = DeepLinkConstants.TAB_ID_RESTAURANTS
    
    @ObjCName("deepLinkTabIdSettings")
    public fun tabIdSettings(): String = DeepLinkConstants.TAB_ID_SETTINGS
}
```

**Swift integration (optional, for future use):**
```swift
// iosApp/iosApp/Core/DeepLinking/DeepLinkConfig.swift
import shared

struct DeepLinkConfig {
    static let scheme = IosAggregatorExportsKt.scheme()
    static let hostRestaurants = IosAggregatorExportsKt.hostRestaurants()
    static let hostSettings = IosAggregatorExportsKt.hostSettings()
    static let tabIdRestaurants = IosAggregatorExportsKt.tabIdRestaurants()
    static let tabIdSettings = IosAggregatorExportsKt.tabIdSettings()
}
```

**Benefits:**
- ✓ Documents deep link architecture
- ✓ Ensures consistency with Android
- ✓ Enables future universal links support
- ✓ Follows KMM code-sharing principle

**Implementation effort:** ~15 minutes

**Timeline:** After basic app functionality verified

---

#### 🟢 LOW PRIORITY: DesignTokens Full Integration

**Current state:**
- DesignTokens Kotlin object exists but NOT directly accessible in Swift
- iOS uses parallel extensions instead

**Current pattern (extensions approach):**
```swift
// iosApp/iosApp/Core/DesignTokens/Extensions/CGFloat+DesignTokens.swift
extension CGFloat {
    static var iconSmall: CGFloat { DesignTokens.iOS.size.iconUI.small }
    static var iconMedium: CGFloat { DesignTokens.iOS.size.iconUI.medium }
    // Parallel definitions!
}
```

**Assessment:**
- ✗ Less clean than current extensions approach
- ✗ Doesn't improve type safety
- ✗ Current extensions are idiomatic Swift
- ✗ Would require reimporting all extension code

**Recommendation:** **KEEP CURRENT EXTENSIONS APPROACH**
- Extensions are more idiomatic for Swift
- Provides clean API surface
- No benefit from changing

---

### Implementation Roadmap

#### Phase 1: StringResources (IMMEDIATE - AFTER BASIC FUNCTIONALITY VERIFIED)
```
Week 1:
1. Create StringConstants export functions in Kotlin
2. Create StringConstants.swift wrapper
3. Replace hardcoded keys in views (5-10 files)
4. Test string localization works
5. Commit: "feat: export StringResources constants for type-safe key access"
```

#### Phase 2: DeepLinkConstants (OPTIONAL - when needed)
```
Week 2+:
1. Create IosDeepLinkConfig export
2. Document deep link architecture for iOS
3. Create DeepLinkConfig.swift wrapper
4. Prepare for future universal links support
5. Commit: "feat: export DeepLinkConstants for iOS deep link handling"
```

#### Phase 3: DesignTokens (NOT RECOMMENDED - keep extensions)
```
Decision: Maintain current extension-based approach
- It's idiomatic Swift
- Works well for design system patterns
- Extensions provide better SwiftUI integration
```

---

### Summary: Unused Constants Re-Integration

| Constant | Current State | Recommendation | Effort | Priority |
|----------|---------------|-----------------|--------|----------|
| **StringResources** | Hardcoded in views | Export all keys via Kotlin → Swift wrapper | 30 min | 🔴 HIGH |
| **DeepLinkConstants** | Unused in iOS | Export for documentation + future support | 15 min | 🟡 MEDIUM |
| **DesignTokens** | Used via extensions | Keep extensions (idiomatic) | 0 | 🟢 DONE |

**Next steps:** Implement Phase 1 (StringResources) after confirming basic app functionality works on simulator.
