# NEXT STEPS - Phase 0 Implementation Guide

**Status**: Ready to Begin Implementation  
**Current Branch**: `feature/scaleable-navigation`  
**Date**: February 17, 2026

---

## 🎯 Your Current Situation

You're on the `feature/scaleable-navigation` branch with:

- **✅ COMPLETED**: Comprehensive architecture documentation (6 documents, 160+ pages)
- **✅ IN PROGRESS**: Refactoring route providers for cleaner architecture
- **⚠️ PENDING**: Phase 0 implementation (navigation state management)
- **⚠️ PENDING**: Platform-specific integration

### Git Status
```
Modified: 2 files (PlatformAppRouteProviders.kt, Route.swift)
Deleted: 4 files (old route providers and handlers - replaced by new structure)
Untracked: 8 files (all the architecture documentation)
```

### Last 5 Commits
1. `8d73e15` - refactor: align route names and parameters across platforms using RouteConstants
2. `7bb76a0` - fix: implement handler-based scope creation to eliminate recursion in iOS navigation
3. `6c3d4c6` - fix: prevent duplicate scope creation in route lifecycle management
4. `c94d192` - refactor: move route providers to feature layer with multi-platform factory pattern
5. `8ab6508` - feat: eliminate hardcoded route strings with data-driven navigation

---

## ⚡ Immediate Next Steps (This Session)

### Option A: Complete Current Work + Start Phase 0 (Recommended)

**Step 1: Commit Current Progress** (10 minutes)
```bash
git add -A
git commit -m "refactor: reorganize restaurant route providers and simplify handler registration"
```

**Why?** Preserves the refactoring work, keeps git history clean, creates a checkpoint before Phase 0

**Step 2: Verify Build** (5 minutes)
```bash
./gradlew build -x iosApp
# On macOS: xcodebuild build -scheme iosApp
```

**Why?** Ensures no compilation errors after refactoring

**Step 3: Start Phase 0** (See Phase 0 checklist below)

---

### Option B: Start Fresh (If Current Changes Don't Compile)

**If compilation fails**, reset everything:
```bash
git reset --hard HEAD
git clean -fd
```

Then proceed with Phase 0 below.

---

## 📋 Phase 0: Foundation Implementation Checklist

### Phase 0 Goal
Create the core navigation state management system that all other phases depend on.

**Estimated Time**: 15 hours over 3-5 days

---

### Task 1: Create NavigationState.kt (1.5 hours)

**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationState.kt`

**What to do**:
1. Copy the complete `NavigationState` and related data classes from **NAVIGATION_IMPLEMENTATION_EXAMPLES.md** (Section 2.1)
2. Create the file with these classes:
   - `NavigationState` - Main state container
   - `StackRoute` sealed class - Route types on main stack
   - `ModalRoute` sealed class - Route types for modals (currently empty, will be added in Phase 1)

**Expected Code**:
```kotlin
data class NavigationState(
    val primaryStack: List<StackRoute> = listOf(StackRoute.RestaurantList),
    val modalStack: List<ModalRoute> = emptyList()
)

sealed class StackRoute {
    data object RestaurantList : StackRoute()
    data class RestaurantDetail(val restaurantId: String) : StackRoute()
}

sealed class ModalRoute {
    // Will be populated in Phase 1
}
```

**Verification**:
```bash
./gradlew :core:compileKotlin
# Should have no errors
```

---

### Task 2: Create TabNavigationState.kt (1 hour)

**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/TabNavigationState.kt`

**What to do**:
1. Copy the complete `TabNavigationState` and related classes from **NAVIGATION_IMPLEMENTATION_EXAMPLES.md** (Section 2.2)
2. This can remain mostly empty for Phase 0 - it's prepared for Phase 2

**Expected Code**:
```kotlin
data class TabNavigationState(
    val tabs: Map<String, List<StackRoute>> = mapOf("home" to listOf(StackRoute.RestaurantList)),
    val activeTab: String = "home"
)
```

**Verification**:
```bash
./gradlew :core:compileKotlin
```

---

### Task 3: Create NavigationReducer.kt (3 hours)

**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationReducer.kt`

**What to do**:
1. Copy the complete `NavigationReducer` from **NAVIGATION_IMPLEMENTATION_EXAMPLES.md** (Section 2.5)
2. This is the MOST IMPORTANT file - implement all state mutations here
3. Verify each function handles state immutably

**Key Functions to Implement**:
- `reduce()` - Main entry point, handles all event types
- `handlePush()` - Add route to stack
- `handlePop()` - Remove route from stack
- `handlePopToRoot()` - Clear stack

**Critical**: Every function must be PURE (no side effects, no external calls)

**Verification**:
```bash
./gradlew :core:compileKotlin
# Then write tests (Task 5)
```

---

### Task 4: Update AppCoordinator.kt (2 hours)

**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/AppCoordinator.kt`

**What to do**:
1. Replace the current `AppCoordinator` with the extended version from **NAVIGATION_IMPLEMENTATION_EXAMPLES.md** (Section 2.6)
2. Key additions:
   - `navigationState: StateFlow<NavigationState>` - Current navigation state
   - `reducer: NavigationReducer` - Instance of the reducer
   - `dispatch(event: NavigationEvent)` - Apply events to reducer
   - Convenience methods: `push()`, `pop()`, `popToRoot()`

**Expected Structure**:
```kotlin
class AppCoordinator {
    // State flow
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    // Reducer
    private val reducer = NavigationReducer()
    
    // Events (keep existing)
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 10)
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    // Dispatch method
    fun dispatch(event: NavigationEvent) {
        val newState = reducer.reduce(_navigationState.value, event)
        _navigationState.value = newState
        _navigationEvents.tryEmit(event)
    }
    
    // Keep existing methods for backward compatibility
    fun navigateTo(destination: Destination) { ... }
    fun navigateToRestaurantDetail(restaurantId: String) { ... }
    fun navigateBack() { ... }
    fun navigateToRoot() { ... }
}
```

**Verification**:
```bash
./gradlew :core:compileKotlin
```

---

### Task 5: Write Unit Tests (5 hours)

**Files to Create**:

#### 5A: NavigationReducerTest.kt
**File**: `core/src/commonTest/kotlin/io/umain/munchies/navigation/NavigationReducerTest.kt`

**What to do**:
1. Copy test template from **NAVIGATION_IMPLEMENTATION_EXAMPLES.md** (Section 3.1)
2. Tests should verify:
   - Push adds route to stack
   - Pop removes route (but not below 1 item)
   - PopToRoot clears stack
   - State is immutable

**Expected Tests**:
```kotlin
class NavigationReducerTest {
    @Test
    fun pushDestinationAddsToStack() { ... }
    
    @Test
    fun popRemovesLastRoute() { ... }
    
    @Test
    fun popAtRootDoesNothing() { ... }
    
    @Test
    fun popToRootClearsStack() { ... }
    
    @Test
    fun stateIsImmutable() { ... }
}
```

**Target Coverage**: >90% of NavigationReducer

#### 5B: AppCoordinatorTest.kt
**File**: `core/src/commonTest/kotlin/io/umain/munchies/navigation/AppCoordinatorTest.kt`

**What to do**:
1. Copy test template from **NAVIGATION_IMPLEMENTATION_EXAMPLES.md** (Section 3.2)
2. Tests should verify:
   - dispatch() updates state
   - dispatch() emits events
   - convenience methods work
   - state flows are reactive

**Expected Tests**:
```kotlin
class AppCoordinatorTest {
    @Test
    fun dispatchUpdateState() { ... }
    
    @Test
    fun dispatchEmitsEvent() { ... }
    
    @Test
    fun pushConvenienceMethod() { ... }
    
    @Test
    fun navigationStateIsObservable() { ... }
}
```

**Run Tests**:
```bash
./gradlew :core:testDebugUnitTest
# or
./gradlew :core:test
```

---

### Task 6: Verify Phase 0 (1 hour)

**Checklist**:
- [ ] All files compile without errors
- [ ] All tests pass (100%)
- [ ] Test coverage >90% for NavigationReducer and AppCoordinator
- [ ] No ktlint warnings: `./gradlew ktlintCheck`
- [ ] Existing navigation still works:
  ```bash
  ./gradlew :androidApp:build
  # and test on Android
  ```

**Run Full Build**:
```bash
./gradlew build -x iosApp
# or on macOS:
./gradlew build
xcodebuild build -scheme iosApp
```

---

### Task 7: Commit Phase 0 (30 minutes)

**Verify status**:
```bash
git status
```

**Should show**:
- Modified: `core/src/commonMain/kotlin/io/umain/munchies/navigation/AppCoordinator.kt`
- Added: 4 new files
  - `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationState.kt`
  - `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationReducer.kt`
  - `core/src/commonMain/kotlin/io/umain/munchies/navigation/TabNavigationState.kt`
  - `core/src/commonTest/kotlin/io/umain/munchies/navigation/NavigationReducerTest.kt`
  - `core/src/commonTest/kotlin/io/umain/munchies/navigation/AppCoordinatorTest.kt`

**Create Commit**:
```bash
git add core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationState.kt
git add core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationReducer.kt
git add core/src/commonMain/kotlin/io/umain/munchies/navigation/TabNavigationState.kt
git add core/src/commonMain/kotlin/io/umain/munchies/navigation/AppCoordinator.kt
git add core/src/commonTest/kotlin/io/umain/munchies/navigation/NavigationReducerTest.kt
git add core/src/commonTest/kotlin/io/umain/munchies/navigation/AppCoordinatorTest.kt

git commit -m "feat: implement Phase 0 navigation foundation with state management

- Add NavigationState and related data models for route stacking
- Implement pure reducer pattern for state mutations
- Extend AppCoordinator with StateFlow-based state management
- Add >90% unit test coverage for reducer and coordinator
- Enable future phases (modals, tabs, deep links) with solid foundation
"
```

---

## 🎓 Learning Resources for Phase 0

### Key Concepts
1. **Pure Reducers**: Functions with no side effects
   - Read: "Redux Pattern Advantages" in NAVIGATION_SYSTEM_ARCHITECTURE.md
   
2. **StateFlow**: Reactive state management in Kotlin
   - Read: "State Management" section in NAVIGATION_SYSTEM_ARCHITECTURE.md
   - Example: Section 2.6 in NAVIGATION_IMPLEMENTATION_EXAMPLES.md

3. **Data Classes & Sealed Classes**: Kotlin fundamentals
   - Used extensively in NavigationState and StackRoute

### Where to Find Code
All code is in **NAVIGATION_IMPLEMENTATION_EXAMPLES.md**:
- Section 2.1: NavigationState.kt
- Section 2.2: TabNavigationState.kt
- Section 2.5: NavigationReducer.kt
- Section 2.6: AppCoordinator.kt
- Section 3.1: NavigationReducerTest.kt
- Section 3.2: AppCoordinatorTest.kt

### Testing Resources
- Section 3 (entire): Testing strategy, examples, best practices
- NAVIGATION_QUICK_REFERENCE.md: Testing checklist

---

## ⏱️ Time Breakdown

| Task | Hours | Status |
|------|-------|--------|
| Commit current work | 0.25 | Pending |
| Verify build | 0.25 | Pending |
| NavigationState.kt | 1.5 | Pending |
| TabNavigationState.kt | 1 | Pending |
| NavigationReducer.kt | 3 | Pending |
| AppCoordinator.kt | 2 | Pending |
| Unit Tests | 5 | Pending |
| Verify & Fix Issues | 1 | Pending |
| Commit Phase 0 | 0.5 | Pending |
| **TOTAL** | **14.5** | **Pending** |

**Target**: Complete in 3-5 days (3-4 hours per day)

---

## 🚀 Starting Right Now

### Quick Start Command Sequence

**If current changes compile**:
```bash
# 1. Commit current work
git add -A
git commit -m "refactor: reorganize restaurant route providers and simplify handler registration"

# 2. Verify build
./gradlew build -x iosApp

# 3. Create NavigationState.kt
# → Copy from NAVIGATION_IMPLEMENTATION_EXAMPLES.md section 2.1
# → Create at: core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationState.kt

# 4. Build and verify
./gradlew :core:compileKotlin
```

**If current changes don't compile**:
```bash
# Reset and start fresh
git reset --hard HEAD
git clean -fd

# Then proceed with NavigationState.kt as above
```

---

## 🔑 Key Success Factors

1. **Follow the documents**: Every line of code is already written in NAVIGATION_IMPLEMENTATION_EXAMPLES.md
2. **Copy-paste approach**: You're not writing from scratch - use the examples
3. **Test thoroughly**: All 5+ hours of testing time is critical
4. **Commit frequently**: Break it into pieces, don't wait until everything is done
5. **Ask questions**: If something is unclear, check NAVIGATION_QUICK_REFERENCE.md or architecture doc

---

## 📞 When You Get Stuck

1. **Code won't compile?**
   - Check import statements
   - Verify file locations
   - Look at example in NAVIGATION_IMPLEMENTATION_EXAMPLES.md

2. **Tests failing?**
   - Run: `./gradlew :core:test --info`
   - Check test output for specific failures
   - Verify reducer logic against state transitions

3. **Don't understand the design?**
   - Read: NAVIGATION_SYSTEM_ARCHITECTURE.md (relevant section)
   - Look at: NAVIGATION_QUICK_REFERENCE.md (patterns section)
   - Ask: Review architecture doc's FAQ section

---

## ✨ What You'll Have After Phase 0

✅ Working navigation state management  
✅ >90% test coverage  
✅ Foundation for modals (Phase 1)  
✅ Foundation for tabs (Phase 2)  
✅ Foundation for deep links (Phase 3)  
✅ Clear git history with focused commits  
✅ Confidence in the architecture  

---

## 🎯 Phase 0 Complete When

- ✅ All 4 files created and compiling
- ✅ All unit tests passing
- ✅ >90% code coverage
- ✅ Full build passes: `./gradlew build`
- ✅ Existing navigation still works on Android
- ✅ No compiler warnings or lint errors
- ✅ Phase 0 commit created

---

## 📅 Timeline

```
DAY 1: Commit current work + NavigationState.kt + TabNavigationState.kt (4 hours)
DAY 2: NavigationReducer.kt + AppCoordinator.kt (5 hours)
DAY 3: Unit Tests - Part 1 (3 hours)
DAY 4: Unit Tests - Part 2 + Verification (3 hours)
DAY 5: Fixes + Final Build + Commit (2 hours)

TOTAL: 14.5 hours over 5 days (or 3-4 days if full-time)
```

---

## 🎬 Start Now!

You have everything you need. The code is written, the tests are outlined, the documentation is complete.

**Next action**: Choose Option A or B above, then start Task 1.

---

**Questions?** Check:
- NAVIGATION_IMPLEMENTATION_EXAMPLES.md - For code
- NAVIGATION_QUICK_REFERENCE.md - For patterns and debugging
- NAVIGATION_SYSTEM_ARCHITECTURE.md - For design questions

**Good luck! You've got this! 🚀**
