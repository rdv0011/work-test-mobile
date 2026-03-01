# Phase 3: Testing & Validation Guide

## Overview
This document provides comprehensive manual testing procedures for the SharedViewModel-initiated navigation pattern implementation.

**Commits Being Tested:**
- `cdd21e9` - Phase 1: Architecture implementation
- `989cf70` - UI Integration: Android and iOS review modals

---

## Part 1: Environment Setup

### Android Setup

#### Prerequisites
- Android Studio installed
- Android SDK 31+ (API level 31 or higher)
- Connected Android device OR emulator running

#### Build Steps
```bash
# Build debug APK
./gradlew assembleDebug

# Or build and install directly (requires connected device/emulator)
./gradlew installDebug
```

#### Verification
- Open Android Studio
- Navigate to: View → Tool Windows → Logcat
- Filter by package: `io.umain.munchies`
- Watch for navigation events in logs

### iOS Setup

#### Prerequisites
- Xcode installed (version 14+)
- iOS 15+ deployment target
- iOS Simulator running (or physical device connected)

#### Build Steps
```bash
# Build for iOS Simulator
./gradlew :iosApp:build -Dorg.gradle.kotlin.native.cocoapods=true

# Or use Xcode
cd iosApp
xcodebuild -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 15'
```

#### Verification
- Open Xcode console
- Watch for Swift print statements
- Use Xcode debugger to set breakpoints in ModalDestinationView

---

## Part 2: Test Scenarios

### Scenario 1: Basic Review Submission Flow

**Objective:** Verify happy path of review submission with success modal

#### Steps

**Android:**
1. Launch the app from Android Studio or adb
2. Navigate to a restaurant detail page (may need to create test data)
3. Look for "Leave a Review" button
4. Tap the button to open review modal
5. Expected: Modal appears with:
   - ⭐⭐⭐⭐⭐ star display (all filled stars by default for 5 rating)
   - 5 numbered buttons (1, 2, 3, 4, 5) for rating selection
   - Text input field for comment (placeholder: "Add your comment")
   - "Cancel" button (enabled)
   - "Submit Review" button (initially disabled - grayed out)

6. Change rating to 3 stars by tapping button "3"
7. Expected: Star display updates to ⭐⭐⭐☆☆ (3 filled, 2 unfilled)

8. Type comment: "Great food and service!"
9. Expected: "Submit Review" button becomes enabled (darker color)

10. Tap "Submit Review"
11. Expected:
    - Button shows loading state (disabled)
    - Modal automatically dismisses after ~0.5s
    - Success modal appears with confirmation message

12. Dismiss success modal by tapping outside or OK button
13. Expected: Return to restaurant detail page

**iOS:**
1. Launch the app from Xcode simulator
2. Navigate to a restaurant detail page
3. Look for "Leave a Review" button/action
4. Tap the button to open review modal
5. Expected: Modal appears with:
   - Title: "Leave a Review"
   - Star display (default 5 stars, all filled)
   - 5 numbered buttons (1, 2, 3, 4, 5)
   - "Add your comment" label
   - TextEditor for comment input
   - "Cancel" button (enabled)
   - "Submit Review" button (initially disabled - grayed out)

6. Change rating to 4 stars
7. Expected: Star display updates to ⭐⭐⭐⭐☆ (4 filled, 1 unfilled)

8. Type comment: "Amazing experience!"
9. Expected: "Submit Review" button becomes enabled

10. Tap "Submit Review"
11. Expected:
    - Button shows loading state
    - Modal auto-dismisses after ~0.5s
    - Success modal appears

12. Dismiss by tapping outside or done
13. Expected: Return to restaurant detail

#### Expected Logs

**Android Logcat:**
```
I/RestaurantDetailViewModel: Submitting review for restaurant: {id}
I/RestaurantDetailViewModel: Review submitted successfully
I/RestaurantNavigationViewModel: Showing review success modal
I/AppCoordinator: Modal navigation event received: ReviewSuccessModal
```

**iOS Console:**
```
RestaurantDetailViewModel: Submitting review
RestaurantNavigationViewModel: showReviewSuccessModal()
ModalDestinationView: ReviewsModalView dismissing
```

---

### Scenario 2: Empty Comment Validation

**Objective:** Verify submit button remains disabled with empty/whitespace comment

#### Steps

**Both Platforms:**
1. Open review modal
2. Keep comment field empty (or only whitespace)
3. Expected: "Submit Review" button stays disabled

4. Type a space: " "
5. Expected: Button remains disabled (whitespace-only is treated as empty)

6. Type valid comment: "Great place"
7. Expected: Button becomes enabled

8. Delete all text
9. Expected: Button becomes disabled again

#### Verification

This tests the validation logic:
- Android: `comment.isNotBlank()`
- iOS: `!comment.trimmingCharacters(in: .whitespaces).isEmpty`

---

### Scenario 3: Network Failure Handling

**Objective:** Verify error modal appears when submission fails

#### Steps (Requires Mock/Stub Network)

**Android:**
1. Open review modal
2. Simulate network failure by:
   - Option A: Turn off internet/WiFi
   - Option B: Use Charles Proxy to block `/api/v1/restaurants/*/review` endpoint
   - Option C: Modify RestaurantRepositoryImpl to throw exception

3. Enter valid rating and comment
4. Tap "Submit Review"
5. Expected: Error modal appears with message:
   - "Failed to submit review. Please try again."
   - OR specific error message from API

6. Tap "OK" to dismiss
7. Expected: Return to restaurant detail (modal is still accessible, can retry)

**iOS:**
1. Same steps as Android
2. Expected: Error alert appears with error message
3. Tap "OK"
4. Expected: Return to detail view

#### Expected Logs

**Failure Flow:**
```
I/RestaurantDetailViewModel: Review submission failed: {error message}
I/RestaurantNavigationViewModel: Showing review error alert: {error message}
I/AppCoordinator: Modal navigation event received: ReviewErrorAlert
```

---

### Scenario 4: Double-Submission Prevention

**Objective:** Verify duplicate submissions are prevented

#### Steps

**Android:**
1. Open review modal
2. Enter rating and comment
3. Tap "Submit Review" quickly twice in succession
4. Expected:
   - Only one submission attempt (visible in logs)
   - Button disables after first tap
   - Modal dismisses only once
   - Success modal appears only once

**iOS:**
1. Same steps
2. Expected: `isSubmitting` flag prevents second tap from executing
3. Verify in Xcode debugger:
   - breakpoint on `submitReview()` hit only once
   - `viewModel.submitReview()` called exactly once

#### Code Verification

**Android:** Check that `isLoading` state prevents multiple calls
```kotlin
if (state.isLoading) return  // Guards against re-entry
```

**iOS:** Check that `isSubmitting` flag prevents multiple calls
```swift
if isSubmitting { return }
isSubmitting = true
```

---

### Scenario 5: Navigation Integration

**Objective:** Verify navigation events properly connect ViewModels and UI

#### Steps

**Android (Using Debugger):**
1. Set breakpoint in `RestaurantDetailViewModel.submitReview()`
2. Set breakpoint in `RestaurantNavigationViewModel.showReviewSuccessModal()`
3. Set breakpoint in `AppCoordinator.onNavigationEvent(ReviewSuccessModal)`
4. Open review modal and submit
5. Expected:
   - Breakpoint 1 hit with correct rating/comment
   - Breakpoint 2 hit immediately after
   - Breakpoint 3 hit to process navigation event
   - Modal transitions to success screen

**iOS (Using Xcode Debugger):**
1. Set breakpoint in `RestaurantDetailViewModel.submitReview()`
2. Set breakpoint in `ReviewsModalView.submitReview()`
3. Set breakpoint in `TabNavigationView.modalDestination`
4. Open review modal and submit
5. Expected:
   - Breakpoint 1 hit with Int32 rating and String comment
   - Breakpoint 2 hit to trigger submission
   - UI dismisses and updates

#### Dependency Injection Verification

**Check that the right ViewModels are injected:**

**Android:**
```kotlin
// In AppNavigation.kt
val scope = registry.createScopeForRoute(route)
val viewModel = scope.get<RestaurantDetailViewModel>()
// Verify: This should get the scoped ViewModel, not the navigation ViewModel
```

**iOS:**
```swift
// In TabNavigationView.swift
let holder = navigator.restaurantDetailHolder(restaurantId: submitReview.restaurantId)
let viewModel = holder.viewModel
// Verify: This should get the scoped ViewModel from holder
```

---

### Scenario 6: Modal Lifecycle

**Objective:** Verify modal properly opens and closes

#### Steps

**Android:**
1. Open review modal
2. Expected: Modal appears with correct content
3. Tap "Cancel"
4. Expected: Modal dismisses without submitting

5. Open modal again
6. Expected: Previous state is cleared (rating defaults to 5, comment is empty)

7. Enter data and submit
8. Expected: Modal dismisses and success modal appears

9. Dismiss success modal
10. Expected: Can open review modal again and cycle repeats

**iOS:**
1. Same as Android
2. Verify using Xcode debugger:
   - State variables reset each time modal opens
   - `rating` defaults to 5
   - `comment` defaults to ""
   - `isSubmitting` defaults to false

---

## Part 3: Regression Tests

### Existing Functionality

Verify that our changes didn't break anything:

**Restaurant Detail Page:**
- [ ] Page loads correctly
- [ ] Restaurant data displays
- [ ] Star rating/review count shows
- [ ] Other buttons work (share, call, directions, etc.)

**Navigation Between Features:**
- [ ] Can navigate from home → restaurant detail
- [ ] Can navigate from restaurant list → detail
- [ ] Back button works correctly
- [ ] Tab switching works

**Other Modals:**
- [ ] Success modals from other features still work
- [ ] Error alerts still display correctly
- [ ] Loading states work

---

## Part 4: Performance Testing

### Submission Performance

**Measure:** Time from submit click to success modal appearance

**Expected:** < 1000ms (1 second)

**Steps:**
1. Android: Use Android Profiler (View → Tool Windows → Profiler)
   - Record CPU activity during submission
   - Verify no ANR (Application Not Responding) dialogs

2. iOS: Use Xcode Instruments
   - Profile System Trace during submission
   - Look for any hangs or long-running operations

### Memory Testing

**Android:**
1. Open Android Profiler
2. Open and close review modal 10 times
3. Check memory graph
4. Expected: Stable memory usage, no leaks (should not keep increasing)

**iOS:**
1. Use Memory Graph in Xcode Debugger
2. Open and close review modal 10 times
3. Take memory snapshots before and after
4. Expected: No retained cycles, consistent memory usage

---

## Part 5: API Integration Verification

### Mock API Response

Verify the API integration with expected response format.

**Expected API Endpoint:**
```
POST /api/v1/restaurants/{restaurantId}/review
```

**Expected Request Body:**
```json
{
  "rating": 4,
  "comment": "Great food!"
}
```

**Expected Success Response (200):**
```json
{
  "id": "review-123",
  "restaurantId": "rest-456",
  "rating": 4,
  "comment": "Great food!",
  "createdAt": "2024-03-01T12:00:00Z"
}
```

**Expected Error Response (4xx/5xx):**
```json
{
  "error": "Invalid rating: must be 1-5",
  "code": "INVALID_RATING"
}
```

**Verification Steps:**
1. Use Chrome DevTools or Postman to monitor network requests
2. Submit a review
3. Expected:
   - One POST request to `/api/v1/restaurants/{id}/review`
   - Request body contains correct rating and comment
   - Response code 200
   - Modal dismisses

---

## Part 6: Checklist Summary

### Functionality
- [ ] Star rating display works (1-5 stars)
- [ ] Rating selection buttons work
- [ ] Comment input field accepts text
- [ ] Submit button enables/disables correctly
- [ ] Submit button triggers submission
- [ ] Modal auto-dismisses after success
- [ ] Success modal appears
- [ ] Error modal appears on failure
- [ ] Cancel button works
- [ ] Can retry after failure

### UI
- [ ] Star display updates when rating changes (Android)
- [ ] TextEditor shows comment input (iOS)
- [ ] OutlinedTextField shows comment input (Android)
- [ ] All buttons are properly sized and tappable
- [ ] Modal title is visible
- [ ] Loading state is visible during submission

### Navigation
- [ ] Correct ViewModel is injected
- [ ] Navigation events are emitted
- [ ] Platform UI responds to navigation events
- [ ] Modal dismissal is coordinated

### Data
- [ ] Rating is passed as Int (Android) / Int32 (iOS)
- [ ] Comment text is passed correctly
- [ ] Empty validation works
- [ ] Whitespace is trimmed

### Build & Compilation
- [ ] All platforms compile with zero errors
- [ ] No type errors or suppressions
- [ ] No warnings in changed files

---

## Part 7: Known Limitations & Troubleshooting

### If Modal Doesn't Appear

**Android:**
1. Check that RestaurantDetailRoute is correctly registered
2. Verify restaurant detail page loads without errors
3. Check Logcat for exceptions in modal composition

**iOS:**
1. Verify TabNavigationView has correct modal presentation code
2. Check that modal is present in navigation state
3. Use Xcode debugger to step through modal presentation

### If Submit Button Stays Disabled

**Android:**
1. Check that OutlinedTextField is updating `comment` state
2. Verify validation logic: `comment.isNotBlank()`
3. Add log statement in OutlinedTextField's onValueChange

**iOS:**
1. Verify TextEditor is binding to `@State private var comment`
2. Check that submitReview button condition is correct:
   ```swift
   .disabled(comment.trimmingCharacters(in: .whitespaces).isEmpty || isSubmitting)
   ```

### If ViewModel Is Not Injected

**Android:**
1. Check registry.createScopeForRoute() is called
2. Verify RestaurantDetailRoute is constructed with correct ID
3. Check that RestaurantDetailViewModel is in Koin scope

**iOS:**
1. Check that navigator.restaurantDetailHolder() returns non-nil
2. Verify holder.viewModel is properly initialized
3. Debug print the viewModel in getViewModelForModal()

### If Modal Auto-Dismisses Too Quickly

**iOS:**
1. Increase DispatchQueue.main.asyncAfter delay from 0.5 to 1.0
2. Check that viewModel.submitReview() is actually async
3. Verify the delay is intentional (gives visual feedback)

### If Network Errors Don't Show

**Android:**
1. Verify RestaurantRepositoryImpl throw/catch is working
2. Check that navigationViewModel.showReviewErrorAlert() is called
3. Verify error modal is registered in platform UI

**iOS:**
1. Check that submitReview() catches exceptions
2. Verify error message is passed to alert
3. Ensure error handling code is not skipped

---

## Part 8: Final Sign-Off

### When All Tests Pass

1. Create a summary document with:
   - Platform: Android/iOS
   - Date tested
   - Build version
   - Test results (all pass/some fail)

2. If all tests pass:
   - Consider moving to Phase 4 (extend pattern)
   - or Phase 5 (documentation)

3. If tests fail:
   - Document failures with screenshots
   - Create issues in project tracker
   - Fix issues and re-test

### Test Report Template

```markdown
## Testing Report - Review Submission Feature

**Platform:** [Android/iOS]
**Date:** [Date]
**Tester:** [Name]
**Build Commit:** 989cf70

### Scenario Results
- [ ] Scenario 1: Basic Flow - PASS/FAIL
- [ ] Scenario 2: Validation - PASS/FAIL
- [ ] Scenario 3: Error Handling - PASS/FAIL
- [ ] Scenario 4: Double-Submit Prevention - PASS/FAIL
- [ ] Scenario 5: Navigation - PASS/FAIL
- [ ] Scenario 6: Modal Lifecycle - PASS/FAIL

### Regression Tests
- [ ] Restaurant Detail Page - OK
- [ ] Navigation - OK
- [ ] Other Modals - OK

### Notes
[Any observations, issues, or comments]

### Sign-Off
Tested by: [Name]
Date: [Date]
Status: READY FOR RELEASE / NEEDS FIXES
```

---

## Next Steps

After testing completes:

**If all tests pass:**
1. Go to Phase 4: Extend to Settings feature
2. Or Phase 5: Create documentation

**If issues found:**
1. Create bug reports with reproduction steps
2. Fix issues in new commits
3. Re-run affected test scenarios
4. Update test report

**Documentation:**
- Keep this testing guide updated as new scenarios are discovered
- Share test results with team
- Update implementation guide based on testing learnings
