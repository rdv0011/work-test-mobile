# KMP Navigation System - Migration Checklist & Decision Framework

**Purpose**: Structured plan for rolling out the new navigation system  
**Status**: Ready to execute

---

## Pre-Implementation Decision Framework

### Before Starting, Answer These Questions

#### 1. **What navigation patterns do you need?**

- [ ] Linear stack (push → pop) only
- [ ] Linear stack + modal overlays
- [ ] Linear stack + modals + tabs
- [ ] Tabs with modals
- [ ] All of the above (future-proof)

**Decision**: Start with what you need NOW, architecture supports adding later.

#### 2. **Timeline & Team Capacity**

- [ ] Can dedicate 1-2 developers for 4-7 weeks?
- [ ] Can do this in parallel with feature development?
- [ ] Do you have time for testing and iteration?

**Decision**: Phase-based approach allows parallel work. Recommend allocating 1-2 devs full-time.

#### 3. **Platform Priority**

- [ ] Android only for now?
- [ ] iOS only?
- [ ] Parallel implementation (recommended)?

**Decision**: Recommend parallel; most code is shared. Platform-specific code is minimal.

#### 4. **Breaking Changes Tolerance**

- [ ] Can modify `AppCoordinator` usage?
- [ ] Can update all screens to use `NavigationCoordinator`?
- [ ] Can test thoroughly before shipping?

**Decision**: New system is backward compatible if desired. Can migrate gradually.

#### 5. **Deep Link Timeline**

- [ ] Implement now (notifications need it)?
- [ ] Implement later (phase 3)?
- [ ] Not needed yet?

**Decision**: Recommend implementing in Phase 3, gives time for other features to stabilize.

---

## Phase 0: Foundation (Week 1)

### Goals
- ✅ Establish new data model
- ✅ Create pure reducers (testable)
- ✅ Maintain backward compatibility
- ✅ Achieve >90% test coverage

### Ownership & Responsibilities

| Task | Owner | Time | Verification |
|------|-------|------|--------------|
| Create core data models | Lead Dev | 1-2 days | Compiles, no LSP errors |
| Implement NavigationReducer | Lead Dev | 1-2 days | 20+ unit tests passing |
| Extend NavigationCoordinator | Lead Dev | 1 day | API complete, docs written |
| Write reducer tests | QA/Dev | 1-2 days | >90% coverage |
| Verify backward compat | Lead Dev | 1 day | Existing navigation works |
| Code review & cleanup | Team | 1 day | No comments |

### Pre-Checklist

**Before starting Phase 0**:
- [ ] Team aligned on approach
- [ ] One developer assigned as navigation owner
- [ ] Testing environment set up
- [ ] Feature branches ready
- [ ] Documentation repository ready

### Step-by-Step Implementation

#### Step 0.1: Create Base Data Models (2 hours)

```bash
# In core/src/commonMain/kotlin/io/umain/munchies/navigation/

# Create new files:
- NavigationState.kt
- ModalDestination.kt
- TabNavigationState.kt

# Files should be copied from NAVIGATION_IMPLEMENTATION_EXAMPLES.md
```

**Verification**:
```bash
./gradlew core:compileKotlin
# Should complete without errors
```

#### Step 0.2: Update NavigationEvent (1 hour)

**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationEvent.kt`

**Changes**:
- Add modal events: `ShowModal`, `DismissModal`, `DismissAllModals`
- Add tab events: `SelectTab`, `PushInTab`, `PopInTab`
- Add deep link event: `ApplyNavigationState`
- Keep existing events: `Push`, `Pop`, `PopToRoot`

**Verification**:
```bash
./gradlew core:compileKotlin
# Should compile without warnings
```

#### Step 0.3: Create NavigationReducer (3 hours)

**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationReducer.kt`

**Structure**:
```
NavigationReducer (object)
├─ reduce() [public entry point]
├─ handlePush()
├─ handlePop()
├─ handlePopToRoot()
├─ handleShowModal()
├─ handleDismissModal()
├─ handleSelectTab()
├─ handlePushInTab()
├─ handlePopInTab()
└─ (private helper functions)
```

**Verification**:
```bash
./gradlew core:compileKotlin
# Should compile
```

#### Step 0.4: Extend NavigationCoordinator (2 hours)

**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationCoordinator.kt`

**Current vs New**:
```kotlin
// OLD
class AppCoordinator {
    fun navigateTo(destination: Destination)
    fun navigateBack()
    fun navigateToRoot()
}

// NEW (extends above)
class NavigationCoordinator {
    // Keep old methods
    fun navigateToScreen(destination: Destination)
    fun navigateBack()
    fun navigateToRoot()
    
    // NEW methods
    fun showModal(destination: ModalDestination)
    fun dismissModal()
    fun selectTab(tabId: String)
    fun navigateInTab(destination: Destination)
    fun applyNavigationState(newState: NavigationState)
}

// Backward compatibility
typealias AppCoordinator = NavigationCoordinator
```

**Verification**:
```bash
./gradlew core:compileKotlin
# Should compile
```

#### Step 0.5: Extend RouteHandler Interface (1 hour)

**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/RouteHandler.kt`

**Addition**:
```kotlin
interface RouteHandler {
    // Existing (unchanged)
    val route: Route
    fun toRouteString(): String
    fun canHandle(destination: Destination): Boolean
    fun destinationToRoute(destination: Destination): Route?
    
    // NEW (optional, default implementations)
    fun canHandleModal(destination: ModalDestination): Boolean = false
    fun destinationToModalRoute(destination: ModalDestination): ModalRoute? = null
}
```

**Why**: Allows existing handlers to work unchanged. Features can opt-in to modal support.

**Verification**:
```bash
./gradlew compileKotlin
# No errors on existing handlers
```

#### Step 0.6: Write Reducer Tests (3 hours)

**File**: `androidApp/src/test/kotlin/navigation/NavigationReducerTest.kt`

**Test Cases** (minimum 20):
- [ ] Push adds to stack
- [ ] Pop removes from stack
- [ ] Pop with modal shows dismisses modal
- [ ] Show modal doesn't affect stack
- [ ] Dismiss modal pops modal stack
- [ ] Dismiss all modals clears stack
- [ ] Select tab updates active tab
- [ ] Push in tab updates tab's stack only
- [ ] Pop to root clears stack
- [ ] Apply state overwrites current state

**Verification**:
```bash
./gradlew androidApp:testDebugUnitTest
# Should have >90% coverage
# All tests green
```

#### Step 0.7: Integration Test - No Breaking Changes (2 hours)

**Verify**: Existing navigation still works

```bash
# Run existing navigation tests
./gradlew androidApp:connectedAndroidTest

# Compile iOS app
cd iosApp && xcodebuild -scheme iosApp -configuration Debug

# Verify:
# - App launches
# - Can navigate list → detail → back
# - No crashes
# - No new warnings
```

#### Step 0.8: Documentation (1 hour)

- [ ] Add navigation docs to team wiki
- [ ] Add comments to key classes
- [ ] Create ADR (Architecture Decision Record)

### Phase 0 Completion Checklist

- [ ] All new data classes created and compiling
- [ ] NavigationReducer implemented with >90% test coverage
- [ ] NavigationCoordinator extended (backward compatible)
- [ ] RouteHandler extended with optional modal support
- [ ] Existing navigation tests pass
- [ ] No new LSP errors
- [ ] Code reviewed by team
- [ ] Documentation complete
- [ ] Ready for Phase 1

### Phase 0 Estimated Timeline

| Task | Time | Total |
|------|------|-------|
| Data models | 2 hrs | 2 hrs |
| Update events | 1 hr | 3 hrs |
| Reducer | 3 hrs | 6 hrs |
| Extend coordinator | 2 hrs | 8 hrs |
| Update interface | 1 hr | 9 hrs |
| Tests | 3 hrs | 12 hrs |
| Integration tests | 2 hrs | 14 hrs |
| Docs | 1 hr | 15 hrs |
| **TOTAL** | | **15 hours** |

**Reality**: With code review, debugging, refactoring → **4-5 days for 1 developer**

---

## Phase 1: Modal Support (Weeks 2-3)

### Goals
- ✅ Show/dismiss modals
- ✅ Test modal lifecycle
- ✅ Implement first feature modal (FilterModal)
- ✅ No breaking changes to existing screens

### Ownership & Responsibilities

| Task | Owner | Platform | Time |
|------|-------|----------|------|
| ModalLayer composable | Android Dev | Android | 2 days |
| Modal route handlers | Feature Dev | Both | 1 day |
| Modal views/composables | Android Dev | Android | 2 days |
| Modal presentation | iOS Dev | iOS | 2 days |
| Modal dismissal logic | Both | Both | 1 day |
| Tests | QA/Both | Both | 2 days |
| **TOTAL** | | | **10 days** |

### Step-by-Step Implementation

#### Step 1.1: Android - ModalLayer Composable (2 days)

**File**: `androidApp/src/main/kotlin/io/umain/munchies/android/navigation/modals/ModalLayer.kt`

**Structure**:
```kotlin
@Composable
fun ModalLayer(
    navigationState: NavigationState,
    coordinator: NavigationCoordinator
) {
    navigationState.topModal?.let { modal ->
        when (modal) {
            is FilterModalRoute -> ModalBottomSheet(...)
            // Add more modals here as features add support
        }
    }
}
```

**Verification**:
```bash
./gradlew androidApp:compileDebugKotlin
# Should compile
```

#### Step 1.2: Android - Update AppNavigation (1 day)

**File**: `androidApp/src/main/kotlin/io/umain/munchies/android/navigation/AppNavigation.kt`

**Changes**:
- Integrate `ModalLayer` composable after `NavHost`
- Wire up `navigationState` from coordinator
- Connect modal dismissal to coordinator

**Current Structure**:
```kotlin
@Composable
fun AppNavigation(coordinator: NavigationCoordinator) {
    val navController = rememberNavController()
    
    // ... existing setup ...
    
    CompositionLocalProvider(LocalRouteRegistry provides registry) {
        NavHost(...) { /* existing routes */ }
        // ← ADD HERE
        ModalLayer(navigationState, coordinator)
    }
}
```

#### Step 1.3: Feature - FilterModalRoute Handler (1 day)

**Files**:
- `feature-restaurant/src/commonMain/kotlin/.../FilterModalRoute.kt`
- `feature-restaurant/src/commonMain/kotlin/.../FilterModalRouteHandler.kt`

**Implementation**:
```kotlin
// FilterModalRoute.kt
data class FilterModalRoute(
    val preSelectedFilters: List<String> = emptyList()
) : ModalRoute {
    override val key: String = "FilterModal_${preSelectedFilters.hashCode()}"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}

// FilterModalRouteHandler.kt
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

#### Step 1.4: Android - FilterModal Composable (2 days)

**File**: `feature-restaurant/src/androidMain/kotlin/.../FilterModalComposable.kt`

**Implementation**:
```kotlin
@Composable
fun FilterModalContent(
    preSelectedFilters: List<String>,
    onApply: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // UI implementation
}
```

#### Step 1.5: iOS - Modal Presentation (2 days)

**File**: `iosApp/iosApp/Navigation/ModalPresentationLayer.swift`

**Implementation**:
```swift
struct ModalPresentationLayer: View {
    let navigationState: NavigationState
    let coordinator: NavigationCoordinator
    
    var body: some View {
        Group {
            if let topModal = navigationState.topModal {
                modalView(for: topModal)
            }
        }
    }
    
    @ViewBuilder
    private func modalView(for modal: any ModalRoute) -> some View {
        switch modal {
        case let filterModal as FilterModalRoute:
            sheet(isPresented: .constant(true)) {
                FilterModalView(...)
            }
        default:
            EmptyView()
        }
    }
}
```

#### Step 1.6: Tests - Modal Lifecycle (2 days)

**Files**:
- `androidApp/src/test/kotlin/navigation/ModalNavigationTest.kt`
- `androidApp/src/androidTest/kotlin/navigation/ModalUITest.kt`

**Test Cases**:
- [ ] Show modal updates state
- [ ] Dismiss modal pops modal stack
- [ ] Back button dismisses modal (not screen)
- [ ] Modal not visible when stack empty
- [ ] Modal content renders correctly
- [ ] Multiple modals stack correctly

**Unit Tests**:
```kotlin
@Test
fun `show modal adds to stack`() {
    val initialState = NavigationState()
    val event = NavigationEvent.ShowModal(ModalDestination.Filter())
    
    val newState = NavigationReducer.reduce(initialState, event)
    
    assertEquals(1, newState.modalStack.size)
}
```

**UI Tests**:
```kotlin
@Test
fun `modal appears when shown`() {
    composeRule.setContent {
        AppNavigation(coordinator)
    }
    
    coordinator.showModal(ModalDestination.Filter())
    composeRule.waitForIdle()
    
    composeRule.onNodeWithTag("filter_modal").assertIsDisplayed()
}
```

### Phase 1 Completion Checklist

- [ ] ModalLayer composable implemented (Android)
- [ ] AppNavigation updated with ModalLayer
- [ ] FilterModalRoute & handler created
- [ ] FilterModal view/composable implemented
- [ ] Modal presentation implemented (iOS)
- [ ] Modal lifecycle tests pass
- [ ] UI tests pass
- [ ] No breaking changes to existing features
- [ ] Code reviewed
- [ ] Ready for Phase 2

---

## Phase 2: Tab Support (Weeks 4-5)

### Goals
- ✅ Support multiple navigation stacks per tab
- ✅ Preserve tab history when switching
- ✅ Handle back button in tabbed context
- ✅ Modals work on top of tabs

### Implementation Outline

#### Step 2.1: Data Model Complete
- [ ] `TabNavigationState` fully implemented
- [ ] `NavigationState.usesTabs` flag added
- [ ] Reducer handles tab events

#### Step 2.2: Android TabNavigation Composable
- [ ] `TabNavigation.kt` created
- [ ] `BottomNavigationBar` implemented
- [ ] Tab switching wired to coordinator

#### Step 2.3: iOS TabNavigationView
- [ ] `TabNavigationView.swift` created
- [ ] `TabView` integration complete
- [ ] Tab switching works

#### Step 2.4: Tests
- [ ] Tab switching preserves history
- [ ] Back button works in tabs
- [ ] Modal works on top of tabs

### Phase 2 Timeline: 10-12 days

---

## Phase 3: Deep Links (Weeks 6-7)

### Goals
- ✅ Parse deep links to navigation state
- ✅ Handle notifications with deep links
- ✅ Support URL schemes and universal links
- ✅ Graceful fallback for invalid links

### Implementation Outline

#### Step 3.1: Core Infrastructure
- [ ] `DeepLinkHandler` interface
- [ ] `DeepLinkParser` registry
- [ ] `DeepLinkResult` sealed class

#### Step 3.2: Feature Handlers
- [ ] `RestaurantDeepLinkHandler`
- [ ] `ReviewsDeepLinkHandler`
- [ ] (Per-feature implementation)

#### Step 3.3: Android Integration
- [ ] `DeepLinkProcessor` in MainActivity
- [ ] Intent URI parsing
- [ ] AndroidManifest.xml schemes

#### Step 3.4: iOS Integration
- [ ] `DeepLinkProcessor` in SceneDelegate
- [ ] Universal Links configuration
- [ ] Notification handling

#### Step 3.5: Tests
- [ ] Deep link parsing tests
- [ ] Invalid link handling
- [ ] End-to-end deep link navigation

### Phase 3 Timeline: 7-10 days

---

## Risk Mitigation

### Risk 1: Breaking Existing Navigation

**Mitigation**:
- [ ] Keep `AppCoordinator` as alias
- [ ] Old events still work
- [ ] Old handler interface extends without breaking
- [ ] Comprehensive backward compat tests

**Rollback Plan**:
- Feature branch with tests
- Can revert if issues
- Zero production impact

### Risk 2: Performance Issues

**Mitigation**:
- [ ] Monitor memory with multiple modals/tabs
- [ ] Profile build times
- [ ] Test with realistic navigation depth

**Action if Issue**:
- [ ] Implement stack depth limits
- [ ] Lazy-load tab content
- [ ] Audit reducer performance

### Risk 3: iOS Implementation Delays

**Mitigation**:
- [ ] Start iOS in parallel with Android
- [ ] Shared data models ready upfront
- [ ] Separate iOS Dev to work independently

**Action if Delayed**:
- [ ] Can ship Android modals first
- [ ] iOS can catch up next sprint
- [ ] Not blocking feature work

### Risk 4: Deep Link Complexity

**Mitigation**:
- [ ] Implement in Phase 3 (gives time)
- [ ] Start simple (one feature)
- [ ] Expand gradually

**Action if Complex**:
- [ ] Push to Phase 4/next quarter
- [ ] Modals + Tabs work without deep links
- [ ] Can add incrementally

---

## Go/No-Go Decision Points

### After Phase 0: Foundation
**Questions**:
- Can reducers pass tests consistently?
- Does existing navigation still work?
- Are new data models correct?

**Go if**: All questions are "yes"  
**No-Go if**: Issues with backward compatibility  
**Escalation**: Call architecture review meeting

### After Phase 1: Modals
**Questions**:
- Do modals appear/dismiss correctly?
- Does back button work properly?
- Are UI tests stable?

**Go if**: All questions are "yes"  
**No-Go if**: Back button logic confused or flaky tests  
**Escalation**: May need to revise modal dismissal strategy

### After Phase 2: Tabs
**Questions**:
- Do tab histories persist correctly?
- Do modals work on top of tabs?
- Is memory usage acceptable?

**Go if**: All questions are "yes"  
**No-Go if**: Memory issues or history not preserved  
**Escalation**: May need to revise tab stack preservation

### Before Phase 3: Deep Links
**Questions**:
- Is there need for deep links now?
- Do we have time for implementation?
- Are notifications/marketing ready?

**Go if**: Clear use case and timeline exists  
**No-Go if**: Can defer to future sprint  
**Escalation**: Product/marketing alignment needed

---

## Success Metrics

### Code Quality
- ✅ >90% test coverage for reducers
- ✅ Zero breaking changes to existing code
- ✅ <5% change in build time
- ✅ Zero new LSP errors

### Functional
- ✅ Modals work on Android and iOS
- ✅ Tab history preserved when switching
- ✅ Back button behavior consistent
- ✅ Deep links navigate correctly

### Performance
- ✅ No memory leaks with modals/tabs
- ✅ Smooth animations on transitions
- ✅ No noticeable lag when opening modals
- ✅ App startup time unchanged

### Team Satisfaction
- ✅ Developers understand navigation flow
- ✅ Adding new routes is easy
- ✅ Debugging navigation issues is straightforward
- ✅ Team feels confident in architecture

---

## Rollback Plan

If critical issues arise:

**Phase 0 Issues**:
- Revert to previous AppCoordinator
- Keep new data classes (not conflicting)
- Plan follow-up sprint

**Phase 1 Issues**:
- Remove ModalLayer from AppNavigation
- Keep modal route handlers (just unused)
- Continue with existing navigation

**Phase 2 Issues**:
- Remove tab support
- Keep modal support
- Fall back to flat navigation

**Phase 3 Issues**:
- Disable deep link processing
- Continue with modals and tabs
- Revisit deep links later

---

## Communication Plan

### Week 1 (Phase 0)
- [ ] Announce navigation refactor in team standup
- [ ] Share architecture document
- [ ] Schedule alignment meeting

### Week 2-3 (Phase 1)
- [ ] Weekly progress updates
- [ ] Demo modal functionality
- [ ] Gather team feedback

### Week 4-5 (Phase 2)
- [ ] Tab navigation demo
- [ ] Discuss with product (tab UX)
- [ ] Plan next features needing tabs

### Week 6-7 (Phase 3)
- [ ] Deep link demo
- [ ] Coordinate with marketing (if needed)
- [ ] Plan notification integration

### Week 8+
- [ ] Retrospective on architecture
- [ ] Document lessons learned
- [ ] Plan next improvements

---

## Team Roles & Responsibilities

### Navigation Owner (1 Full-Time Dev)
- Responsible for all navigation code
- Approves PRs for navigation changes
- Maintains documentation
- Handles architecture decisions

### Android Developer
- Implements Android-specific code
- Writes Compose-specific tests
- Ensures smooth animations
- Maintains `androidApp/navigation/`

### iOS Developer  
- Implements iOS-specific code
- Ensures SwiftUI integration
- Maintains `iosApp/Navigation/`
- Works in parallel with Android

### Feature Developers
- Implement feature-specific routes/handlers
- Register in feature's RouteProvider
- Write feature navigation tests
- Provide feedback on API

### QA
- Tests navigation flows
- Writes integration tests
- Tests edge cases (rapid navigation, crashes)
- Reports issues

---

## Learning Resources

### For Your Team
1. Read `NAVIGATION_SYSTEM_ARCHITECTURE.md` (40 min)
2. Read `NAVIGATION_QUICK_REFERENCE.md` (20 min)
3. Review `NAVIGATION_IMPLEMENTATION_EXAMPLES.md` (30 min)
4. Run Phase 0 reducer tests (understand flow)

### For Architecture Deep Dives
- Redux pattern documentation
- State management in Compose
- SwiftUI navigation patterns
- Deep linking best practices

---

## Final Checklist Before Starting

- [ ] Team has read architecture documents
- [ ] Navigation owner identified
- [ ] Calendar blocked for Phase 0 (1 week)
- [ ] Feature branch created
- [ ] Code review process established
- [ ] Testing environment ready
- [ ] Documentation repository ready
- [ ] Go/no-go criteria agreed upon
- [ ] Rollback plan understood
- [ ] Success metrics defined

**Sign-Off**: Architecture review approved by:
- [ ] Tech Lead
- [ ] Product Manager
- [ ] QA Lead

---

## Timeline Summary

```
Week 1: Phase 0 (Foundation) ✓
├─ Monday-Tuesday: Data models + events
├─ Wednesday: Reducer implementation  
├─ Thursday: Coordinator extension
└─ Friday: Tests + code review

Week 2-3: Phase 1 (Modals) ✓
├─ Android ModalLayer + composables
├─ iOS modal presentation
├─ Feature modal handlers
└─ Tests + integration

Week 4-5: Phase 2 (Tabs) ✓
├─ TabNavigation implementation
├─ Tab switching logic
├─ History preservation
└─ Tests + integration

Week 6-7: Phase 3 (Deep Links) ✓
├─ Deep link parsing
├─ Feature handlers
├─ Platform integration
└─ Tests + documentation

Week 8: Review & Polish
├─ Retrospective
├─ Documentation updates
├─ Team training
└─ Ready for production
```

**Total: 8 weeks, 1-2 developers full-time**

