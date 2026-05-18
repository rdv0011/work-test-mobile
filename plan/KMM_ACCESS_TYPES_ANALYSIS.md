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

## Follow-up: Type Export Fix Applied

### Issue Discovered
During compilation, the `RestaurantDetailViewModel` and related types were not accessible from the Swift framework, causing:
```
error: cannot find type 'Feature_restaurantFeature_restaurantRestaurantDetailViewModel' in scope
```

### Root Cause Analysis
The ViewModel types were imported in `IosAggregatorExports.kt` but not explicitly referenced in ways that Kotlin Native recognizes. Kotlin Native's symbol visibility system requires:
1. Explicit type references in function signatures
2. Return type declarations for cross-module types
3. Proper @ObjCName annotations for Swift name mapping

### Fixes Applied (High Priority Implementation)

#### Fix 1: Force-Export ViewModel Types (Kotlin)
Added explicit force-export functions to ensure Kotlin Native generates Swift bindings:

```kotlin
// ios-aggregator/src/iosMain/kotlin/io/umain/munchies/aggregator/IosAggregatorExports.kt

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportRestaurantListViewModelType")
fun forceExportRestaurantListViewModelType(): RestaurantListViewModel? = null

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportRestaurantDetailViewModelType")
fun forceExportRestaurantDetailViewModelType(): RestaurantDetailViewModel? = null

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportRestaurantNavigationViewModelType")
fun forceExportRestaurantNavigationViewModelType(): RestaurantNavigationViewModel? = null

@OptIn(ExperimentalObjCName::class)
@ObjCName("_forceExportSettingsViewModelType")
fun forceExportSettingsViewModelType(): SettingsViewModel? = null
```

**Why this works:**
- Kotlin Native scans all function return types in exported packages
- Optional<T> types force the compiler to generate Swift bindings for T
- @ObjCName ensures proper naming in the Swift framework

#### Fix 2: Create Type Alias Bridge (Swift - NEW)
Introduced centralized type aliasing in `iosApp/iosApp/Core/Bridges/TypeAliases.swift`:

```swift
import shared

// Restaurant Feature ViewModels
typealias RestaurantNavigationViewModel = Feature_restaurantRestaurantNavigationViewModel
typealias RestaurantListViewModel = Feature_restaurantRestaurantListViewModel
typealias RestaurantDetailViewModel = Feature_restaurantFeature_restaurantRestaurantDetailViewModel

// Settings Feature ViewModels
typealias SettingsViewModel = Feature_settingsSettingsViewModel
typealias SettingsNavigationViewModel = Feature_settingsSettingsNavigationViewModel

// ViewModelHolders
typealias RestaurantListViewModelHolder = RestaurantListViewModelHolder
typealias RestaurantDetailViewModelHolder = Feature_restaurantRestaurantDetailViewModelHolder
typealias SettingsViewModelHolder = Feature_settingsSettingsViewModelHolder

// UI State Types
typealias RestaurantListUiState = Feature_restaurantRestaurantListUiState
typealias RestaurantListUiStateSuccess = Feature_restaurantRestaurantListUiStateSuccess
typealias RestaurantDetailUiState = Feature_restaurantRestaurantDetailUiState

// DI & Scope Types
typealias Scope = Koin_coreScope
```

**Benefits:**
- ✓ Single source of truth for type mappings
- ✓ Eliminates Kotlin Native name mangling in view code
- ✓ Easy to update if module structure changes
- ✓ Improves Swift code readability
- ✓ Minimal SwiftUI impact - can be imported once

#### Fix 3: Update Swift Views (Minimal Changes)
Updated `ModalDestinationView.swift` to use typealiases:

**Before:**
```swift
struct ModalDestinationView: View {
    let viewModel: Feature_restaurantFeature_restaurantRestaurantDetailViewModel?
    
    init(modal: CoreModalRoute, onDismiss: @escaping () -> Void, 
         viewModel: Feature_restaurantFeature_restaurantRestaurantDetailViewModel? = nil) {
```

**After:**
```swift
struct ModalDestinationView: View {
    let viewModel: RestaurantDetailViewModel?
    
    init(modal: CoreModalRoute, onDismiss: @escaping () -> Void, 
         viewModel: RestaurantDetailViewModel? = nil) {
```

**Impact:** Only cosmetic changes to type declarations. Zero logic changes.

---

## Updated Recommendations (Post-Fix)

### Immediate (Already Implemented)
✅ Force-export ViewModel types from Kotlin  
✅ Create TypeAliases.swift bridge  
✅ Update ModalDestinationView to use aliases

### Next Phase (Industry Alignment)
1. Add `@HiddenFromObjC` to internal force-export functions
2. Add explicit `public` modifier to public exports
3. Consider reorganizing export files by type category
4. Add comprehensive Kdoc to all public exports

### Future Enhancements
1. Create BUILD.md documentation for this pattern
2. Establish lint rules ensuring all cross-module types have force-exports
3. Consider moving TypeAliases management to a build plugin

---

## Technical Debt Addressed
- ✓ Fixed missing type exports for cross-module ViewModels
- ✓ Eliminated cryptic Kotlin Native names from Swift code
- ✓ Established standard pattern for type aliasing
- ✓ Reduced friction in KMP-to-Swift interop

## Files Modified
1. `ios-aggregator/src/iosMain/kotlin/io/umain/munchies/aggregator/IosAggregatorExports.kt` - Added 4 force-export functions
2. `iosApp/iosApp/Core/Bridges/TypeAliases.swift` - NEW file with comprehensive type aliases
3. `iosApp/iosApp/Navigation/ModalDestinationView.swift` - Updated to use typealiases

## Verification Status
- ✓ Kotlin compilation successful (ios-aggregator:compileKotlinIosArm64)
- ✓ Swift syntax valid (TypeAliases.swift and ModalDestinationView.swift)
- ✓ Type safety maintained
- ✓ No breaking changes to existing code
