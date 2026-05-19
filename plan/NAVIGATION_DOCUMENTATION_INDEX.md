# Navigation Documentation - Index & Roadmap

**Location:** `/plan/`  
**Last Updated:** May 19, 2026

---

## 📚 Documentation Files

### 1. **NAVIGATION_ARCHITECTURE_GUIDE.md** (41 KB)
**Comprehensive deep dive into the navigation system**

- Overview and design patterns (Redux + Observer)
- Complete component architecture
- AppCoordinator, NavigationState, NavigationEvent, NavigationReducer
- Deep link handling (cold/warm start, parser chain, feature handlers)
- Analytics integration (Observer Pattern, event tracking, data safety)
- Shared view models and navigation abstraction layers
- How to navigate (screen, modal, tab, deep link operations)
- Real-world code examples
- File reference guide

**Best For:** Understanding the complete architecture, implementation details, code patterns

**Key Sections:**
- ✅ Core Architecture (Redux + Observer Pattern)
- ✅ 4 Mermaid diagrams (flow, components, deep links, analytics)
- ✅ Deep link system with examples
- ✅ Analytics event lifecycle
- ✅ Feature navigation VMs
- ✅ 4 real-world code examples

---

### 2. **NAVIGATION_MERMAID_DIAGRAMS.md** (18 KB)
**Visual reference for all Mermaid diagrams**

- Complete navigation flow diagram
- Component architecture diagram
- Component dependency graph
- Deep link processing flow
- Deep link parser sequence diagram
- Analytics observer pattern architecture
- Analytics event lifecycle state machine
- Navigation ViewModel abstraction layers
- NavigationState tree structure diagram
- Redux reducer pattern diagram
- Navigation event type hierarchy
- Multi-feature integration diagram
- Screen navigation lifecycle state machine

**Best For:** Visual learners, presentations, architecture documentation

**Key Features:**
- ✅ 14+ Mermaid diagrams
- ✅ Color-coded components
- ✅ Clear flow relationships
- ✅ Self-contained reference
- ✅ Copy-paste ready diagrams

---

### 3. **NAVIGATION_QUICK_REFERENCE.md** (13 KB)
**Quick lookup guide for developers**

- System capabilities checklist
- System flow diagram (ASCII + Mermaid)
- Core components table
- Navigation methods reference (all APIs)
- Deep link examples (7+ examples)
- Analytics tracking events table
- Key design patterns explanation
- How to add new navigation (4-step guide)
- File locations and structure
- Testing navigation (unit tests, debugging tips)

**Best For:** Day-to-day development, quick lookups, integration reference

**Key Sections:**
- ✅ Method reference (all navigation APIs)
- ✅ Deep link examples
- ✅ File structure overview
- ✅ Debugging tips
- ✅ Testing patterns

---

## 🎯 Navigation System Capabilities

### ✅ Implemented Features

| Feature | Details | Documentation |
|---------|---------|---|
| **Deep Link Handling** | Cold start, warm start, parser chain, feature-specific handlers | GUIDE § 4 |
| **Navigation Analytics** | Observer pattern, screen tracking, tab tracking, modal tracking | GUIDE § 5 |
| **Shared View Models** | Feature-scoped, type-safe, abstraction layer | GUIDE § 6 |
| **Tab Navigation** | Independent stacks, state preservation, switching | GUIDE § 3 |
| **Modal System** | Multiple destinations, conditional dismissal, time tracking | GUIDE § 2 |
| **Redux Pattern** | Pure reducers, immutable state, predictable flow | GUIDE § 2 |
| **Platform Independence** | Identical logic Android/iOS | GUIDE § 1 |
| **Scope Management** | Koin scope lifecycle, creation/destruction | GUIDE § 3 |
| **State Persistence** | Always written after every event; read path gated by `RestoreConditionDetector` | RESTORATION ANALYSIS |
| **Restoration Gate** | Restore only on crash/OS-kill/config-change; clean exit produces fresh launch | RESTORATION ANALYSIS |

---

## 📖 How to Use These Docs

### Scenario 1: "I need to understand the architecture"
1. Start: **NAVIGATION_ARCHITECTURE_GUIDE.md** § Overview
2. Read: § Core Architecture (Redux + Observer)
3. Study: **NAVIGATION_MERMAID_DIAGRAMS.md** - System Overview diagram
4. Deep dive: § Navigation System Components

### Scenario 2: "I need to navigate to a screen"
1. Quick ref: **NAVIGATION_QUICK_REFERENCE.md** - Navigation Methods
2. Example: **NAVIGATION_ARCHITECTURE_GUIDE.md** § How to Navigate
3. Code: § Code Examples → Example 1

### Scenario 3: "I need to implement deep links"
1. Learn: **NAVIGATION_ARCHITECTURE_GUIDE.md** § Deep Link Handling
2. Diagram: **NAVIGATION_MERMAID_DIAGRAMS.md** - Deep Link Flow diagram
3. Reference: **NAVIGATION_QUICK_REFERENCE.md** - Deep Link Examples
4. Code: **NAVIGATION_ARCHITECTURE_GUIDE.md** § Code Examples → Example 2

### Scenario 4: "I need to add analytics"
1. Overview: **NAVIGATION_ARCHITECTURE_GUIDE.md** § Analytics Integration
2. Diagram: **NAVIGATION_MERMAID_DIAGRAMS.md** - Analytics Architecture diagram
3. Learn: § Analytics Observer Pattern Architecture
4. Example: **NAVIGATION_ARCHITECTURE_GUIDE.md** § Code Examples → Example 3

### Scenario 5: "I need to add a new feature with navigation"
1. Understand: **NAVIGATION_QUICK_REFERENCE.md** § How to Add New Navigation
2. Reference: **NAVIGATION_QUICK_REFERENCE.md** § File Locations
3. Create: Feature navigation ViewModel
4. Example: **NAVIGATION_ARCHITECTURE_GUIDE.md** § Code Examples → Example 4

### Scenario 6: "I'm debugging navigation issues"
1. Troubleshoot: **NAVIGATION_QUICK_REFERENCE.md** § Debugging Tips
2. Check: § Testing Navigation
3. Reference: **NAVIGATION_ARCHITECTURE_GUIDE.md** § Components

### Scenario 7: "I need to understand restoration behaviour"
1. Overview: **NAVIGATION_QUICK_REFERENCE.md** § State Persistence capability
2. Deep dive: `plan/navigation_restoration/KMM_NAVIGATION_RESTORATION_ANALYSIS.md` § Restoration Trigger Detection
3. Diagrams: `plan/navigation_restoration/ARCHITECTURE_PATTERNS.md` § Crash/Clean-Exit/Config-Change flows
4. Code: `plan/navigation_restoration/IMPLEMENTATION_GUIDE.md` § Priority 1

---

## 🏗️ System Architecture at a Glance

```
┌─────────────────────────────────────────┐
│  UI Layer (Composable / SwiftUI)        │
│  - Observes NavigationState StateFlow   │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  Feature Navigation ViewModels          │
│  - RestaurantNavigationViewModel        │
│  - SettingsNavigationViewModel          │
│  (type-safe, feature-scoped)            │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  NavigationDispatcher                   │
│  (generic navigation abstraction)       │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  AppCoordinator (Central Hub)           │
│  - Dispatches events                    │
│  - Manages state                        │
│  - Coordinates observers                │
│  - initializeNavigation(detector)       │
└──────────────────┬──────────────────────┘
                   │
    ┌──────────────┼──────────────┬──────────────┐
    │              │              │              │
    ▼              ▼              ▼              ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐
│NavigNav  │ │Analytics │ │Persistce │ │RestoreCondition  │
│Reducer   │ │ Listener │ │  Store   │ │Detector          │
│(Pure fn) │ │(Observer)│ │(always   │ │(crash vs. clean) │
│          │ │          │ │ write)   │ │                  │
└──────────┘ └──────────┘ └──────────┘ └──────────────────┘
```

---

## 🔑 Key Concepts

### Redux Pattern
- **State:** `NavigationState` (immutable)
- **Event:** `NavigationEvent` (sealed class)
- **Reducer:** `NavigationReducer` (pure function)
- **Flow:** Event → Reducer → State → UI

### Observer Pattern (Analytics)
- **Observable:** `navigationState: StateFlow<NavigationState>`
- **Observer:** `NavigationAnalyticsListener` (independent)
- **Decoupled:** No coupling between observer and coordinator
- **Thread-safe:** Coroutine-based collection

### Restoration Contract (Native NavController Semantics)
- **Write path:** State is persisted after **every** navigation event
- **Read path:** State is restored **only** when `RestoreConditionDetector.shouldRestoreNavigation()` returns `true`
- **Android trigger:** `savedInstanceState` Bundle present in `Activity.onCreate` (config change or process death)
- **iOS trigger:** Absence of a "clean exit" flag written in `applicationWillTerminate`
- **Clean exit:** `onDestroy(isFinishing=true)` / `applicationWillTerminate` → clear snapshot, next launch is fresh

### Abstraction Layers
1. **UI Layer** → Feature Navigation ViewModels
2. **Feature VMs** → NavigationDispatcher (generic)
3. **Dispatcher** → AppCoordinator (central)
4. **Coordinator** → NavigationReducer (pure)

### Deep Link Chain
1. **Platform Layer** → Extracts URI components
2. **AppCoordinator** → Calls `applyDeepLink()`
3. **DeepLinkParser** → Tries feature handlers
4. **Feature Handlers** → Parse and return result
5. **AppCoordinator** → Applies navigation state

---

## 📊 Component Matrix

| Component | File | Type | Layer |
|-----------|------|------|-------|
| **AppCoordinator** | `AppCoordinator.kt` | Class | Core |
| **NavigationState** | `NavigationState.kt` | Data Class | Core |
| **NavigationEvent** | `NavigationEvent.kt` | Sealed Class | Core |
| **NavigationReducer** | `NavigationReducer.kt` | Object (functions) | Core |
| **NavigationDispatcher** | `NavigationDispatcher.kt` | Class | Core |
| **Destination** | `Destination.kt` | Sealed Class | Core |
| **Route** | `Routes.kt` | Sealed Class | Core |
| **DeepLinkHandler** | `DeepLinkHandler.kt` | Interface | Core |
| **DeepLinkProcessor** | `DeepLinkProcessor.kt` | Object | Core |
| **RestoreConditionDetector** | `RestoreConditionDetector.kt` | Interface | Restoration |
| **AndroidRestoreConditionDetector** | `AndroidRestoreConditionDetector.kt` | Class | Restoration (Android) |
| **IosRestoreConditionDetector** | `IosRestoreConditionDetector.kt` | Class | Restoration (iOS) |
| **NavigationAnalyticsListener** | `NavigationAnalyticsListener.kt` | Class | Analytics |
| **RestaurantNavigationViewModel** | `RestaurantNavigationViewModel.kt` | Class | Feature |
| **SettingsDeepLinkHandler** | `SettingsDeepLinkHandler.kt` | Class | Feature |
| **ReviewsDeepLinkHandler** | `ReviewsDeepLinkHandler.kt` | Class | Feature |

---

## 🚀 Quick Start Examples

### Navigate to a Screen
```kotlin
navigationVM.showRestaurantDetail("123")
```

### Show a Modal
```kotlin
navigationVM.showFilterModal(preSelectedFilters = listOf("tag1"))
```

### Handle a Deep Link
```kotlin
coordinator.applyDeepLink("munchies://restaurants/123")
```

### Track Analytics (Automatic)
```
User navigates → AppCoordinator state changes → 
NavigationAnalyticsListener detects → 
Event emitted to Firebase/backend
```

---

## 📋 Feature Checklist

- ✅ **Deep Link Handling** - Full cold/warm start support
- ✅ **Navigation Analytics** - Track all screen transitions, modal events, time spent
- ✅ **Shared View Models** - Feature-scoped, type-safe navigation methods
- ✅ **Tab Navigation** - Multiple independent stacks
- ✅ **Modal System** - Multiple destinations, conditional dismissal
- ✅ **Redux Pattern** - Pure, predictable state management
- ✅ **Platform Independence** - Same logic on Android & iOS
- ✅ **Scope Management** - Automatic Koin scope lifecycle
- ✅ **State Persistence** - Written after every event; read path gated
- ✅ **Restoration Gate** - Restore only on crash/config-change; clean exit = fresh launch
- ✅ **Error Handling** - Graceful deep link parsing failures
- ✅ **Data Safety** - Sensitive data filtering for analytics
- ✅ **Logging** - Detailed navigation logs

---

## 🔗 Related Documentation

- **Navigation Restoration:** `plan/navigation_restoration/KMM_NAVIGATION_RESTORATION_ANALYSIS.md`
- **Type Export Refactor:** `plan/type_export_refactor/`
- **Build Documentation:** `BUILD.md`
- **Project README:** `README.md`

---

## 💡 Pro Tips

### For Developers
1. Always use feature-scoped navigation VMs, never pass `AppCoordinator` directly
2. Use `Destination` for type-safety, not raw strings
3. Analytics is automatic - no need to manually call tracking
4. Deep links use the same coordinator methods as UI navigation
5. Modals are handled in a separate `modalStack`, independent of screen stack

### For Architects
1. The Redux pattern ensures predictable, testable state management
2. The Observer pattern decouples analytics from the core system
3. Feature handlers make deep link extensible without modifying core
4. Koin scopes provide automatic dependency injection per screen
5. The abstraction layer (NavigationDispatcher) prevents tight coupling

### For Debugging
1. Check `AppCoordinator` logs - prefixed with 🔄, 🔧, ✅ for easy scanning
2. Monitor `NavigationAnalyticsListener` logs - prefixed with 📊, 📥, etc.
3. Use `coordinator.getCurrentState()` to inspect current navigation state
4. Test deep links directly with `coordinator.applyDeepLink()`
5. Verify route creation in reducer with NavigationReducerTest

---

## 📝 Documentation Status

| Document | Status | Last Updated | Lines | Size |
|----------|--------|---|---|---|
| NAVIGATION_ARCHITECTURE_GUIDE.md | ✅ Revised | May 19, 2026 | ~1340 | ~42 KB |
| NAVIGATION_MERMAID_DIAGRAMS.md | ✅ Revised | May 19, 2026 | 640+ | ~19 KB |
| NAVIGATION_QUICK_REFERENCE.md | ✅ Revised | May 19, 2026 | ~390 | ~14 KB |
| NAVIGATION_DOCUMENTATION_INDEX.md | ✅ This File | May 19, 2026 | - | - |

---

**Navigation Documentation Suite - Complete & Current** ✨
