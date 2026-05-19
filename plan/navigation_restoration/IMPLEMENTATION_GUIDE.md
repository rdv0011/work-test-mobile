# KMM Navigation Restoration: Implementation Guide

**Document**: Step-by-step implementation guide for recommended improvements  
**Audience**: Developers implementing navigation system  
**Date**: May 19, 2026 (Revised — Crash/Config-Change-Only Restoration)

---

## Core Principle

Navigation state must be restored **only** when the OS or a crash forced the app to restart, not when the user deliberately exited. This mirrors what Android's `NavController` and iOS's `UINavigationController` do natively.

```
Restore?  YES  ← crash, OS kill, configuration change (rotation/locale)
Restore?  NO   ← user swiped app away, pressed back to root, force-quit
```

---

## Priority 1: Correct Restoration Semantics

### 1.1 Add `RestoreConditionDetector` Interface

**File**: `core/src/commonMain/.../navigation/restoration/RestoreConditionDetector.kt`

```kotlin
/**
 * Determines whether navigation state should be restored on this launch.
 *
 * Restoration fires ONLY for:
 *   - Configuration changes (rotation, locale, multi-window): Activity recreated with Bundle
 *   - Process death / crash: Activity recreated with Bundle by the system
 *
 * Restoration does NOT fire for:
 *   - User deliberately swiping app away from recents
 *   - User pressing back to root and relaunching
 *   - Fresh install
 */
interface RestoreConditionDetector {
    fun shouldRestoreNavigation(): Boolean
}
```

---

### 1.2 Android Implementation

**How Android signals "restore vs. fresh":**

Android passes a non-null `savedInstanceState` Bundle to `Activity.onCreate` **only** when the Activity is being recreated (configuration change or process death). A cold start always receives `null`. This is identical to how `NavController` decides whether to restore its back-stack.

**File**: `androidApp/.../navigation/AndroidRestoreConditionDetector.kt`

```kotlin
class AndroidRestoreConditionDetector(
    private val savedInstanceState: Bundle?
) : RestoreConditionDetector {

    override fun shouldRestoreNavigation(): Boolean =
        savedInstanceState?.getBoolean(KEY_RESTORE_NAV, false) ?: false

    companion object {
        const val KEY_RESTORE_NAV = "nav_restore_requested"
    }
}
```

**File**: `androidApp/.../MainActivity.kt`

```kotlin
class MainActivity : ComponentActivity() {

    private lateinit var coordinator: AppCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val detector = AndroidRestoreConditionDetector(savedInstanceState)
        coordinator = AppCoordinator(/* ... */)

        lifecycleScope.launch {
            coordinator.initializeNavigation(restoreConditionDetector = detector)
        }

        setContent { AppNavigation(coordinator) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Write the restore marker. The system will deliver this Bundle back
        // after a config change or process death — but NOT after a clean exit.
        outState.putBoolean(AndroidRestoreConditionDetector.KEY_RESTORE_NAV, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        // When the user deliberately dismisses the app, clear the snapshot
        // so the next cold start is a fresh launch.
        if (isFinishing && !isChangingConfigurations) {
            lifecycleScope.launch {
                coordinator.clearPersistedNavigationState()
            }
        }
    }
}
```

> **Why `onDestroy` and not `onStop`?**  
> `onStop` fires even when the app is backgrounded normally; the process might still be alive. `onDestroy` with `isFinishing == true` is the reliable signal that the Activity stack is being torn down by the user.

---

### 1.3 iOS Implementation

iOS has no equivalent of `savedInstanceState`. The closest reliable signal is:

- **`applicationWillTerminate`** is called when the user force-quits from the app switcher. We write a "clean exit" flag and clear it on the next launch.
- If `applicationWillTerminate` was **not** called (OS killed silently, crash), the flag is absent → restore.

**File**: `core/src/iosMain/.../navigation/IosRestoreConditionDetector.kt`

```kotlin
class IosRestoreConditionDetector(
    private val persistenceStore: NavigationPersistenceStore
) : RestoreConditionDetector {

    override fun shouldRestoreNavigation(): Boolean {
        val hadCleanExit = persistenceStore.hasCleanExitFlag()
        // Consume the flag so it doesn't influence a second restart
        persistenceStore.clearCleanExitFlag()
        return !hadCleanExit
    }
}
```

**File**: `iosApp/.../AppDelegate.swift` (or bridged Kotlin equivalent)

```swift
func applicationWillTerminate(_ application: UIApplication) {
    // User explicitly killed the app — mark clean exit
    coordinator.onApplicationWillTerminate()
}
```

```kotlin
// Kotlin side (called from Swift bridge)
fun onApplicationWillTerminate() {
    persistenceStore.markCleanExit()
}
```

**Add to `NavigationPersistenceStore` interface**:
```kotlin
interface NavigationPersistenceStore {
    // ... existing methods ...

    fun hasCleanExitFlag(): Boolean
    fun markCleanExit()
    fun clearCleanExitFlag()
}
```

---

### 1.4 Update `NavigationStateRestorer`

**File**: `core/src/commonMain/.../navigation/restoration/NavigationStateRestorer.kt`

```kotlin
class NavigationStateRestorer(
    private val persistenceStore: NavigationPersistenceStore
) {

    suspend fun restoreNavigationState(
        detector: RestoreConditionDetector
    ): NavigationState {
        // Gate: only restore when crash or config-change demands it
        if (!detector.shouldRestoreNavigation()) {
            logInfo(TAG, "Clean start — skipping restoration")
            return createDefaultNavigationState()
        }

        return try {
            val result = persistenceStore.loadNavigationState()

            when {
                result.isFailure -> {
                    logError(TAG, "Load failed: ${result.exceptionOrNull()?.message}")
                    createDefaultNavigationState()
                }

                result.getOrNull() == null -> {
                    logInfo(TAG, "No persisted state — using default")
                    createDefaultNavigationState()
                }

                !isValidSnapshot(result.getOrNull()!!) -> {
                    logInfo(TAG, "Snapshot invalid — using default")
                    createDefaultNavigationState()
                }

                else -> {
                    val snapshot = result.getOrNull()!!
                    logInfo(TAG, "Restoring navigation state (timestamp=${snapshot.restorationTimestamp})")
                    snapshot.toNavigationState()
                }
            }
        } catch (e: Exception) {
            logError(TAG, "Unexpected error during restoration: ${e.message}")
            createDefaultNavigationState()
        }
    }

    private fun isValidSnapshot(snapshot: NavigationStateSnapshot): Boolean {
        val tabNav = snapshot.tabNavigation
        return tabNav.tabDefinitions.isNotEmpty() &&
            tabNav.tabDefinitions.any { it.id == tabNav.activeTabId } &&
            tabNav.stacksByTab.values.all { it.isNotEmpty() }
    }

    companion object {
        private const val TAG = "NavigationStateRestorer"
    }
}
```

**Update `AppCoordinator`**:
```kotlin
suspend fun initializeNavigation(restoreConditionDetector: RestoreConditionDetector) {
    val restorer = NavigationStateRestorer(persistenceStore)
    val state = restorer.restoreNavigationState(restoreConditionDetector)
    applyNavigationState(state)
}

suspend fun clearPersistedNavigationState() {
    persistenceStore.clearNavigationState()
}
```

---

### 1.5 Add Modal Restoration Tests (Both Paths)

**File**: `core/src/commonTest/.../navigation/NavigationRestorationTest.kt`

```kotlin
class NavigationRestorationTest {

    private lateinit var store: FakeNavigationPersistenceStore
    private lateinit var restorer: NavigationStateRestorer

    @BeforeTest
    fun setup() {
        store = FakeNavigationPersistenceStore()
        restorer = NavigationStateRestorer(store)
    }

    // ── Crash path ────────────────────────────────────────────────────────────

    @Test
    fun testStateRestoredAfterCrash() = runTest {
        store.saveNavigationState(stateWithModal().toSnapshot())
        // No markCleanExit() — simulates crash or OS kill

        val detector = IosRestoreConditionDetector(store)
        val restored = restorer.restoreNavigationState(detector)

        assertEquals(1, restored.modalStack.size)
        assertTrue(restored.modalStack[0] is FilterModalRoute)
    }

    @Test
    fun testStateRestoredAfterConfigChange() = runTest {
        store.saveNavigationState(stateWithModal().toSnapshot())

        // Android config-change: savedInstanceState Bundle is present
        val bundle = Bundle().apply {
            putBoolean(AndroidRestoreConditionDetector.KEY_RESTORE_NAV, true)
        }
        val detector = AndroidRestoreConditionDetector(bundle)
        val restored = restorer.restoreNavigationState(detector)

        assertEquals(1, restored.modalStack.size)
    }

    // ── Clean-exit path ───────────────────────────────────────────────────────

    @Test
    fun testNoRestorationAfterCleanExit_iOS() = runTest {
        store.saveNavigationState(stateWithModal().toSnapshot())
        store.markCleanExit()  // Simulates applicationWillTerminate

        val detector = IosRestoreConditionDetector(store)
        val restored = restorer.restoreNavigationState(detector)

        assertEquals(0, restored.modalStack.size)  // Fresh start
    }

    @Test
    fun testNoRestorationAfterCleanExit_Android() = runTest {
        store.saveNavigationState(stateWithModal().toSnapshot())

        // Android cold start: savedInstanceState is null
        val detector = AndroidRestoreConditionDetector(savedInstanceState = null)
        val restored = restorer.restoreNavigationState(detector)

        assertEquals(0, restored.modalStack.size)  // Fresh start
    }

    @Test
    fun testCleanExitFlagConsumedOnNextLaunch() = runTest {
        store.markCleanExit()

        val detector = IosRestoreConditionDetector(store)
        detector.shouldRestoreNavigation()  // First call consumes the flag

        // Second launch without a new markCleanExit() → treated as crash → restore
        assertTrue(detector.shouldRestoreNavigation())
    }

    // ── Modal ordering ────────────────────────────────────────────────────────

    @Test
    fun testMultipleModalsRestoredInOrder() = runTest {
        val state = NavigationState(
            tabNavigation = defaultTabState(),
            modalStack = listOf(
                FilterModalRoute(listOf("vegetarian")),
                ConfirmActionModalRoute("Delete?", "Yes", "No")
            )
        )
        store.saveNavigationState(state.toSnapshot())

        val bundle = Bundle().apply {
            putBoolean(AndroidRestoreConditionDetector.KEY_RESTORE_NAV, true)
        }
        val restored = restorer.restoreNavigationState(AndroidRestoreConditionDetector(bundle))

        assertEquals(2, restored.modalStack.size)
        assertTrue(restored.modalStack[0] is FilterModalRoute)
        assertTrue(restored.modalStack[1] is ConfirmActionModalRoute)
    }
}

// ── Test double ──────────────────────────────────────────────────────────────

class FakeNavigationPersistenceStore : NavigationPersistenceStore {

    private var snapshot: NavigationStateSnapshot? = null
    private var cleanExitFlag = false

    override suspend fun saveNavigationState(snapshot: NavigationStateSnapshot) =
        Result.success(Unit.also { this.snapshot = snapshot })

    override suspend fun loadNavigationState() =
        Result.success(snapshot)

    override suspend fun clearNavigationState() =
        Result.success(Unit.also { snapshot = null })

    override suspend fun hasPersistedState() =
        Result.success(snapshot != null)

    override fun hasCleanExitFlag() = cleanExitFlag
    override fun markCleanExit() { cleanExitFlag = true }
    override fun clearCleanExitFlag() { cleanExitFlag = false }
}
```

---

## Priority 2: Robustness Improvements

### 2.1 Persistence Ordering with Channel

Prevents a rapid-navigation scenario where an older state is persisted last.

```kotlin
// In AppCoordinator
private val persistenceQueue = Channel<NavigationState>(capacity = Channel.CONFLATED)

init {
    persistenceScope.launch {
        for (state in persistenceQueue) {
            persistenceStore.saveNavigationState(state.toSnapshot())
                .onFailure { logError(TAG, "Persistence failed: ${it.message}") }
        }
    }
}

// In reduceState()
persistenceQueue.trySend(newState)  // Latest state always wins
```

`Channel.CONFLATED` drops older values automatically — only the latest state is written.

---

### 2.2 Stack Depth Monitoring

```kotlin
object NavigationMetrics {
    const val MAX_SAFE_STACK_DEPTH = 20
    const val WARN_STACK_DEPTH = 15
}

fun NavigationState.maxStackDepth(): Int =
    tabNavigation.stacksByTab.values.maxOfOrNull { it.size } ?: 0

// In AppCoordinator.reduceState()
val depth = newState.maxStackDepth()
if (depth > NavigationMetrics.MAX_SAFE_STACK_DEPTH) {
    logError(TAG, "Navigation stack depth CRITICAL: $depth")
} else if (depth > NavigationMetrics.WARN_STACK_DEPTH) {
    logInfo(TAG, "Navigation stack depth elevated: $depth")
}
```

---

## Rollout Checklist

- [ ] Add `RestoreConditionDetector` interface to common module
- [ ] Implement `AndroidRestoreConditionDetector`
- [ ] Implement `IosRestoreConditionDetector`
- [ ] Add `hasCleanExitFlag / markCleanExit / clearCleanExitFlag` to `NavigationPersistenceStore`
- [ ] Update `MainActivity.onSaveInstanceState` and `onDestroy`
- [ ] Bridge `applicationWillTerminate` on iOS
- [ ] Update `NavigationStateRestorer` to accept `RestoreConditionDetector`
- [ ] Update `AppCoordinator.initializeNavigation` to pass detector
- [ ] Add `clearPersistedNavigationState()` to `AppCoordinator`
- [ ] Write tests for crash path (restore) and clean-exit path (no restore)
- [ ] Run full test suite — no regressions
- [ ] Code review + merge
- [ ] Deploy to staging, verify manually:
  - [ ] Rotate device → state preserved ✅
  - [ ] Kill from recents → fresh launch ✅
  - [ ] Force-stop via adb → state restored on relaunch ✅

---

## Common Issues & Solutions

### Issue: State still restored after user swipes from recents (Android)

**Check**: Is `onDestroy` being called with `isFinishing == true`?  
Some OEM launchers delay or skip `onDestroy`. Use `ProcessLifecycleOwner` as a fallback:

```kotlin
ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
        if (activity.isFinishing) {
            // Fallback clear
            lifecycleScope.launch { coordinator.clearPersistedNavigationState() }
        }
    }
})
```

### Issue: iOS not calling `applicationWillTerminate` reliably

**Note**: `applicationWillTerminate` is only called when the app is in the foreground or recently suspended. Apps suspended for a long time are killed silently. This is the **correct** behaviour for our model — silent kill = potential crash = restore.

### Issue: Modal doesn't restore on iOS

Ensure modal route types are exported to Swift in `NavigationExports.ios.kt`:
```kotlin
fun _exportModalRoutesForSwift() {
    // Forces Swift linker to include modal route classes
    FilterModalRoute::class
    ConfirmActionModalRoute::class
}
```

---

## Next Steps

1. Implement Priority 1 items (restoration gate + cleanup)
2. Gather feedback from QA: test both paths manually on device
3. Move to Priority 2 (persistence ordering, history limits)
4. Create runbooks for diagnosing unexpected restoration or non-restoration
