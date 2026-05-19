# Navigation Restoration Analysis: Executive Summary

**Document**: High-level summary and key takeaways  
**Audience**: Project stakeholders, decision makers  
**Date**: May 19, 2026 (Revised)

---

## Overview

The Umain Munchies mobile app uses **Kotlin Multiplatform Mobile (KMM)** to share navigation logic across iOS and Android. Since KMM provides no native navigation controller, we've implemented a **manual Redux-based navigation state machine** that mimics native navigation controller behavior.

This revision addresses a critical design mismatch: **navigation state was being unconditionally restored on every app launch**. The correct behaviour — matching both Android `NavController` and iOS `UINavigationController` — is:

> **Restore navigation state ONLY when the process was killed unexpectedly (crash, OS memory pressure) or a configuration change (rotation, locale change) forced the Activity/Scene to be recreated. A deliberate user exit must always produce a fresh launch.**

---

## Key Findings

### ✅ What's Working Well

1. **Redux Pattern is Solid** — Deterministic, testable, debuggable
2. **Tab Navigation Works** — Per-tab stacks prevent cross-tab contamination
3. **Scope Lifecycle Management** — ViewModels survive configuration changes via Koin
4. **Deep Link Support** — URLs converted to navigation states
5. **State Persistence Mechanism** — State is saved asynchronously after every event

### ⚠️ Critical Issue — Unconditional Restoration

| Behaviour | Old Design | Revised Design |
|-----------|-----------|----------------|
| App crash → relaunch | ✅ Restores | ✅ Restores |
| Configuration change (rotation) | ✅ Restores | ✅ Restores |
| User swipes app from recents → relaunches | ⚠️ **Also restores** (wrong!) | ✅ Fresh start |
| User presses back to root → relaunches | ⚠️ **Also restores** (wrong!) | ✅ Fresh start |
| First install / cold start | ⚠️ **May restore stale state** | ✅ Fresh start |

### Other Identified Gaps

| Issue | Impact | Effort | Status |
|-------|--------|--------|--------|
| Unconditional restoration (wrong semantics) | **High** | Low | **Priority 1** |
| No cleanup on deliberate exit | High | Low | **Priority 1** |
| `restoredFromCrash` flag never set | Medium | Low | Priority 1 |
| Modal stack restoration untested | Medium | Low | Priority 1 |
| Stack depth unlimited | Low | Low | Priority 2 |
| Async persistence race conditions | Low | Medium | Priority 2 |
| No route handler validation | Medium | Medium | Priority 2 |
| Analytics integration missing | Medium | Medium | Priority 3 |

---

## Priority 1 (Implement Immediately)

### 1.1 Add `RestoreConditionDetector`
```
What:   Interface + platform implementations that decide whether to restore state
Why:    Gate restoration to crash/config-change only (current design restores always)
How:
  Android — check savedInstanceState Bundle (present = restore, absent = fresh)
  iOS     — check absence of a "clean exit" flag in UserDefaults
Effort: 3-4 hours
Benefit: Correct UX; matches native platform contract
```

### 1.2 Clear Snapshot on Deliberate Exit
```
What:   Delete or invalidate the persisted snapshot when the user deliberately exits
Why:    Without this, old state leaks into the next fresh launch
How:
  Android — onDestroy() when isFinishing && !isChangingConfigurations
  iOS     — applicationWillTerminate
Effort: 1-2 hours
Benefit: Fresh-start guarantee on deliberate exit
```

### 1.3 Enable `restoredFromCrash` Flag
```
What:   Set restoredFromCrash=true in the snapshot when restoring after an unclean shutdown
Why:    Crash monitoring and optional "App recovered" UX nudge
Effort: 1-2 hours
Benefit: Better observability; enables crash analytics
```

### 1.4 Add Modal Restoration Tests (Both Paths)
```
What:   Tests for crash-path (restore) AND clean-exit path (no restore)
Why:    Both paths were previously untested or untested for the correct condition
Effort: 2-3 hours
Benefit: Regression safety; documents the contract
```

**Total Effort**: 7-11 hours | **Total Benefit**: High

---

## Priority 2 (Implement in Next Phase)

### 2.1 Navigation History Limits
Enforce max stack depth (e.g. 20 routes per tab) and auto-prune. Prevents snapshot bloat.

### 2.2 Persistence Ordering
Use a `Channel(capacity=1)` so only the latest state is ever written. Prevents stale-state recovery after rapid navigation.

### 2.3 Route Handler Validation
Wrap handlers in a `ValidatingRouteHandler` to catch silent failures.

**Total Effort**: 9-13 hours | **Total Benefit**: High

---

## Priority 3 (Nice to Have)

- Analytics hook
- Debugging tools
- Navigation documentation / playbook

---

## Recommended Action Plan

### Phase 0: Alignment (This week)
- [ ] Review revised semantics with team
- [ ] Confirm Android and iOS lifecycle hook locations
- [ ] Create tickets for Priority 1 tasks

### Phase 1: Correct Semantics (1 sprint)
- [ ] Implement `RestoreConditionDetector` on both platforms
- [ ] Add `onDestroy` / `applicationWillTerminate` cleanup
- [ ] Enable `restoredFromCrash` flag
- [ ] Add tests for both restore and non-restore paths
- [ ] Code review and merge

### Phase 2: Robustness (2-3 sprints)
- [ ] Navigation history limits
- [ ] Persistence ordering
- [ ] Route handler validation

### Phase 3: Observability (Future)
- [ ] Analytics hook
- [ ] Debug tooling

---

## Success Criteria

After Phase 1:
- ✅ User relaunching after deliberate exit sees a fresh app
- ✅ User relaunching after crash sees their previous state
- ✅ Configuration change (rotation) preserves navigation state
- ✅ Both paths covered by tests

After Phase 2:
- ✅ Navigation stack bounded
- ✅ Persistence race condition eliminated
- ✅ Route errors surfaced immediately

---

## Q&A

### Q: Why not use a published navigation library (Voyager, Decompose)?
**A**: Current implementation is production-ready and team-owned. Migration risk mid-project is high. Consider for next project or after stabilisation.

### Q: How does the revised design compare to native navigation controllers?

| Aspect | NavController (Android) | UINavigationController (iOS) | KMM Redux (Revised) |
|--------|------------------------|------------------------------|---------------------|
| Restoration trigger | `savedInstanceState` Bundle | `UIStateRestoration` (opt-in) | `RestoreConditionDetector` |
| Clean exit clears state | ✅ Yes | ✅ Yes (no restoration by default) | ✅ Yes (after revision) |
| Crash restores state | ✅ Yes (Bundle from system) | ❌ Not by default | ✅ Yes |
| Config change restores | ✅ Yes | ✅ Yes | ✅ Yes |

### Q: What if the user force-quits via the Android recents panel?
**A**: `onDestroy` is called with `isFinishing == true`. The snapshot is cleared and the next launch starts fresh. This matches `NavController` behaviour.

### Q: What about the iOS app switcher swipe-to-kill?
**A**: `applicationWillTerminate` is called. We write a "clean exit" flag. On next launch, the detector sees the flag and starts fresh.

### Q: What if the OS kills the process without warning (low memory, iOS background)?
**A**: `applicationWillTerminate` / `onDestroy` are **not** called. No clean-exit flag is written. On next launch, the detector treats it as a crash and restores. This is the correct behaviour.

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| Users see stale state after clean exit | **High** (current bug) | High | Priority 1.1 + 1.2 |
| Crash recovery fails | Low | High | Priority 1.3 + 1.4 |
| Snapshot bloat causes slow restoration | Medium | Medium | Priority 2.1 |
| Persistence race → wrong state after crash | Low | Medium | Priority 2.2 |

---

## Investment Summary

**Total Investment**: ~25 hours over 2-3 sprints

**Return on Investment**:
- ✅ Correct UX: users no longer see stale state after deliberate exit
- ✅ Reliable crash recovery
- ✅ Parity with native platform contracts
- ✅ Improved observability and debugging

---

**Document Generated**: May 19, 2026  
**Analysis Scope**: KMM navigation restoration semantics (core + platform integration)  
**Coverage**: Restoration conditions, snapshot lifecycle, crash vs. clean-exit discrimination
