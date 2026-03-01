# Navigation Architecture Guide

## Overview

This document describes the navigation architecture for Munchies mobile app (iOS & Android). The approach balances **separation of concerns** with **code reuse** across platforms.

---

## Architecture Pattern: Hybrid Coordinator + Dispatcher

### Three-Layer Navigation Stack

```
┌─────────────────────────────────────────┐
│ UI Layer (Views/Composables)            │
│ - Declares intent to navigate            │
│ - Calls navigationViewModel directly     │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ Feature-Scoped Navigation ViewModels     │
│ - RestaurantNavigationViewModel          │
│ - SettingsNavigationViewModel            │
│ - Exposes semantic navigation actions    │
│ - Single responsibility: route changes   │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ Core Navigation Dispatcher               │
│ - Emits NavigationEvent                  │
│ - Platform-agnostic event stream         │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ Platform-Specific Handlers               │
│ - iOS: NavigationCoordinator             │
│ - Android: AppNavigation                 │
│ - Updates UI state (NavigationPath, etc) │
└─────────────────────────────────────────┘
```

### Data Flow: Complete Example

**Scenario: User taps "View Restaurant Detail" in Restaurant List**

#### **On Both iOS & Android:**

```
RestaurantListView/Screen
    ↓ (User taps restaurant card)
    ↓ navigationViewModel.showRestaurantDetail(restaurantId: "123")
    ↓
RestaurantNavigationViewModel.showRestaurantDetail()
    ↓ dispatcher.navigate(Destination.RestaurantDetail("123"))
    ↓
NavigationDispatcher emits:
    → NavigationEvent.Push(Destination.RestaurantDetail("123"))
```

#### **iOS Specific:**

```
NavigationEvent.Push received by NavigationCoordinator
    ↓ Appends corresponding Route to NavigationPath
    ↓ SwiftUI detects state change
    ↓ Pushes RestaurantDetailView
```

#### **Android Specific:**

```
NavigationEvent.Push received by AppNavigation
    ↓ NavController.navigate(route) with NavHost
    ↓ Composable recomposes with new screen
    ↓ Displays RestaurantDetailScreen
```

---

## Key Design Decisions & Rationale

### **Decision 1: Separate Navigation VMs from Shared VMs**

**Architecture:**
- ✅ `RestaurantNavigationViewModel` (owns: navigation actions)
- ✅ `RestaurantListViewModel` (owns: state + business logic)
- ❌ NO wrapper methods in SharedVMs

**Rationale:**

Navigation is a **cross-cutting concern**. By separating it:

1. **Reusability**: Same `RestaurantNavigationViewModel` can be injected into any screen in the restaurant feature
   - `RestaurantListView` uses it to navigate to detail
   - `RestaurantDetailView` uses it to go back or show modals
   - Avoids duplication of `showRestaurantDetail()`, `navigateBack()` across multiple VMs

2. **Single Responsibility**: Each ViewModel has one reason to change
   - SharedVM changes only when state logic changes
   - NavVM changes only when navigation actions change
   - Easier to test, debug, and maintain

3. **Platform Consistency**: Both iOS and Android can use the same pattern
   - UI → NavVM → Dispatcher
   - No "Android specific" routing logic mixed with state management

### **Decision 2: UI Calls Navigation VMs Directly**

**Pattern (on both platforms):**
```kotlin
// ✅ CORRECT: UI asks NavVM to navigate
button.onClick { navigationViewModel.showRestaurantDetail(id) }

// ❌ WRONG: UI asks SharedVM to navigate (deprecated)
button.onClick { viewModel.navigateToRestaurantDetail(id) }
```

**Rationale:**

1. **Clear Intent**: The UI explicitly declares "I need to navigate"
   - Navigation is not a side effect of business logic
   - Navigation is a user intent, not data transformation

2. **Flexibility**: Business logic can emit navigation independently
   - SharedVM can trigger navigation as a side effect (see below)
   - No coupling between business logic and UI gestures

3. **Platform Alignment**: 
   - iOS: Native coordinator pattern (UIViewController.navigationController)
   - Android: Modern Compose navigation
   - Both naturally separate navigation from state

### **Decision 3: When SharedVM Initiates Navigation**

**Scenario**: After saving a form, app should navigate to success screen.

**Correct Implementation:**

```kotlin
class FormViewModel(
    private val repository: FormRepository,
    private val navigationViewModel: FormNavigationViewModel
) : KmpViewModel() {
    
    fun submitForm(data: FormData) {
        scope.launch {
            try {
                val result = repository.save(data)
                if (result.success) {
                    navigationViewModel.showSuccess()  // ✅ SharedVM triggers navigation
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
}
```

**Why This Works:**

1. **Business Logic Drives Navigation**: Navigation is a consequence of successful data processing
2. **No UI Coupling**: FormView doesn't need to know about success navigation
3. **Testable**: Test that `submitForm()` calls `showSuccess()` when `repository.save()` succeeds
4. **Reusable**: Same success behavior regardless of which button triggered submission

**Anti-Pattern:**

```kotlin
// ❌ WRONG: UI must orchestrate navigation
button.onClick {
    if (viewModel.submitForm()) {  // Returns success boolean
        navigationViewModel.showSuccess()  // UI decides when to navigate
    }
}
```

This couples the UI gesture to navigation logic.

---

## When Each Party Should Initiate Navigation

### **UI Should Initiate Navigation When:**

- ✅ User gesture directly maps to navigation
  - Tapping a list item → show detail
  - Tapping a back button → go back
  - Tapping "Filters" button → show filter modal

### **SharedVM Should Initiate Navigation When:**

- ✅ Navigation is a consequence of business logic
  - Form validation fails → dismiss keyboard, shake field
  - Form submission succeeds → navigate to success screen
  - API call completes → auto-navigate to detail
  - User gets logged out → navigate to login

### **Key Principle:**

> **UI handles direct user gestures. Business logic handles state-driven navigation.**

---

## Directory Structure

```
feature-restaurant/
├── src/commonMain/kotlin/io/umain/munchies/feature/restaurant/
│   ├── navigation/
│   │   └── RestaurantNavigationViewModel.kt
│   │       ├── showRestaurantDetail(id)
│   │       ├── showRestaurantList()
│   │       ├── showFilterModal(filters)
│   │       ├── showSubmitReviewModal(id)
│   │       └── navigateBack()
│   │
│   ├── presentation/
│   │   ├── RestaurantListViewModel.kt
│   │   │   ├── load()
│   │   │   ├── toggleFilter(id)
│   │   │   └── applyFilters()
│   │   │
│   │   └── RestaurantDetailViewModel.kt
│   │       ├── load()
│   │       └── (navigation is NavVM's job)
│   │
│   └── di/
│       ├── FeatureRestaurantModule.kt
│       ├── ViewModelGetters.kt (iOS interop)
│       └── RestaurantListScope.kt
│
├── iosApp/iosApp/Features/Restuarants/
│   ├── RestaurantList/RestaurantListView.swift
│   │   ├── navigationViewModel: RestaurantNavigationViewModel
│   │   └── viewModel: RestaurantListViewModel
│   │
│   └── RestaurantDetail/RestaurantDetailView.swift
│       ├── navigationViewModel: RestaurantNavigationViewModel
│       └── viewModel: RestaurantDetailViewModel
│
└── androidApp/src/main/kotlin/io/umain/munchies/android/features/restaurant/
    ├── RestaurantList/RestaurantListScreen.kt
    │   ├── navigationViewModel: RestaurantNavigationViewModel (param)
    │   └── viewModel: RestaurantListViewModel (from scope)
    │
    └── RestaurantDetail/RestaurantDetailScreen.kt
        ├── navigationViewModel: RestaurantNavigationViewModel (param)
        └── viewModel: RestaurantDetailViewModel (from scope)
```

---

## Implementation Checklist

### For New Features:

- [ ] Create `FeatureNavigationViewModel` in `feature-{name}/src/commonMain/kotlin/.../navigation/`
  - List all navigation actions as methods
  - Inject `NavigationDispatcher`
  - Make it a `single` in DI (shared across all screens in feature)

- [ ] Create `FeatureViewModel` in `feature-{name}/src/commonMain/kotlin/.../presentation/`
  - Focus on state and business logic
  - Inject dependencies (repo, etc.)
  - Can inject `FeatureNavigationViewModel` if business logic drives navigation
  - NO wrapper navigation methods

- [ ] Android: Create RouteHandlerAndroid
  - `buildComposable()` method: Get NavVM from scope, pass to Screen
  - Screen receives NavVM as parameter, calls it directly

- [ ] iOS: Create ViewModelHolder
  - Provides both navigationViewModel and viewModel
  - Views receive both

- [ ] Views/Composables:
  - Receive navigationViewModel as parameter
  - Call it directly for user gestures
  - Example: `button.onClick { navigationViewModel.showDetail(id) }`

---

## Testing Examples

### **Testing Navigation Trigger (UI → NavVM)**

```kotlin
// Android
@Test
fun `tapping restaurant card navigates to detail`() {
    val mockDispatcher = mockk<NavigationDispatcher>()
    val navVM = RestaurantNavigationViewModel(mockDispatcher)
    
    navVM.showRestaurantDetail("123")
    
    verify { mockDispatcher.navigate(Destination.RestaurantDetail("123")) }
}
```

### **Testing Business-Driven Navigation (SharedVM → NavVM)**

```kotlin
// Kotlin Multiplatform
@Test
fun `successful form submission navigates to success`() {
    val mockRepository = mockk<FormRepository>()
    val mockNavVM = mockk<FormNavigationViewModel>()
    val viewModel = FormViewModel(mockRepository, mockNavVM)
    
    coEvery { mockRepository.save(any()) } returns FormResult.Success
    
    viewModel.submitForm(testData)
    
    verify { mockNavVM.showSuccess() }
}
```

---

## Common Pitfalls

### ❌ Pitfall 1: Wrapper Methods in SharedVM

```kotlin
class RestaurantListViewModel(
    private val navigationViewModel: RestaurantNavigationViewModel
) {
    fun navigateToDetail(id: String) {
        navigationViewModel.showRestaurantDetail(id)  // ❌ Boilerplate
    }
}
```

**Why it's bad:**
- Adds indirection without value
- Hides the actual navigator (NavVM)
- Creates dead code on iOS if UI doesn't use it
- Makes refactoring harder (change in 2 places)

**Solution:** Remove wrapper, call NavVM directly from UI

### ❌ Pitfall 2: Navigation Dispatcher in UI

```swift
// ❌ WRONG
struct RestaurantListView: View {
    let dispatcher: NavigationDispatcher  // Too low-level
    
    button.onClick {
        dispatcher.navigate(Destination.RestaurantDetail("123"))
    }
}
```

**Why it's bad:**
- Exposes implementation detail (Dispatcher)
- Loses semantic meaning ("navigate to detail" is clearer than "emit Push event")
- Hard to test (mock Dispatcher instead of NavVM)

**Solution:** Use NavVM as intermediary

### ❌ Pitfall 3: Storing Navigation State in SharedVM

```kotlin
class RestaurantListViewModel {
    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId = _selectedId.asStateFlow()
    
    fun selectRestaurant(id: String) {
        _selectedId.value = id  // ❌ This is UI state, not business state
        navigationViewModel.showDetail(id)
    }
}
```

**Why it's bad:**
- Couples UI state with business state
- Makes it hard to navigate programmatically (without selecting)
- Violates single responsibility

**Solution:** Navigation state lives in NavVM, UI state lives in SharedVM

---

## Summary: The Clean Navigation Contract

```
╔════════════════════════════════════════════════════════════════╗
║                    NAVIGATION CONTRACT                         ║
╠════════════════════════════════════════════════════════════════╣
║                                                                ║
║ 1. UI explicitly calls navigationViewModel.action()            ║
║    Example: button.onClick { navVM.showDetail(id) }            ║
║                                                                ║
║ 2. navigationViewModel only declares intents, doesn't execute  ║
║    It emits events to NavigationDispatcher                     ║
║                                                                ║
║ 3. SharedViewModel focuses on state + business logic           ║
║    It can trigger navigation as a side effect of business      ║
║    but never wrapper navigation for UI convenience             ║
║                                                                ║
║ 4. Platform-specific handlers (iOS Coordinator, Android Nav)   ║
║    React to navigation events and update UI accordingly        ║
║                                                                ║
╚════════════════════════════════════════════════════════════════╝
```

---

## References

- **Android Architecture**: Modern Android Compose Navigation
- **iOS Architecture**: Apple's Navigation Controller Pattern / NavigationStack
- **Pattern**: MVI (Model-View-Intent) with Coordinator overlay
- **Principle**: Separation of Concerns + Platform Independence
