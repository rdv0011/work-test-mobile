# Quick Reference: SharedViewModel Navigation Pattern

## One-Page Guide for Developers

### When to Use This Pattern

✅ **Use this pattern when:**
- Feature has async business logic (API calls, database operations)
- Operation has success/error outcomes
- Outcomes should trigger navigation **automatically**
- Navigation is NOT a direct user gesture

❌ **DON'T use when:**
- Navigation is a direct user gesture (button tap → screen change)
- Local state changes (toggles, form validation)
- Manual navigation needed (user explicitly asked to navigate)

---

## 6-Step Implementation Template

### Step 1: Add Navigation Events

```kotlin
// core/navigation/NavigationEvent.kt
sealed class ModalRoute {
    data object {Action}SuccessModal : ModalRoute()
    data class {Action}ErrorAlert(val message: String) : ModalRoute()
}
```

### Step 2: Add Navigation Methods to Navigation ViewModel

```kotlin
// feature-{name}/navigation/{Name}NavigationViewModel.kt
class {Name}NavigationViewModel(private val dispatcher: NavigationDispatcher) {
    fun show{Action}SuccessModal() {
        dispatcher.navigate(ModalRoute.{Action}SuccessModal)
    }
    
    fun show{Action}ErrorAlert(message: String) {
        dispatcher.navigate(ModalRoute.{Action}ErrorAlert(message))
    }
}
```

### Step 3: Add Business Logic Method to Feature ViewModel

```kotlin
// feature-{name}/presentation/{Name}ViewModel.kt
class {Name}ViewModel(
    private val repository: {Name}Repository,
    private val navigationViewModel: {Name}NavigationViewModel
) {
    fun {action}(params: String) {
        scope.launch {
            try {
                repository.{action}(params)
                navigationViewModel.show{Action}SuccessModal()
            } catch (e: Exception) {
                navigationViewModel.show{Action}ErrorAlert(
                    e.message ?: "Operation failed"
                )
            }
        }
    }
}
```

### Step 4: Update Dependency Injection

```kotlin
// feature-{name}/di/{Name}Module.kt
scoped { (params: String) ->
    {Name}ViewModel(
        repository = get(),
        navigationViewModel = get()  // Inject from Single scope
    )
}
```

### Step 5: Call from UI (No Changes Needed)

```kotlin
// Android
Button(onClick = { viewModel.{action}(param) }) { Text("Action") }

// iOS
Button(action: { viewModel.{action}(param: param) }) { Text("Action") }
```

### Step 6: Write Tests

```kotlin
@Test
fun {action}_onSuccess_navigates() {
    val mockRepository = mockk<{Name}Repository>()
    val mockNavViewModel = mockk<{Name}NavigationViewModel>()
    val viewModel = {Name}ViewModel(mockRepository, mockNavViewModel)
    
    coEvery { mockRepository.{action}(any()) } returns Unit
    
    viewModel.{action}("test")
    advanceUntilIdle()
    
    coVerify { mockNavViewModel.show{Action}SuccessModal() }
}
```

---

## Data Flow (Visual)

```
User Action → ViewModel Business Logic → Repository API Call
                                              ↓
                                         Success/Error
                                              ↓
                                    Navigation ViewModel Method
                                              ↓
                                      Navigation Dispatcher
                                              ↓
                                        Platform UI Updates
```

---

## Dependency Injection Scoping

**Valid Scope Hierarchies:**
- ✅ Single → Single
- ✅ Single → Scoped (Scoped can depend on Single)
- ✅ Scoped → Scoped
- ❌ Scoped → Single (don't do this)

**Our Setup:**
```
RestaurantNavigationViewModel (Single)
        ↓ (injected into)
RestaurantDetailViewModel (Scoped)
        ↓ (calls)
navigationViewModel.showReviewSuccessModal()
```

---

## Common Mistakes & Fixes

### ❌ Mistake 1: Navigating User Gestures
```kotlin
// WRONG
fun onDeleteButtonClicked() {
    navigationViewModel.showDeleteModal()  // User already clicked!
}

// RIGHT
// Keep in UI as state, not navigation
@State var showDeleteConfirm = false
```

### ❌ Mistake 2: Over-Navigation
```kotlin
// WRONG
fun toggleDarkMode() {
    navigationViewModel.showDarkModeChanged()  // Too much!
}

// RIGHT
// Just update state locally, no navigation
_stateFlow.value = state.copy(darkMode = !state.darkMode)
```

### ❌ Mistake 3: Missing Try-Catch
```kotlin
// WRONG
fun submitReview() {
    repository.submitReview()  // What if it fails?
    navigationViewModel.show...()
}

// RIGHT
fun submitReview() {
    scope.launch {
        try {
            repository.submitReview()
            navigationViewModel.showSuccess()
        } catch (e: Exception) {
            navigationViewModel.showError(e.message)
        }
    }
}
```

---

## Testing Quick Checklist

For each business logic method:

- [ ] **Success Test:** Repository succeeds → navigation method called
- [ ] **Error Test:** Repository throws → error alert triggered
- [ ] **Exception Test:** Unexpected error → graceful handling
- [ ] **Integration Test:** Manual test on device (success flow)

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| **Modal doesn't appear** | Check modal is in navigation state; verify router handles event |
| **ViewModel not injected** | Check DI config has `navigationViewModel: get()` |
| **Navigation not triggered** | Add logging in `navigationViewModel.show{Action}()` |
| **Build fails** | Check imports; verify `{Name}NavigationViewModel` exists |
| **Tests fail** | Verify `advanceUntilIdle()` called after `launch` |

---

## Real-World Example: Review Submission

**What User Does:**
1. Fills review form (rating + comment)
2. Taps "Submit Review" button

**What Happens Behind Scenes:**
1. UI calls `viewModel.submitReview(rating, comment)`
2. ViewModel calls `repository.submitReview()`
3. Repository makes API call
4. On success: `navigationViewModel.showReviewSuccessModal()`
5. On error: `navigationViewModel.showReviewErrorAlert(error)`
6. Platform UI observes navigation event and shows modal

**No explicit navigation in UI code.** Pattern handles it automatically.

---

## Files to Check/Modify

For new implementation:

```
feature-{name}/
├── domain/
│   └── repository/
│       └── {Name}Repository.kt (add method signature)
├── data/
│   ├── repository/
│   │   └── {Name}RepositoryImpl.kt (implement method)
│   └── remote/
│       └── Ktor{Name}Api.kt (add API call)
├── presentation/
│   └── {Name}ViewModel.kt (add business logic method)
├── navigation/
│   └── {Name}NavigationViewModel.kt (add nav methods)
├── di/
│   └── {Name}Module.kt (update DI config)
└── commonTest/
    └── {Name}ViewModelTest.kt (add tests)
```

---

## Documentation References

- **Full Testing Guide:** `TESTING_GUIDE.md` (350+ lines)
- **Extension Guide:** `PATTERN_EXTENSION_GUIDE.md` (450+ lines)
- **Session Summary:** `SESSION_SUMMARY.md` (comprehensive overview)

---

## Key Principles

1. **Single Responsibility:** Navigation ViewModel only handles navigation
2. **Fail Loudly:** Always catch and report errors
3. **Test Consequences:** Test navigation as much as business logic
4. **Explicit Over Implicit:** Name methods after actions
5. **User Feedback:** Always respond to user actions with visible feedback

---

## Copy-Paste Templates

### Kotlin Coroutine Template
```kotlin
scope.launch {
    try {
        // TODO: Call repository
        // TODO: On success, navigate
    } catch (e: Exception) {
        // TODO: On error, show alert
    }
}
```

### Navigation Method Template
```kotlin
fun show{Action}SuccessModal() {
    navigationDispatcher.navigate(ModalRoute.{Action}SuccessModal)
}
```

### Test Template
```kotlin
@Test
fun {action}_onSuccess_navigates() {
    // Arrange
    val mockRepository = mockk<{Name}Repository>()
    val mockNavViewModel = mockk<{Name}NavigationViewModel>()
    val viewModel = {Name}ViewModel(mockRepository, mockNavViewModel)
    
    // Act
    viewModel.{action}(param)
    advanceUntilIdle()
    
    // Assert
    coVerify { mockNavViewModel.show{Action}SuccessModal() }
}
```

---

## Questions?

1. **"How do I test this?"** → See TESTING_GUIDE.md
2. **"How do I extend to another feature?"** → See PATTERN_EXTENSION_GUIDE.md
3. **"When do I use this pattern?"** → See "When to Use" section above
4. **"How do I debug?"** → See TESTING_GUIDE.md Part 9

---

**Pattern Status:** ✅ PROVEN & READY FOR ADOPTION

Last Updated: 2024-03-01  
Examples: Review Submission Feature  
Test Coverage: Unit + Manual Testing  
Documentation: 1000+ lines across 3 guides
