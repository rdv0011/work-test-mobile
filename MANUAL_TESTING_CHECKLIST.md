# Manual Testing Checklist - Ready to Execute

## Starting Point

**Branch:** `feature/phase0-navigation-foundation`  
**Latest Commits:**
- `989cf70` - UI Integration: Android and iOS
- `cdd21e9` - Phase 1 Architecture

**Build Status:** ✅ All platforms compile successfully

---

## Pre-Testing Setup (5 minutes)

### 1. Verify Environment
```bash
# Check you're on correct branch
git branch -v
# Expected: feature/phase0-navigation-foundation

# Verify latest commits
git log --oneline -3
# Expected: 989cf70 at top
```

### 2. Review Documentation
- [ ] Read QUICK_REFERENCE.md (4 pages, 5 min)
- [ ] Skim TESTING_GUIDE.md Part 1 (Environment Setup)
- [ ] Note test environment requirements

### 3. Verify Build
```bash
# Full build verification
./gradlew compileCommonMainKotlinMetadata compileDebugKotlinAndroid compileKotlinIosArm64 -x test --no-daemon

# Expected output:
# BUILD SUCCESSFUL in ~5s
```

---

## Testing Phases

### Phase A: Android Testing (15-20 minutes)

#### Setup
1. [ ] Connect Android device OR start emulator
2. [ ] Verify device is connected: `adb devices`
3. [ ] Build and install: `./gradlew installDebug`

#### Basic Flow Test (Scenario 1)
1. [ ] Launch app
2. [ ] Navigate to restaurant detail view
3. [ ] Tap "Leave a Review" button
4. [ ] Verify modal appears with:
   - [ ] Star rating display (5 filled stars)
   - [ ] 5 numbered buttons (1, 2, 3, 4, 5)
   - [ ] Comment text input field
   - [ ] Cancel button (enabled)
   - [ ] Submit Review button (disabled - grayed out)

5. [ ] Change rating to 3 stars (tap button "3")
6. [ ] Verify stars update: ⭐⭐⭐☆☆

7. [ ] Type comment: "Great food and service!"
8. [ ] Verify Submit button becomes enabled

9. [ ] Tap "Submit Review"
10. [ ] Verify:
    - [ ] Button shows loading state
    - [ ] Modal dismisses after ~0.5 seconds
    - [ ] Success modal appears

11. [ ] Dismiss success modal
12. [ ] Verify returned to restaurant detail page

**Result: PASS / FAIL**

#### Validation Test (Scenario 2)
1. [ ] Open review modal again
2. [ ] Keep comment field empty
3. [ ] Verify Submit button stays disabled
4. [ ] Type space: " "
5. [ ] Verify button still disabled (whitespace is treated as empty)
6. [ ] Type comment: "Good"
7. [ ] Verify button becomes enabled
8. [ ] Clear comment
9. [ ] Verify button becomes disabled again

**Result: PASS / FAIL**

#### Modal Lifecycle Test (Scenario 6)
1. [ ] Open review modal
2. [ ] Tap Cancel button
3. [ ] Verify modal dismisses without submitting
4. [ ] Open modal again
5. [ ] Verify state is clean (rating=5, comment="")
6. [ ] Enter data and submit
7. [ ] Verify modal dismisses and success appears
8. [ ] Dismiss success modal
9. [ ] Verify can open review modal again

**Result: PASS / FAIL**

#### Logcat Verification
1. [ ] Open Android Studio Logcat
2. [ ] Filter by package: `io.umain.munchies`
3. [ ] Submit review and watch logs
4. [ ] Verify logs show:
   - [ ] "Submitting review for restaurant: {id}"
   - [ ] "Review submitted successfully"
   - [ ] "Showing review success modal"

**Result: PASS / FAIL**

---

### Phase B: iOS Testing (15-20 minutes)

#### Setup
1. [ ] Start iOS Simulator (iPhone 15 recommended)
2. [ ] Build and run from Xcode or gradle
3. [ ] Verify app launches

#### Basic Flow Test (Scenario 1)
1. [ ] Navigate to restaurant detail view
2. [ ] Tap "Leave a Review" button/action
3. [ ] Verify modal appears with:
   - [ ] Title: "Leave a Review"
   - [ ] Star display (5 filled stars)
   - [ ] 5 numbered buttons (1, 2, 3, 4, 5)
   - [ ] "Add your comment" label
   - [ ] TextEditor for input
   - [ ] Cancel button (enabled)
   - [ ] Submit Review button (disabled)

4. [ ] Change rating to 4 stars
5. [ ] Verify stars update: ⭐⭐⭐⭐☆

6. [ ] Type comment: "Amazing experience!"
7. [ ] Verify Submit button becomes enabled

8. [ ] Tap "Submit Review"
9. [ ] Verify:
    - [ ] Button shows loading state
    - [ ] Modal auto-dismisses after ~0.5s
    - [ ] Success modal appears

10. [ ] Dismiss by tapping outside or done
11. [ ] Verify returned to restaurant detail

**Result: PASS / FAIL**

#### Validation Test (Scenario 2)
1. [ ] Open review modal
2. [ ] Leave comment empty
3. [ ] Verify Submit button is disabled
4. [ ] Type space
5. [ ] Verify button still disabled
6. [ ] Type comment text
7. [ ] Verify button becomes enabled
8. [ ] Delete all text
9. [ ] Verify button becomes disabled

**Result: PASS / FAIL**

#### Modal Lifecycle Test (Scenario 6)
1. [ ] Open review modal
2. [ ] Tap Cancel button
3. [ ] Verify modal dismisses
4. [ ] Open modal again
5. [ ] Verify clean state (rating=5, comment="")
6. [ ] Enter and submit
7. [ ] Verify success modal appears
8. [ ] Dismiss
9. [ ] Can reopen modal

**Result: PASS / FAIL**

#### Xcode Verification
1. [ ] Open Xcode console
2. [ ] Look for swift print statements
3. [ ] Submit review and verify logs show:
   - [ ] "RestaurantDetailViewModel: Submitting review"
   - [ ] Navigation ViewModel methods called

**Result: PASS / FAIL**

---

### Phase C: Regression Tests (5-10 minutes)

#### Restaurant Detail Page
1. [ ] Page loads correctly
2. [ ] Restaurant data displays
3. [ ] Other buttons work (share, call, directions)
4. [ ] No crashes or errors

**Result: PASS / FAIL**

#### Navigation Between Features
1. [ ] Can navigate from home → restaurant list → detail
2. [ ] Back button works
3. [ ] Tab switching works
4. [ ] No navigation state issues

**Result: PASS / FAIL**

#### Other Modals
1. [ ] Success modals from other features work
2. [ ] Error alerts display correctly
3. [ ] No conflicts with review modal

**Result: PASS / FAIL**

---

## Summary Report Template

```markdown
## Manual Testing Report - Review Submission Feature

**Date:** [Date]
**Tester:** [Name]
**Platform:** [Android/iOS]
**Device:** [Device/Emulator name]

### Android Testing Results
- Scenario 1 (Basic Flow): PASS / FAIL
- Scenario 2 (Validation): PASS / FAIL
- Scenario 6 (Modal Lifecycle): PASS / FAIL
- Logcat Verification: PASS / FAIL

### iOS Testing Results
- Scenario 1 (Basic Flow): PASS / FAIL
- Scenario 2 (Validation): PASS / FAIL
- Scenario 6 (Modal Lifecycle): PASS / FAIL
- Xcode Verification: PASS / FAIL

### Regression Tests
- Restaurant Detail Page: PASS / FAIL
- Navigation: PASS / FAIL
- Other Modals: PASS / FAIL

### Issues Found
[List any issues, with steps to reproduce]

### Overall Status
- Android: PASS / FAIL / PARTIAL
- iOS: PASS / FAIL / PARTIAL

### Sign-Off
Tested by: [Name]
Date: [Date]
Status: ✅ READY FOR RELEASE / ❌ NEEDS FIXES
```

---

## Troubleshooting During Testing

### If Modal Doesn't Appear
1. [ ] Check restaurant detail page loads
2. [ ] Check Logcat/Xcode console for exceptions
3. [ ] Verify review button exists and is tappable
4. [ ] Check git status - confirm on correct branch

### If Submit Button Stays Disabled
1. [ ] Verify you typed valid comment text
2. [ ] Check that whitespace is being trimmed
3. [ ] In Android: Check OutlinedTextField is updating state
4. [ ] In iOS: Check TextEditor is bound to @State

### If Modal Doesn't Auto-Dismiss
1. [ ] Check viewModel.submitReview() is being called
2. [ ] Verify DispatchQueue delay (iOS should be 0.5s)
3. [ ] Check Android submission handler calls dismiss
4. [ ] Verify no exceptions in business logic

### If Success Modal Doesn't Appear
1. [ ] Check API endpoint returns 200 (success)
2. [ ] Verify ModalRoute.ReviewSuccessModal is registered
3. [ ] Check platform UI is observing navigation state
4. [ ] Review AppCoordinator event handling

### If Network Error Appears
1. [ ] This is expected if API is offline
2. [ ] Should see error modal instead of success
3. [ ] Can retry after fixing connection
4. [ ] Error message should be user-friendly

---

## Quick Links

- **Quick Reference:** QUICK_REFERENCE.md (1-page template)
- **Detailed Testing:** TESTING_GUIDE.md (all scenarios)
- **Implementation:** PATTERN_EXTENSION_GUIDE.md (extending pattern)
- **Full Summary:** SESSION_SUMMARY.md (comprehensive overview)

---

## After Testing

### If All Tests Pass ✅
1. [ ] Complete sign-off report
2. [ ] Share results with team
3. [ ] Proceed to code review
4. [ ] Plan next features to implement pattern

### If Issues Found ❌
1. [ ] Document issues with reproduction steps
2. [ ] Create GitHub issues if not already tracked
3. [ ] Prioritize fixes
4. [ ] Fix in new commits
5. [ ] Re-test affected scenarios
6. [ ] Update sign-off report

### Next Steps
- [ ] Code review by team lead
- [ ] Fix any review comments
- [ ] Create PR with documentation
- [ ] Merge to main branch
- [ ] Plan Phase 4 (extend to other features)

---

## Estimated Time

| Phase | Duration | Status |
|-------|----------|--------|
| Setup | 5 min | Quick |
| Android Testing | 15-20 min | Moderate |
| iOS Testing | 15-20 min | Moderate |
| Regression Tests | 5-10 min | Quick |
| **Total** | **45-55 min** | **Doable in 1 hour** |

---

## Success Criteria

✅ **Pass when:**
- All 3 scenarios pass on both Android and iOS
- Regression tests show no new issues
- Logcat/Xcode verification succeeds
- No crashes or exceptions
- All modal state changes work correctly

❌ **Fail when:**
- Any scenario fails on either platform
- Modal doesn't appear or disappear
- Validation doesn't work
- Success/error modals don't show
- Crashes or exceptions occur

---

**Ready to test?** Start with Phase A (Android) or Phase B (iOS).

Questions? See TESTING_GUIDE.md Part 9 (Troubleshooting) or QUICK_REFERENCE.md.

Good luck! 🚀
