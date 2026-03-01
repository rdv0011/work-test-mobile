# Phase 4: Extending the Pattern to Other Features

## Overview

This guide documents how to extend the **SharedViewModel-initiated navigation pattern** (Pattern 2) to other features in the codebase.

**Pattern Rule (Quick Reference):**
```
IF (navigation is direct user gesture)
  THEN UI calls navigationViewModel
ELSE IF (navigation is automatic business consequence)
  THEN SharedViewModel calls navigationViewModel ← USE THIS PATTERN
ELSE
  Don't navigate automatically
```

---

## Part 1: Identifying Candidates for Extension

### Question 1: Does the Feature Have Business Logic Consequences?

**Yes if:**
- ✅ Feature has async operations (API calls, database operations)
- ✅ Operations have success/error outcomes
- ✅ Outcomes should trigger navigation automatically
- ✅ Navigation is NOT a direct user gesture

**Examples:**
- ✅ Review submission (success → show modal) - **IMPLEMENTED**
- ✅ Save preferences (success → show confirmation)
- ✅ Delete account (success → logout and navigate to login)
- ✅ Upload profile picture (success → show updated profile)
- ❌ Toggle dark mode (direct user action, not a consequence)
- ❌ Navigate to search results (direct user gesture, handle in UI)

### Question 2: Is There a Feature-Scoped Navigation ViewModel?

**Required Architecture:**
```
FeatureNavigationViewModel (Single) ← Centralized navigation dispatcher
        ↑
        |
FeatureDetailViewModel (Scoped) → Injects FeatureNavigationViewModel
        ↑
        |
    Repository → FeatureNavigationViewModel emits events
```

**Check:** Does the feature have:
- [ ] `{Feature}NavigationViewModel` in `navigation/` directory
- [ ] `{Feature}ViewModel` that can be scoped
- [ ] Repository layer for business logic
- [ ] DI setup for scoped ViewModels

If NO → You'll need to create these first (similar to Restaurant feature)

---

## Part 2: Implementation Checklist

### Step 1: Define Navigation Events

**File:** `{feature}/navigation/{Feature}ModalEvents.kt`

```kotlin
// Example: If implementing for Settings feature
package io.umain.munchies.feature.settings.navigation

sealed class SettingsModalEvent {
    data object PreferencesUpdatedModal : SettingsModalEvent()
    data class ErrorAlert(val message: String) : SettingsModalEvent()
}
```

**Or add to existing NavigationEvent:**

```kotlin
// In: core/navigation/NavigationEvent.kt
sealed class ModalRoute {
    // ... existing modals
    data object PreferencesUpdatedModal : ModalRoute()
    data class PreferencesErrorAlert(val message: String) : ModalRoute()
}
```

### Step 2: Add Navigation Methods to Feature Navigation ViewModel

**File:** `{feature}/navigation/{Feature}NavigationViewModel.kt`

```kotlin
class SettingsNavigationViewModel(
    private val navigationDispatcher: NavigationDispatcher
) {
    fun showPreferencesUpdatedModal() {
        navigationDispatcher.navigate(ModalRoute.PreferencesUpdatedModal)
    }

    fun showErrorAlert(message: String) {
        navigationDispatcher.navigate(ModalRoute.PreferencesErrorAlert(message))
    }
}
```

### Step 3: Add Business Logic Method to Feature ViewModel

**File:** `{feature}/presentation/{Feature}ViewModel.kt`

**Pattern:**
```kotlin
class SettingsViewModel(
    private val repository: SettingsRepository,
    private val navigationViewModel: SettingsNavigationViewModel
) : KmpViewModel() {
    
    fun savePreferences(preferences: UserPreferences) {
        scope.launch {
            try {
                // Call repository
                repository.savePreferences(preferences)
                
                // Success → trigger navigation
                navigationViewModel.showPreferencesUpdatedModal()
            } catch (e: Exception) {
                // Error → trigger error navigation
                navigationViewModel.showErrorAlert(e.message ?: "Unknown error")
            }
        }
    }
}
```

**Key Points:**
- Method name describes the action: `savePreferences()`, `updateProfile()`, etc.
- Business logic stays in repository
- ViewModel orchestrates: call repository → navigate on result
- Both success and error paths trigger navigation

### Step 4: Update Dependency Injection

**File:** `{feature}/di/FeatureModule.kt`

```kotlin
// Old: FeatureViewModel gets only repository
scoped { (featureId: String) ->
    FeatureViewModel(repository = get())
}

// New: FeatureViewModel gets repository AND navigationViewModel
scoped { (featureId: String) ->
    FeatureViewModel(
        repository = get(),
        navigationViewModel = get()  // Single-scoped ViewModel
    )
}
```

**Or in Settings (if scoped differently):**

```kotlin
single {
    SettingsViewModel(
        repository = get(),
        navigationViewModel = get()  // Gets SettingsNavigationViewModel
    )
}
```

### Step 5: Integrate with Platform UI

#### Android: Composable Integration

**File:** `androidApp/features/{feature}/SettingsScreen.kt`

```kotlin
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    navigator: AppNavigator
) {
    val state by viewModel.stateFlow.collectAsState()
    
    Column {
        // Settings UI
        Button(onClick = {
            viewModel.savePreferences(
                UserPreferences(
                    darkModeEnabled = state.darkModeEnabled,
                    notificationsEnabled = state.notificationsEnabled
                )
            )
        }) {
            Text("Save Preferences")
        }
    }
}
```

**No changes needed in SettingsScreen** - it just calls `viewModel.savePreferences()`. Navigation events are handled automatically by the existing modal system.

#### iOS: SwiftUI Integration

**File:** `iosApp/iosApp/Features/{Feature}/{Feature}View.swift`

```swift
struct SettingsView: View {
    @StateObject var viewModel: SettingsViewModel
    
    var body: some View {
        VStack {
            // Settings UI
            Button("Save Preferences") {
                viewModel.savePreferences(
                    darkModeEnabled: viewModel.darkModeEnabled,
                    notificationsEnabled: viewModel.notificationsEnabled
                )
            }
        }
    }
}
```

**Same as Android:** No navigation code in UI. ViewModel triggers navigation automatically.

### Step 6: Add Tests

**File:** `{feature}/src/commonTest/kotlin/{Feature}ViewModelTest.kt`

```kotlin
class SettingsViewModelTest {
    @Test
    fun savePreferences_onSuccess_showsConfirmationModal() {
        // Arrange
        val mockRepository = mockk<SettingsRepository>()
        val mockNavViewModel = mockk<SettingsNavigationViewModel>()
        val viewModel = SettingsViewModel(mockRepository, mockNavViewModel)
        
        coEvery { mockRepository.savePreferences(any()) } returns Unit
        
        // Act
        viewModel.savePreferences(testPreferences)
        advanceUntilIdle()
        
        // Assert
        coVerify { mockNavViewModel.showPreferencesUpdatedModal() }
    }
    
    @Test
    fun savePreferences_onFailure_showsErrorAlert() {
        // Arrange
        val error = Exception("Network error")
        val mockRepository = mockk<SettingsRepository>()
        val mockNavViewModel = mockk<SettingsNavigationViewModel>()
        val viewModel = SettingsViewModel(mockRepository, mockNavViewModel)
        
        coEvery { mockRepository.savePreferences(any()) } throws error
        
        // Act
        viewModel.savePreferences(testPreferences)
        advanceUntilIdle()
        
        // Assert
        coVerify { mockNavViewModel.showErrorAlert("Network error") }
    }
}
```

---

## Part 3: Feature-Specific Guides

### A) Settings Feature: Preferences Update

**Candidate Action:** `savePreferences()`

**Current State:**
```kotlin
fun toggleDarkMode() {
    _stateFlow.value = _stateFlow.value.copy(
        darkModeEnabled = !_stateFlow.value.darkModeEnabled
    )
}
```

**Problem:** This is a local state change, not a consequence. No need to navigate.

**Alternative:** If settings are saved to server:

```kotlin
fun savePreferences() {
    scope.launch {
        try {
            repository.saveUserPreferences(state.value.toUserPreferences())
            navigationViewModel.showSavedConfirmation()  // Navigate on success
        } catch (e: Exception) {
            navigationViewModel.showErrorAlert(e.message)  // Navigate on error
        }
    }
}
```

**Where it's triggered:**
- User taps "Save" button in settings
- ViewModel calls `savePreferences()`
- On success → Confirmation modal appears
- On error → Error alert appears

### B) Restaurant Feature: Additional Actions

**Existing:** Review submission (already implemented)

**Candidates for Extension:**
1. **Add to Favorites**
   - ViewModel method: `addToFavorites(restaurantId: String)`
   - Success: Show "Added to favorites" toast/modal
   - Error: "Failed to add to favorites"

2. **Share Restaurant**
   - ViewModel method: `generateShareLink(restaurantId: String)`
   - Success: Show generated link in modal
   - Error: "Failed to generate share link"

3. **Make Reservation**
   - ViewModel method: `makeReservation(restaurantId: String, dateTime: LocalDateTime)`
   - Success: Show confirmation modal with booking details
   - Error: "Reservation failed - {error}"

---

## Part 4: Step-by-Step Extension Example

### Scenario: Add "Add to Favorites" to Restaurant Feature

**Step 1: Add Navigation Event**

```kotlin
// In: core/navigation/NavigationEvent.kt
sealed class ModalRoute {
    // ... existing
    data object FavoritesAddedModal : ModalRoute()
    data class FavoritesErrorAlert(val message: String) : ModalRoute()
}
```

**Step 2: Add Navigation Method**

```kotlin
// In: RestaurantNavigationViewModel.kt
fun showFavoritesAddedModal() {
    navigationDispatcher.navigate(ModalRoute.FavoritesAddedModal)
}

fun showFavoritesErrorAlert(message: String) {
    navigationDispatcher.navigate(ModalRoute.FavoritesErrorAlert(message))
}
```

**Step 3: Add ViewModel Method**

```kotlin
// In: RestaurantDetailViewModel.kt
fun addToFavorites(restaurantId: String) {
    scope.launch {
        try {
            repository.addToFavorites(restaurantId)
            navigationViewModel.showFavoritesAddedModal()
        } catch (e: Exception) {
            navigationViewModel.showFavoritesErrorAlert(
                e.message ?: "Failed to add to favorites"
            )
        }
    }
}
```

**Step 4: Add Repository Method**

```kotlin
// In: RestaurantRepository.kt (interface)
suspend fun addToFavorites(restaurantId: String): Boolean

// In: RestaurantRepositoryImpl.kt (implementation)
override suspend fun addToFavorites(restaurantId: String): Boolean {
    return api.addToFavorites(restaurantId).isSuccess
}
```

**Step 5: Call from UI**

```kotlin
// Android
Button(onClick = {
    viewModel.addToFavorites(restaurantId)
}) {
    Text("♥ Add to Favorites")
}

// iOS
Button(action: {
    viewModel.addToFavorites(restaurantId: restaurantId)
}) {
    Text("♥ Add to Favorites")
}
```

**Step 6: Add Tests**

```kotlin
@Test
fun addToFavorites_onSuccess_showsFavoritesAddedModal() {
    // Arrange
    val mockRepository = mockk<RestaurantRepository>()
    val mockNavViewModel = mockk<RestaurantNavigationViewModel>()
    val viewModel = RestaurantDetailViewModel("123", mockRepository, mockNavViewModel)
    
    coEvery { mockRepository.addToFavorites("123") } returns true
    
    // Act
    viewModel.addToFavorites("123")
    advanceUntilIdle()
    
    // Assert
    coVerify { mockNavViewModel.showFavoritesAddedModal() }
}
```

---

## Part 5: Common Patterns & Anti-Patterns

### ✅ GOOD: Clear Consequence Navigation

```kotlin
fun submitReview(rating: Int, comment: String) {
    scope.launch {
        try {
            repository.submitReview(restaurantId, rating, comment)
            // Success is a CONSEQUENCE, not a gesture
            navigationViewModel.showReviewSuccessModal()
        } catch (e: Exception) {
            navigationViewModel.showReviewErrorAlert(e.message)
        }
    }
}
```

**Why it's good:**
- Clear intent: success/error are business consequences
- User didn't gesture to show modal
- Modal appears automatically as feedback

### ✅ GOOD: Error Handling with Message

```kotlin
fun updateProfile(name: String) {
    scope.launch {
        try {
            repository.updateProfile(name)
            navigationViewModel.showProfileUpdatedModal()
        } catch (e: NetworkException) {
            navigationViewModel.showErrorAlert("Network unavailable")
        } catch (e: ValidationException) {
            navigationViewModel.showErrorAlert("Invalid name")
        } catch (e: Exception) {
            navigationViewModel.showErrorAlert("Update failed: ${e.message}")
        }
    }
}
```

**Why it's good:**
- Different errors handled differently
- User-friendly error messages
- Navigation always called (success or specific error)

### ❌ BAD: Navigating User Gestures

```kotlin
// DON'T DO THIS
fun onDeleteButtonClicked() {
    // User just tapped a button - this IS a gesture
    navigationViewModel.showDeleteConfirmationModal()
}
```

**Why it's bad:**
- User already gestured (tapped button)
- Should handle in UI, not ViewModel
- ViewModel shouldn't react to UI gestures

**Fix:** Move to UI
```kotlin
// Android
Button(onClick = {
    showDeleteConfirmationDialog = true  // UI state, not navigation
})

// iOS
Button(action: {
    showDeleteConfirmation = true  // UI state
})
```

### ❌ BAD: Over-Navigation

```kotlin
// DON'T DO THIS
fun toggleDarkMode() {
    scope.launch {
        // Toggle is local - doesn't need async or navigation
        _stateFlow.value = _stateFlow.value.copy(
            darkModeEnabled = !_stateFlow.value.darkModeEnabled
        )
        // This adds noise to navigation
        navigationViewModel.showDarkModeToggledModal()
    }
}
```

**Why it's bad:**
- Local state changes don't need navigation
- Creates excessive modals for simple toggles
- Interrupts user experience

### ✅ GOOD: Selective Navigation

```kotlin
fun updateDarkMode(enabled: Boolean) {
    // Local change - no navigation
    _stateFlow.value = _stateFlow.value.copy(
        darkModeEnabled = enabled
    )
    
    // But if persisting to server:
    scope.launch {
        try {
            repository.saveDarkModeSetting(enabled)
            // Navigation only for consequences of server operation
            navigationViewModel.showPreferencesSavedModal()
        } catch (e: Exception) {
            navigationViewModel.showErrorAlert("Failed to save preference")
        }
    }
}
```

---

## Part 6: Testing Strategy

### Unit Tests (Required)

For each business consequence method, write:
1. **Success path test** - Verifies navigation method called on success
2. **Error path test** - Verifies error navigation method called on failure
3. **Exception path test** - Verifies exception handling

### Example Test Suite

```kotlin
class MyFeatureViewModelTest {
    
    @Test
    fun businessAction_onSuccess_navigates() {
        // Template for success
    }
    
    @Test
    fun businessAction_onError_showsAlert() {
        // Template for error
    }
    
    @Test
    fun businessAction_onException_handlesGracefully() {
        // Template for unexpected errors
    }
}
```

### Integration Tests (Recommended)

```kotlin
// Test actual modal appearance
@AndroidTest
fun submitReview_showsSuccessModal() {
    // 1. Open review modal
    // 2. Fill form and submit
    // 3. Verify success modal appears
    // 4. Dismiss and verify state
}
```

---

## Part 7: Checklist for New Feature Implementation

Use this checklist when extending the pattern:

- [ ] **Architecture**
  - [ ] Feature has scoped ViewModel (`{Feature}DetailViewModel`, `{Feature}ListViewModel`)
  - [ ] Feature has navigation ViewModel (`{Feature}NavigationViewModel`)
  - [ ] Feature has repository layer

- [ ] **Navigation Events**
  - [ ] Added success modal to `NavigationEvent.kt`
  - [ ] Added error alert to `NavigationEvent.kt`
  - [ ] Events are data classes with appropriate fields

- [ ] **Navigation ViewModel**
  - [ ] Added `show{Action}SuccessModal()` method
  - [ ] Added `show{Action}ErrorAlert(message)` method
  - [ ] Methods call `navigationDispatcher.navigate()`

- [ ] **Feature ViewModel**
  - [ ] Added `{action}()` business logic method
  - [ ] Method is scoped (uses `scope.launch`)
  - [ ] Calls repository for business logic
  - [ ] On success: calls `navigationViewModel.show{Success}()`
  - [ ] On error: calls `navigationViewModel.show{Error}()`

- [ ] **Repository Layer**
  - [ ] Added interface method in `{Feature}Repository.kt`
  - [ ] Implemented in `{Feature}RepositoryImpl.kt`
  - [ ] Added API call in `Ktor{Feature}Api.kt` (if server-dependent)

- [ ] **Dependency Injection**
  - [ ] Updated DI to inject `navigationViewModel` into feature ViewModel
  - [ ] Verified scope hierarchy (Single → Scoped is valid)
  - [ ] No circular dependencies

- [ ] **Testing**
  - [ ] Success path test
  - [ ] Error path test
  - [ ] Exception handling test
  - [ ] All tests pass

- [ ] **Platform UI** (No changes needed usually, but verify)
  - [ ] Android UI calls `viewModel.{action}()`
  - [ ] iOS UI calls `viewModel.{action}()`
  - [ ] No hardcoded navigation logic in UI

- [ ] **Build Verification**
  - [ ] All platforms compile
  - [ ] Zero type errors
  - [ ] No `@Suppress` annotations
  - [ ] No `as any` type casts

- [ ] **Verification**
  - [ ] Manual test on Android device/emulator
  - [ ] Manual test on iOS simulator/device
  - [ ] Test success path
  - [ ] Test error path
  - [ ] Test modal dismiss and re-open

---

## Part 8: Implementation Roadmap

### Priority 1: Validate Pattern (Current)
- ✅ Review submission in Restaurant detail
- ✅ Android UI integration
- ✅ iOS UI integration
- ⏳ Manual testing (see TESTING_GUIDE.md)

### Priority 2: Extend to Similar Features
- [ ] Add to Favorites (Restaurant)
- [ ] Save Settings (Settings)
- [ ] Generate Share Link (Restaurant)

### Priority 3: Extend to Complex Features
- [ ] Make Reservation (Restaurant)
- [ ] Delete Account (Settings)
- [ ] Logout (Settings)

### Priority 4: Refine & Document
- [ ] Create PR checklist based on learnings
- [ ] Document patterns in README
- [ ] Create quick-start guide for developers

---

## Part 9: Troubleshooting

### Issue: ViewModel Not Injected

**Symptom:** NullPointerException when calling navigation methods

**Solution:**
1. Verify DI configuration has `navigationViewModel: get()`
2. Check that `{Feature}NavigationViewModel` is registered as `single`
3. Ensure `{Feature}ViewModel` is registered in correct scope

### Issue: Navigation Not Triggering

**Symptom:** Modal doesn't appear after business logic completes

**Solution:**
1. Add logging in `navigationViewModel.show{Action}()` method
2. Check that `navigationDispatcher.navigate()` is called
3. Verify modal route is registered in platform UI
4. Check AppCoordinator is processing navigation events

### Issue: Scope Mismatch

**Symptom:** Cannot inject Single dependency into Scoped ViewModel

**Solution:**
Koin hierarchy is valid:
- ✅ Single → Single
- ✅ Single → Scoped (Scoped can depend on Single)
- ✅ Scoped → Scoped (same scope)
- ❌ Scoped → Single (if you try to inject Scoped into Single)

If invalid, verify DI registration matches the intended hierarchy.

---

## Quick Reference: Copy-Paste Template

Use this template for new business consequence actions:

### Step 1: Navigation Event
```kotlin
// In: core/navigation/NavigationEvent.kt
data object {Action}SuccessModal : ModalRoute()
data class {Action}ErrorAlert(val message: String) : ModalRoute()
```

### Step 2: Navigation ViewModel Method
```kotlin
fun show{Action}SuccessModal() {
    navigationDispatcher.navigate(ModalRoute.{Action}SuccessModal)
}

fun show{Action}ErrorAlert(message: String) {
    navigationDispatcher.navigate(ModalRoute.{Action}ErrorAlert(message))
}
```

### Step 3: Feature ViewModel Method
```kotlin
fun {action}({params}) {
    scope.launch {
        try {
            repository.{action}({params})
            navigationViewModel.show{Action}SuccessModal()
        } catch (e: Exception) {
            navigationViewModel.show{Action}ErrorAlert(
                e.message ?: "Operation failed"
            )
        }
    }
}
```

### Step 4: Repository Method
```kotlin
// Interface
suspend fun {action}({params}): {ReturnType}

// Implementation
override suspend fun {action}({params}): {ReturnType} {
    return api.{action}({params})
}
```

### Step 5: Test
```kotlin
@Test
fun {action}_onSuccess_navigates() {
    // Arrange
    val mockRepository = mockk<{Feature}Repository>()
    val mockNavViewModel = mockk<{Feature}NavigationViewModel>()
    val viewModel = {Feature}ViewModel(mockRepository, mockNavViewModel)
    
    coEvery { mockRepository.{action}(any()) } returns testResult
    
    // Act
    viewModel.{action}(testParams)
    advanceUntilIdle()
    
    // Assert
    coVerify { mockNavViewModel.show{Action}SuccessModal() }
}
```

---

## Next Steps

1. **Review this guide** with team
2. **Identify** 2-3 candidate actions in existing features
3. **Implement** using the checklist
4. **Test** using TESTING_GUIDE.md procedures
5. **Document** learnings in team wiki
6. **Update** this guide based on real-world experience

---

## Document History

- **Created:** 2024-03-01
- **Phase:** 4 (Extending Pattern)
- **Status:** In Progress
- **Last Updated:** 2024-03-01
