# Navigation System Implementation - Action Plan

**Status**: Phase 0 Foundation - Ready to Begin  
**Branch**: `feature/scaleable-navigation`  
**Date**: February 17, 2026

---

## 🎯 Current State Assessment

### ✅ What's Already Done

1. **Architecture Documents** (Complete)
   - `NAVIGATION_SYSTEM_ARCHITECTURE.md` - Full design blueprint
   - `NAVIGATION_IMPLEMENTATION_EXAMPLES.md` - Code templates
   - `NAVIGATION_QUICK_REFERENCE.md` - Developer reference
   - `NAVIGATION_MIGRATION_CHECKLIST.md` - Project timeline
   - Supporting documents (Summary, Index, Deliverables)

2. **Existing Navigation Foundation**
   - `RouteConstants.kt` - Unified route naming across platforms ✅
   - `AppCoordinator.kt` - Basic event-based coordination ✅
   - `Route.kt`, `Destination.kt`, `Routes.kt` - Route definitions ✅
   - `RouteHandler.kt`, `ScopedRouteHandler.kt` - Handler interfaces ✅
   - `RouteNavigationMapper.kt` - Route to platform mapping ✅
   - `RouteProvider.kt` - Feature module integration interface ✅

3. **Platform-Specific Foundation**
   - Android: `PlatformAppRouteProviders.kt` (modified, needs review)
   - iOS: Route constants bridged from Kotlin, handler reorganized
   - iOS: New structure in `iosApp/iosApp/Features/Restuarants/Navigation/`

### ⚠️ Current Issues

1. **Uncommitted Changes** (6 files modified, need handling)
   - `androidApp/src/main/kotlin/io/umain/munchies/android/navigation/PlatformAppRouteProviders.kt` - Modified (refactored)
   - `iosApp/iosApp/Navigation/Route.swift` - Modified (RouteConstants access)
   - Deleted files:
     - `androidApp/src/main/kotlin/io/umain/munchies/android/features/restaurant/navigation/AndroidRestaurantRouteProvider.kt`
     - `feature-restaurant/src/commonMain/kotlin/io/umain/munchies/feature/restaurant/navigation/RestaurantRouteProvider.kt`
     - `iosApp/iosApp/Navigation/RestaurantDetailRouteHandlerSwift.swift`
     - `iosApp/iosApp/Navigation/RestaurantListRouteHandlerSwift.swift`

2. **Missing Phase 0 Implementation**
   - No `NavigationState.kt` (core state model)
   - No `NavigationReducer.kt` (state mutations)
   - No `ModalDestination.kt` (modal support)
   - No `TabNavigationState.kt` (tab support)
   - No unit tests for navigation logic

3. **Platform Integration Gaps**
   - Android: No `ModalLayer.kt` composable
   - iOS: Handler references may be incorrect
   - iOS: Missing proper Swift navigation wrapper

---

## 📋 Implementation Roadmap

### Phase 0: Foundation (Week 1 - 15 hours)

#### Step 1: Handle Current Branch State (1 hour)

**Decision Point**: What to do with current changes?

**Option A - Clean Reset** (Recommended)
```bash
git reset --hard HEAD~
git clean -fd
```
- Pros: Start fresh, clear history
- Cons: Loses current refactoring work
- Use if: Current changes are incomplete/untested

**Option B - Commit Current Progress**
```bash
git add -A
git commit -m "refactor: reorganize restaurant route providers and simplify handler registration"
```
- Pros: Preserves progress, clear history
- Cons: May need to fix issues in next steps
- Use if: Changes are tested and working

**Option C - Stash and Branch**
```bash
git stash
git checkout -b feature/phase0-foundation
```
- Pros: Preserves work, clean separation
- Cons: Need to merge later
- Use if: Want to keep options open

**→ Action**: Choose option and execute (1 hour)

---

#### Step 2: Create Core Navigation Data Models (3 hours)

**Files to Create**:

1. **`core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationState.kt`**
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
       data class FilterModal(val currentFilters: List<String>) : ModalRoute()
       // Add more modal types as needed
   }
   ```

2. **`core/src/commonMain/kotlin/io/umain/munchies/navigation/TabNavigationState.kt`**
   ```kotlin
   data class TabNavigationState(
       val tabs: Map<String, TabStack> = mapOf(
           "home" to TabStack(listOf(StackRoute.RestaurantList))
       ),
       val activeTab: String = "home"
   )
   
   data class TabStack(
       val routes: List<StackRoute> = emptyList()
   )
   ```

3. **`core/src/commonMain/kotlin/io/umain/munchies/navigation/ModalDestination.kt`**
   ```kotlin
   sealed class ModalDestination {
       data class Filter(val current: List<String>) : ModalDestination()
       // Add more modal destinations as needed
   }
   ```

**Copy code from**: `NAVIGATION_IMPLEMENTATION_EXAMPLES.md` (sections 2.1-2.4)

**✅ Success Criteria**:
- Files compile without errors
- Data structures match architecture doc
- Can create instances in unit tests

---

#### Step 3: Implement Navigation Reducer (3 hours)

**File to Create**:
`core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationReducer.kt`

```kotlin
class NavigationReducer {
    fun reduce(state: NavigationState, event: NavigationEvent): NavigationState {
        return when (event) {
            is NavigationEvent.Push -> handlePush(state, event.destination)
            NavigationEvent.Pop -> handlePop(state)
            NavigationEvent.PopToRoot -> state.copy(
                primaryStack = listOf(StackRoute.RestaurantList),
                modalStack = emptyList()
            )
        }
    }
    
    private fun handlePush(state: NavigationState, destination: Destination): NavigationState {
        val newRoute = when (destination) {
            is Destination.RestaurantList -> StackRoute.RestaurantList
            is Destination.RestaurantDetail -> StackRoute.RestaurantDetail(destination.restaurantId)
        }
        return state.copy(
            primaryStack = state.primaryStack + newRoute
        )
    }
    
    private fun handlePop(state: NavigationState): NavigationState {
        return if (state.primaryStack.size > 1) {
            state.copy(primaryStack = state.primaryStack.dropLast(1))
        } else {
            state
        }
    }
}
```

**Copy code from**: `NAVIGATION_IMPLEMENTATION_EXAMPLES.md` (section 2.5)

**✅ Success Criteria**:
- Reducer handles all NavigationEvent types
- State transformations are immutable
- No side effects in reducer functions

---

#### Step 4: Extend AppCoordinator (2 hours)

**File to Update**:
`core/src/commonMain/kotlin/io/umain/munchies/navigation/AppCoordinator.kt`

**Add**:
```kotlin
class AppCoordinator {
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    private val reducer = NavigationReducer()
    
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 10)
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    fun dispatch(event: NavigationEvent) {
        val newState = reducer.reduce(_navigationState.value, event)
        _navigationState.value = newState
        _navigationEvents.tryEmit(event)
    }
    
    // Convenience methods
    fun push(destination: Destination) {
        dispatch(NavigationEvent.Push(destination))
    }
    
    fun pop() {
        dispatch(NavigationEvent.Pop)
    }
    
    fun popToRoot() {
        dispatch(NavigationEvent.PopToRoot)
    }
}
```

**Copy code from**: `NAVIGATION_IMPLEMENTATION_EXAMPLES.md` (section 2.6)

**✅ Success Criteria**:
- AppCoordinator has navigationState Flow
- dispatch() method works with reducer
- Convenience methods delegate to dispatch()

---

#### Step 5: Write Phase 0 Unit Tests (4 hours)

**Files to Create**:

1. **`core/src/commonTest/kotlin/io/umain/munchies/navigation/NavigationReducerTest.kt`**
   - Test each reducer function independently
   - Test state transitions
   - Test edge cases (empty stack, etc.)
   - Target: 90%+ coverage

2. **`core/src/commonTest/kotlin/io/umain/munchies/navigation/AppCoordinatorTest.kt`**
   - Test dispatch() method
   - Test navigation state updates
   - Test event emission
   - Test convenience methods

**Copy test templates from**: `NAVIGATION_IMPLEMENTATION_EXAMPLES.md` (sections 3.1-3.2)

**✅ Success Criteria**:
- All tests pass
- >90% code coverage
- Tests verify state immutability
- Tests verify correct reducer behavior

---

### Phase 0 Completion Checklist

- [ ] Choose and execute branch state handling
- [ ] Create `NavigationState.kt`
- [ ] Create `TabNavigationState.kt`
- [ ] Create `ModalDestination.kt`
- [ ] Create `NavigationReducer.kt`
- [ ] Update `AppCoordinator.kt`
- [ ] Create unit tests (reducer + coordinator)
- [ ] All tests passing
- [ ] No compiler warnings
- [ ] Code review before commit
- [ ] Commit Phase 0: `git commit -m "feat: implement Phase 0 navigation foundation with state management"`

---

## 🚀 Next Phases (Overview)

### Phase 1: Modal Implementation (Weeks 2-3)

**Android**:
- Create `ModalLayer.kt` composable
- Add modal rendering logic to existing screens
- Test modal back button behavior

**iOS**:
- Implement SwiftUI modal presentation
- Handle modal dismissal
- Test modal back navigation

**Shared**:
- Add `ModalDestination` handling to reducer
- Add modal-specific navigation commands

### Phase 2: Tab Navigation (Weeks 4-5)

**Shared**:
- Implement `TabNavigationState` handling in reducer
- Add tab switching logic
- Preserve tab history

**Android/iOS**:
- Implement tab UI
- Integrate with navigation state
- Handle tab switching + restoration

### Phase 3: Deep Links (Weeks 6-7)

**Shared**:
- Create `DeepLinkHandler` interface
- Implement deep link parsing

**Android**:
- Configure intent filters
- Handle deep links in activity

**iOS**:
- Configure universal links
- Handle deep links in app delegate

---

## 📊 Implementation Timeline

```
Week 1 (Phase 0): Foundation
├─ Mon-Tue: Branch state + data models (4 hours)
├─ Wed: Reducer implementation (3 hours)
├─ Thu: AppCoordinator extension + tests (5 hours)
├─ Fri: Review, fixes, commit (3 hours)
└─ Status: ✅ Phase 0 Complete

Week 2-3 (Phase 1): Modals
├─ Android: ModalLayer + integration (5 hours)
├─ iOS: Modal presentation (5 hours)
├─ Shared: Reducer updates (2 hours)
├─ Testing: Unit + integration (3 hours)
└─ Status: ✅ Phase 1 Complete

Week 4-5 (Phase 2): Tabs
├─ Shared: TabNavigationState + reducer (4 hours)
├─ Android: Tab UI + integration (6 hours)
├─ iOS: Tab UI + integration (6 hours)
└─ Status: ✅ Phase 2 Complete

Week 6-7 (Phase 3): Deep Links
├─ Shared: Deep link parsing (3 hours)
├─ Android: Intent filter setup (2 hours)
├─ iOS: Universal links setup (2 hours)
└─ Status: ✅ Phase 3 Complete

Total: 8 weeks, 56 hours
```

---

## 🛠️ Development Setup

### Required Tools
- Kotlin 1.9+
- Gradle 8.0+
- Android Studio latest
- Xcode latest
- Git

### Build Commands
```bash
# Build all
./gradlew build

# Build core module only
./gradlew :core:build

# Run Android tests
./gradlew :core:testDebugUnitTest

# Build iOS (from iosApp)
xcodebuild build -scheme iosApp
```

### Testing Commands
```bash
# Run all tests
./gradlew allTests

# Run with coverage
./gradlew :core:testDebugUnitTestCoverage
```

---

## 📚 Reference Documents

| Document | Use For |
|----------|---------|
| `NAVIGATION_SYSTEM_ARCHITECTURE.md` | Design questions, architecture review |
| `NAVIGATION_IMPLEMENTATION_EXAMPLES.md` | Code templates, copy-paste snippets |
| `NAVIGATION_QUICK_REFERENCE.md` | Pattern lookup, debugging, testing checklist |
| `NAVIGATION_MIGRATION_CHECKLIST.md` | Project timeline, risk management, team coordination |
| `IMPLEMENTATION_ACTION_PLAN.md` (this file) | Step-by-step implementation guide |

---

## 🎯 Success Criteria for Phase 0

- ✅ All data models compile without errors
- ✅ Reducer has >90% unit test coverage
- ✅ No circular dependencies between modules
- ✅ Existing navigation system still works
- ✅ Code passes style checks (ktlint)
- ✅ Can successfully push/pop stack routes
- ✅ Navigation state updates flow correctly

---

## 💡 Pro Tips

1. **Start Small**: Implement just the reducer first, test it thoroughly
2. **Use TDD**: Write tests before implementing reducer functions
3. **Review Code**: Have a team member review before each commit
4. **Commit Often**: Small, focused commits are easier to debug
5. **Document as You Go**: Add comments explaining design decisions
6. **Test Platforms**: Verify on both Android and iOS early

---

## ❓ FAQ

**Q: Should we delete the old route files?**
A: Not yet. Wait until Phase 1 is complete and tested to ensure backward compatibility.

**Q: Can we skip Phase 0 and go directly to Phase 1?**
A: No. Phase 0 foundation is required for Phases 1-3 to work. Skip it only if you modify the approach fundamentally.

**Q: What if existing tests fail?**
A: Run `./gradlew :core:test --info` to see detailed errors. Common issues:
- Missing imports
- Type mismatches in reducer
- Incorrect state initialization

**Q: How do we measure progress?**
A: By phase completion:
- Phase 0: Reducer works + >90% test coverage
- Phase 1: Modals render + back button works
- Phase 2: Tabs switch + history preserved
- Phase 3: Deep links open correct routes

---

## 🚨 Common Pitfalls to Avoid

1. **Don't mutate state directly** - Always use `copy()` or create new instances
2. **Don't add side effects to reducer** - Reducer must be pure
3. **Don't skip tests** - They catch issues early
4. **Don't refactor while implementing** - Stay focused on Phase 0 goals
5. **Don't forget to test edge cases** - Empty stacks, single item, etc.

---

## 📞 Getting Help

If you get stuck:

1. **Check NAVIGATION_QUICK_REFERENCE.md** - Troubleshooting section
2. **Look at NAVIGATION_IMPLEMENTATION_EXAMPLES.md** - Copy working code
3. **Review NAVIGATION_SYSTEM_ARCHITECTURE.md** - Understand the design
4. **Check git history** - See what changed and why

---

**Created**: February 17, 2026  
**Last Updated**: February 17, 2026  
**Status**: Ready for implementation  
**Next Action**: Choose branch handling strategy and begin Step 1
