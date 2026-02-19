# ⚡ QUICK START - Next 30 Minutes

**Time**: February 17, 2026  
**Goal**: Make your first commit of Phase 0  
**Effort**: 30 minutes maximum

---

## 🎯 What You're Doing Right Now

You're starting Phase 0 implementation by:
1. ✅ Reading this document (2 min)
2. 🔄 Committing current work (5 min)
3. 🔄 Verifying build (5 min)
4. 🔄 Creating first file: NavigationState.kt (10 min)
5. 🔄 Building and verifying (8 min)

---

## ✅ Step 1: Read Architecture Overview (2 min)

**Why?** Understand the 30,000-foot view before coding.

Open **NAVIGATION_IMPLEMENTATION_SUMMARY.md** and read:
- The 5 Key Architectural Decisions (section "🎯 Key Architectural Decisions")

This tells you:
- ✅ You're extending RouteHandler (not replacing it)
- ✅ Using pure reducers (no side effects)
- ✅ Separate modal stack from primary stack
- ✅ Per-tab stacks for history preservation
- ✅ Phased implementation (this is Phase 0)

**Time**: 2 minutes

---

## 🔄 Step 2: Commit Current Work (5 min)

**Why?** Save progress, keep git history clean.

### Run These Commands

```bash
cd /Users/rybakdmy/Development/private/work-test-mobile

# Check what changed
git status

# Should show these changes:
# - Modified: androidApp/.../PlatformAppRouteProviders.kt
# - Modified: iosApp/.../Route.swift
# - Deleted: 4 old handler files
# - Untracked: 8 documentation files (leave them)

# Commit only the code changes (not docs)
git add androidApp/src/main/kotlin/io/umain/munchies/android/navigation/PlatformAppRouteProviders.kt
git add iosApp/iosApp/Navigation/Route.swift
git commit -m "refactor: reorganize restaurant route providers and simplify handler registration"

# Verify
git status  # Should only show untracked docs now
```

**Expected Output**:
```
On branch feature/scaleable-navigation
Untracked files:
  (use "git add <file>..." to include in project tracking)
        DELIVERABLES.md
        IMPLEMENTATION_ACTION_PLAN.md
        NAVIGATION_ARCHITECTURE_INDEX.md
        ...8 documentation files...
        iosApp/iosApp/Features/Restuarants/Navigation/

nothing to commit
```

**Time**: 5 minutes

---

## 🔄 Step 3: Verify Build (5 min)

**Why?** Ensure no compilation errors before starting Phase 0.

### Run These Commands

```bash
# Build core module only (faster)
./gradlew :core:compileKotlin

# If successful, try full build
./gradlew build -x iosApp

# Or on macOS, include iOS:
./gradlew build
```

**Expected Output**:
```
BUILD SUCCESSFUL in Xs
```

**If build fails**:
- Check error message carefully
- The error might be in route provider refactoring
- If you can't fix it: `git reset --hard HEAD` and start over

**Time**: 5 minutes

---

## 🔄 Step 4: Create NavigationState.kt (10 min)

**Why?** This is the core data model for all navigation states.

### Create File

**Path**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationState.kt`

**Content**: Copy exactly from **NAVIGATION_IMPLEMENTATION_EXAMPLES.md**, Section 2.1

Here's the code (copy this exactly):

```kotlin
package io.umain.munchies.navigation

/**
 * The complete navigation state at any point in time.
 *
 * This state is:
 * - **Immutable**: Always use `copy()` to create new versions
 * - **Complete**: Contains everything needed to render navigation UI
 * - **Testable**: Easy to construct in tests
 * - **Serializable**: Can be saved/restored
 */
data class NavigationState(
    /**
     * Stack of routes being displayed.
     *
     * The last item in the list is the currently visible route.
     * The first item is always [StackRoute.RestaurantList] (app root).
     *
     * Example: `[RestaurantList, RestaurantDetail("123")]`
     * → Shows detail view with list in back stack
     */
    val primaryStack: List<StackRoute> = listOf(StackRoute.RestaurantList),
    
    /**
     * Stack of modal dialogs displayed on top of primary stack.
     *
     * These are independent from primaryStack and can be dismissed
     * without affecting the primary stack.
     *
     * Example: `[FilterModal([...])]`
     * → Shows filter modal over whatever is in primaryStack
     */
    val modalStack: List<ModalRoute> = emptyList()
)

/**
 * Routes that appear in the primary (full-screen) stack.
 *
 * Each variant represents a different full-screen route in your app.
 */
sealed class StackRoute {
    /**
     * Restaurant list view (app home screen).
     */
    data object RestaurantList : StackRoute()
    
    /**
     * Restaurant detail view.
     *
     * @param restaurantId ID of the restaurant to display
     */
    data class RestaurantDetail(val restaurantId: String) : StackRoute()
}

/**
 * Routes that appear in the modal stack (on top of primary stack).
 *
 * Empty for Phase 0. Will be populated in Phase 1 with modals like:
 * - FilterModal
 * - SortModal
 * - etc.
 */
sealed class ModalRoute {
    // Will be populated in Phase 1
}
```

### Save File

1. Create directory structure if needed: `core/src/commonMain/kotlin/io/umain/munchies/navigation/`
2. Create file: `NavigationState.kt`
3. Copy the code above

**Time**: 10 minutes

---

## 🔄 Step 5: Build and Verify (8 min)

**Why?** Ensure your new file compiles.

### Run Build

```bash
./gradlew :core:compileKotlin
```

**Expected Output**:
```
> Task :core:compileCommonMainKotlinMetadata
> Task :core:compileKotlin

BUILD SUCCESSFUL in 10s
```

**If compilation fails**:
- Check file is in correct location
- Check package statement: `package io.umain.munchies.navigation`
- Check all braces match
- Compare with NAVIGATION_IMPLEMENTATION_EXAMPLES.md section 2.1

**Time**: 8 minutes (build may take a while)

---

## 🎉 You're Done with Phase 0, Step 1!

You've successfully:
✅ Committed current work  
✅ Verified build  
✅ Created NavigationState.kt  
✅ Verified compilation  

**Total Time**: 30 minutes

---

## 🚀 What's Next (After This Session)

Continue with these steps (each in a separate 30-min session):

**Session 2**: TabNavigationState.kt (30 min)
**Session 3**: NavigationReducer.kt (90 min - longer, most complex)
**Session 4**: AppCoordinator.kt (60 min)
**Session 5**: Write unit tests (90 min - very important)
**Session 6**: Final verification + commit (30 min)

---

## ✨ Pro Tips

1. **Always verify each step compiles** before moving to next
2. **Copy code exactly** from NAVIGATION_IMPLEMENTATION_EXAMPLES.md
3. **Build after each file** - don't do multiple files then build
4. **Commit frequently** - each file is one commit
5. **Reference the docs** - when confused, check the relevant section

---

## 📚 Documents Reference

| Need | Document | Section |
|------|----------|---------|
| Code to copy | NAVIGATION_IMPLEMENTATION_EXAMPLES.md | 2.1, 2.2, 2.5, 2.6 |
| Understanding | NAVIGATION_SYSTEM_ARCHITECTURE.md | Full doc |
| Quick lookup | NAVIGATION_QUICK_REFERENCE.md | Any section |
| Timeline | NAVIGATION_MIGRATION_CHECKLIST.md | Phase breakdown |

---

## 💡 If You Get Stuck

**Compilation error?**
1. Check file location matches: `core/src/commonMain/kotlin/io/umain/munchies/navigation/NavigationState.kt`
2. Check package: `package io.umain.munchies.navigation`
3. Check sealed class syntax (need `sealed class` not `class`)
4. Compare with NAVIGATION_IMPLEMENTATION_EXAMPLES.md exactly

**Build fails?**
1. Run: `./gradlew clean :core:compileKotlin` (fresh build)
2. Check gradle error output carefully
3. If still stuck: `git reset --hard HEAD` and start over

**Not sure about next step?**
1. Read PHASE0_IMPLEMENTATION_GUIDE.md
2. Check IMPLEMENTATION_ACTION_PLAN.md
3. Reference NAVIGATION_IMPLEMENTATION_EXAMPLES.md

---

## 🎯 Session Summary

- ✅ Read architecture overview (2 min)
- ✅ Commit current work (5 min)
- ✅ Verify build (5 min)
- ✅ Create NavigationState.kt (10 min)
- ✅ Build & verify (8 min)
- **Total: 30 minutes**

---

## 📌 Check Off As You Go

- [ ] Read architecture overview (2 min)
- [ ] Commit current work (5 min)
- [ ] Verify build (5 min)
- [ ] Create NavigationState.kt file (10 min)
- [ ] Build and verify compilation (8 min)
- [ ] Celebrate! 🎉

---

**You're ready! Start with Step 1 right now. 🚀**

*Questions? Check the relevant document above.*
