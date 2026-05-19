# Navigation Architecture - Mermaid Diagrams Reference

**Location:** `/plan/NAVIGATION_MERMAID_DIAGRAMS.md`  
**Last Updated:** May 19, 2026

---

## Quick Navigation

1. [System Overview](#system-overview)
2. [Component Architecture](#component-architecture)
3. [Deep Link Flow](#deep-link-flow)
4. [Analytics Architecture](#analytics-architecture)
5. [Abstraction Layers](#abstraction-layers)
6. [State Structure](#state-structure)
7. [Event Processing](#event-processing)
8. [Feature Integration](#feature-integration)
9. [Restoration Decision](#restoration-decision)

---

## System Overview

### Complete Navigation Flow

```mermaid
graph TB
    subgraph "User Layer"
        U["🖱️ User Action<br/>(Tap Card, Button, etc)"]
    end
    
    subgraph "Feature Layer"
        FVM["📱 Feature NavigationViewModel<br/>(RestaurantNavigationViewModel)"]
        FVM1["showRestaurantDetail()"]
    end
    
    subgraph "Core Navigation Layer"
        ND["🔄 NavigationDispatcher<br/>(Generic Abstraction)"]
        AC["🎛️ AppCoordinator<br/>(Central Hub)"]
        NE["⚡ NavigationEvent<br/>(Push, Pop, ShowModal, etc)"]
    end
    
    subgraph "Redux Layer"
        NR["🔀 NavigationReducer<br/>(Pure Functions)"]
        NS["💾 NavigationState<br/>(Immutable Tree)"]
    end
    
    subgraph "Observers"
        PUI["🎨 Platform UI<br/>(Compose/SwiftUI)"]
        ANA["📊 Analytics Listener<br/>(Tracks Events)"]
        PS["💿 Persistence Store<br/>(Writes always;<br/>reads gated by detector)"]
        RCD["🔑 RestoreConditionDetector<br/>(crash vs. clean exit)"]
    end
    
    U -->|triggers| FVM1
    FVM1 -->|calls| FVM
    FVM -->|navigate| ND
    ND -->|wraps| AC
    AC -->|dispatch| NE
    NE -->|input| NR
    NR -->|State Event -> NewState| NS
    NS -->|updates| AC
    AC -->|emits| PUI
    AC -->|emits| ANA
    AC -->|emits| PS
    AC -->|uses on startup| RCD
    
    style U fill:#e1f5ff
    style FVM fill:#f3e5f5
    style FVM1 fill:#f3e5f5
    style ND fill:#e8f5e9
    style AC fill:#fff3e0
    style NE fill:#fce4ec
    style NR fill:#f1f8e9
    style NS fill:#ede7f6
    style PUI fill:#e0f2f1
    style ANA fill:#fff9c4
    style PS fill:#f5f5f5
    style RCD fill:#e8eaf6
```

---

## Component Architecture

### Navigation Components Overview

```mermaid
graph LR
    subgraph "Public API Layer"
        AC["AppCoordinator<br/>navigateToScreen()<br/>showModal()<br/>selectTab()"]
    end
    
    subgraph "State Management"
        NR["NavigationReducer<br/>reduce()"]
        NS["NavigationState<br/>tabNavigation<br/>modalStack"]
        NE["NavigationEvent<br/>Push / Pop<br/>ShowModal<br/>SelectTab"]
    end
    
    subgraph "Data Models"
        D["Destination<br/>RestaurantList<br/>RestaurantDetail<br/>Settings"]
        R["Route<br/>RestaurantListRoute<br/>RestaurantDetailRoute"]
        MD["ModalDestination<br/>Filter<br/>SubmitReview<br/>ConfirmAction"]
        MR["ModalRoute<br/>FilterRoute<br/>SubmitReviewRoute"]
    end
    
    subgraph "Observers"
        AAL["NavigationAnalyticsListener<br/>trackScreenView()<br/>trackModalOpen()"]
        NPS["NavigationPersistenceStore<br/>saveNavigationState() ← always<br/>loadNavigationState() ← gated"]
        RCD["RestoreConditionDetector<br/>shouldRestoreNavigation()"]
    end
    
    AC -->|emits| NE
    NE -->|input| NR
    NR -->|transforms| NS
    NS -->|emits to| AAL
    NS -->|emits to| NPS
    D -->|convert to| R
    MD -->|convert to| MR
    AC -->|uses| D
    NR -->|creates| R
    
    style AC fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    style NR fill:#f1f8e9,stroke:#689f38
    style NS fill:#ede7f6,stroke:#5e35b1
    style NE fill:#fce4ec,stroke:#c2185b
    style D fill:#e0f2f1,stroke:#00796b
    style R fill:#e0f2f1,stroke:#00796b
    style MD fill:#fce4ec,stroke:#c2185b
    style MR fill:#fce4ec,stroke:#c2185b
    style AAL fill:#fff9c4,stroke:#f57f17
    style NPS fill:#f5f5f5,stroke:#424242
    style RCD fill:#e8eaf6,stroke:#3949ab
```

### Component Dependencies

```mermaid
graph TB
    AC["🎛️ AppCoordinator"]
    ND["🔄 NavigationDispatcher"]
    NR["🔀 NavigationReducer"]
    NE["⚡ NavigationEvent"]
    NS["💾 NavigationState"]
    RH["🔗 RouteHandler"]
    DLH["🌐 DeepLinkHandler"]
    
    AC -->|depends| NR
    AC -->|depends| RH
    AC -->|depends| DLH
    ND -->|wraps| AC
    NE -->|input to| NR
    NR -->|produces| NS
    RH -->|creates| NS
    DLH -->|parsed by| NS
    
    style AC fill:#fff3e0,stroke:#f57c00,stroke-width:3px
    style ND fill:#e8f5e9,stroke:#2e7d32
    style NR fill:#f1f8e9,stroke:#689f38
    style NE fill:#fce4ec,stroke:#c2185b
    style NS fill:#ede7f6,stroke:#5e35b1
    style RH fill:#bbdefb,stroke:#1565c0
    style DLH fill:#c8e6c9,stroke:#388e3c
```

---

## Deep Link Flow

### Complete Deep Link Processing

```mermaid
graph TD
    subgraph "Entry Points"
        CS["🔥 Cold Start Deep Link<br/>munchies://restaurants/123"]
        WS["🌡️ Warm Start Deep Link<br/>Notification / Browser"]
    end
    
    subgraph "Platform Layer"
        AND["📱 Android<br/>Intent.data parsing"]
        IOS["🍎 iOS<br/>NSUserActivity parsing"]
    end
    
    subgraph "Core Processing"
        AC["AppCoordinator<br/>applyDeepLink(url)"]
        DLP["DeepLinkParser<br/>parse(deepLink)"]
    end
    
    subgraph "Feature Handlers"
        RDH["RestaurantDeepLinkHandler<br/>canHandle()<br/>parseDeepLink()"]
        SDH["SettingsDeepLinkHandler"]
        RVH["ReviewsDeepLinkHandler"]
    end
    
    subgraph "Result"
        DLR["DeepLinkResult<br/>Success / Partial<br/>NotFound / Error"]
        NS["NavigationState<br/>Updated & Applied"]
    end
    
    subgraph "UI Rendering"
        UI["🎨 Platform UI<br/>Observes New State<br/>Renders Full Stack"]
    end
    
    CS -->|extraction| AND
    WS -->|extraction| IOS
    AND -->|URI components| AC
    IOS -->|URI components| AC
    AC -->|parse| DLP
    DLP -->|try handlers| RDH
    DLP -->|try handlers| SDH
    DLP -->|try handlers| RVH
    RDH -->|return result| DLR
    SDH -->|return result| DLR
    DLR -->|create state| NS
    AC -->|reduce| NS
    NS -->|StateFlow emit| UI
    
    style CS fill:#ffebee,stroke:#c62828
    style WS fill:#fff3e0,stroke:#e65100
    style AND fill:#e8f5e9,stroke:#2e7d32
    style IOS fill:#e8f5e9,stroke:#2e7d32
    style AC fill:#e3f2fd,stroke:#1565c0
    style DLP fill:#f3e5f5,stroke:#6a1b9a
    style RDH fill:#fce4ec,stroke:#ad1457
    style SDH fill:#fce4ec,stroke:#ad1457
    style RVH fill:#fce4ec,stroke:#ad1457
    style DLR fill:#e0f2f1,stroke:#00695c
    style NS fill:#ede7f6,stroke:#512da8
    style UI fill:#fff9c4,stroke:#f57f17
```

### Deep Link Parser Chain

```mermaid
sequenceDiagram
    participant Platform as Platform Layer
    participant Parser as DeepLinkParser
    participant H1 as RestaurantDLH
    participant H2 as SettingsDLH
    participant H3 as ReviewsDLH
    participant Result as DeepLinkResult
    
    Platform->>Parser: parse("munchies://restaurants/123")
    Parser->>H1: canHandle()?
    H1-->>Parser: true
    Parser->>H1: parseDeepLink()
    H1->>H1: Parse & validate
    H1->>Result: DeepLinkResult.Success()
    Result-->>Parser: result
    Parser-->>Platform: result
    
    Note over H2,H3: Not called - first handler succeeded
```

---

## Analytics Architecture

### Observer Pattern - Analytics Tracking

```mermaid
graph TB
    subgraph "Navigation System"
        AC["🎛️ AppCoordinator<br/>Manages State"]
        NS["💾 NavigationState<br/>StateFlow Emitter"]
    end
    
    subgraph "Analytics Layer (Decoupled)"
        AAL["📊 NavigationAnalyticsListener<br/>Independent Observer"]
        SC["collect() StateFlow"]
        TD["Detect Changes<br/>screen / tab / modal"]
    end
    
    subgraph "Event Processing"
        SV["ScreenView Event<br/>screenName<br/>previousScreen<br/>properties"]
        TS["TabSwitch Event<br/>tabId<br/>tabName"]
        MO["ModalOpen Event<br/>modalName<br/>timeStarted"]
        MD["ModalDismiss Event<br/>modalName<br/>timeSpentMs"]
    end
    
    subgraph "Backend Services"
        FBA["🔥 Firebase Analytics<br/>Android"]
        FBI["🔥 Firebase Analytics<br/>iOS"]
        BE["📡 Backend Analytics API<br/>Custom Tracking"]
    end
    
    AC -->|updates| NS
    NS -->|emit new state| SC
    SC -->|subscribe| AAL
    AAL -->|collect| TD
    TD -->|screen changed| SV
    TD -->|tab changed| TS
    TD -->|modal opened| MO
    TD -->|modal closed| MD
    SV -->|trackEvent| FBA
    SV -->|trackEvent| FBI
    TS -->|trackEvent| FBA
    TS -->|trackEvent| FBI
    MO -->|trackEvent| FBA
    MO -->|trackEvent| FBI
    MD -->|trackEvent| FBA
    MD -->|trackEvent| FBI
    FBA -->|sync| BE
    FBI -->|sync| BE
    
    style AC fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    style NS fill:#ede7f6,stroke:#5e35b1,stroke-width:2px
    style AAL fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    style SC fill:#fffde7,stroke:#f9a825
    style TD fill:#fffde7,stroke:#f9a825
    style SV fill:#e8f5e9,stroke:#2e7d32
    style TS fill:#e8f5e9,stroke:#2e7d32
    style MO fill:#e8f5e9,stroke:#2e7d32
    style MD fill:#e8f5e9,stroke:#2e7d32
    style FBA fill:#bbdefb,stroke:#1565c0
    style FBI fill:#bbdefb,stroke:#1565c0
    style BE fill:#c8e6c9,stroke:#388e3c
```

### Analytics Event Lifecycle

```mermaid
stateDiagram-v2
    [*] --> StateUpdate: NavigationState Changes
    StateUpdate --> ListenerDetects: NavigationAnalyticsListener collects()
    ListenerDetects --> CalculateDiff: Compare prev vs current state
    CalculateDiff --> ScreenChanged: Previous route ≠ Current route?
    CalculateDiff --> TabChanged: Previous tab ≠ Current tab?
    CalculateDiff --> ModalChanged: Modal stack size changed?
    
    ScreenChanged --> EmitScreenView: Yes → ScreenView event
    ScreenChanged --> SkipScreen: No
    
    TabChanged --> EmitTabSwitch: Yes → TabSwitch event
    TabChanged --> SkipTab: No
    
    ModalChanged --> EmitModalOpen: Modal added → ModalOpen event
    ModalChanged --> EmitModalDismiss: Modal removed → ModalDismiss event
    ModalChanged --> SkipModal: No change
    
    EmitScreenView --> SendToAnalytics: trackEvent()
    EmitTabSwitch --> SendToAnalytics
    EmitModalOpen --> SendToAnalytics
    EmitModalDismiss --> SendToAnalytics
    
    SkipScreen --> [*]
    SkipTab --> [*]
    SkipModal --> [*]
    SendToAnalytics --> [*]
```

---

## Abstraction Layers

### Navigation ViewModel Layers

```mermaid
graph TB
    subgraph "UI Layer"
        RS["🖼️ RestaurantListScreen<br/>(Composable)"]
        RDS["🖼️ RestaurantDetailScreen<br/>(Composable)"]
    end
    
    subgraph "Feature Navigation VMs"
        RNVM["🎯 RestaurantNavigationViewModel<br/>showRestaurantDetail(id)<br/>showFilterModal()<br/>showSubmitReviewModal()<br/>navigateBack()"]
        SNVM["🎯 SettingsNavigationViewModel<br/>showRestaurantList()<br/>navigateBack()"]
    end
    
    subgraph "Core Navigation Abstraction"
        ND["🔄 NavigationDispatcher<br/>navigate(Destination)<br/>presentModal(ModalDestination)<br/>selectTab(tabId)<br/>navigateBack()"]
    end
    
    subgraph "Central Hub"
        AC["🎛️ AppCoordinator<br/>navigateToScreen()<br/>showModal()<br/>selectTab()<br/>dispatch(event)"]
    end
    
    subgraph "State & Events"
        NE["⚡ NavigationEvent"]
        NS["💾 NavigationState"]
    end
    
    RS -->|inject| RNVM
    RDS -->|inject| RNVM
    RS -->|inject| SNVM
    RNVM -->|calls| ND
    SNVM -->|calls| ND
    ND -->|wraps| AC
    AC -->|dispatch| NE
    AC -->|manages| NS
    
    style RS fill:#e1f5ff,stroke:#0277bd
    style RDS fill:#e1f5ff,stroke:#0277bd
    style RNVM fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
    style SNVM fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
    style ND fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    style AC fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    style NE fill:#fce4ec,stroke:#ad1457
    style NS fill:#ede7f6,stroke:#512da8
```

---

## State Structure

### NavigationState Tree

```mermaid
graph TD
    NS["NavigationState"]
    
    NS -->|contains| TNS["TabNavigationState"]
    NS -->|contains| MS["List of ModalRoutes"]
    
    TNS -->|contains| TD["List of TabDefinitions"]
    TNS -->|contains| AID["activeTabId: String"]
    TNS -->|contains| ST["stacksByTab: Map"]
    
    TD -->|ex| T1["TabDefinition<br/>id: 'restaurants'<br/>icon: Restaurant<br/>rootRoute: RestaurantListRoute"]
    TD -->|ex| T2["TabDefinition<br/>id: 'settings'<br/>icon: Settings<br/>rootRoute: SettingsRoute"]
    
    ST -->|key: 'restaurants'| SR["List of Routes<br/>RestaurantListRoute<br/>RestaurantDetailRoute"]
    ST -->|key: 'settings'| SSR["List of Routes<br/>SettingsRoute"]
    
    MS -->|ex| MR1["FilterRoute"]
    MS -->|ex| MR2["SubmitReviewRoute"]
    MS -->|ex| MR3["ConfirmActionRoute"]
    
    style NS fill:#ede7f6,stroke:#512da8,stroke-width:2px
    style TNS fill:#e3f2fd,stroke:#1565c0
    style MS fill:#fce4ec,stroke:#ad1457
    style AID fill:#fff9c4,stroke:#f57f17
    style T1 fill:#c8e6c9,stroke:#388e3c
    style T2 fill:#c8e6c9,stroke:#388e3c
    style SR fill:#e0f2f1,stroke:#00695c
    style SSR fill:#e0f2f1,stroke:#00695c
    style MR1 fill:#ffccbc,stroke:#d84315
    style MR2 fill:#ffccbc,stroke:#d84315
    style MR3 fill:#ffccbc,stroke:#d84315
```

---

## Event Processing

### Redux Reducer Pattern

```mermaid
graph LR
    subgraph "Input"
        CS["Current State<br/>(immutable)"]
        E["NavigationEvent<br/>(user action)"]
    end
    
    subgraph "Processing"
        R["NavigationReducer<br/>(pure function)"]
    end
    
    subgraph "Output"
        NS["New State<br/>(immutable copy)"]
    end
    
    subgraph "Effects"
        KS["Koin Scope<br/>Create/Destroy"]
        PS["Persist State<br/>(always; async)"]
        UI["UI Update<br/>StateFlow"]
    end
    
    CS -->|input| R
    E -->|input| R
    R -->|process| NS
    NS -->|triggers| KS
    NS -->|triggers| PS
    NS -->|emits via| UI
    
    style CS fill:#e3f2fd,stroke:#1565c0
    style E fill:#fce4ec,stroke:#ad1457
    style R fill:#f1f8e9,stroke:#689f38,stroke-width:2px
    style NS fill:#ede7f6,stroke:#512da8,stroke-width:2px
    style KS fill:#ffccbc,stroke:#d84315
    style PS fill:#f5f5f5,stroke:#424242
    style UI fill:#fff9c4,stroke:#f57f17
```

### Navigation Event Types

```mermaid
graph TB
    NE["NavigationEvent<br/>(sealed class)"]
    
    NE --> SCREEN["Screen Navigation"]
    NE --> MODAL["Modal Navigation"]
    NE --> TAB["Tab Navigation"]
    NE --> DL["Deep Linking"]
    
    SCREEN --> P["Push<br/>(Destination)"]
    SCREEN --> POP["Pop"]
    SCREEN --> PTR["PopToRoot"]
    
    MODAL --> SM["ShowModal<br/>(ModalDestination)"]
    MODAL --> DM["DismissModal"]
    MODAL --> DAM["DismissAllModals"]
    MODAL --> DMU["DismissModalUntil<br/>(predicate)"]
    
    TAB --> ST["SelectTab<br/>(tabId)"]
    TAB --> PIT["PushInTab<br/>(Destination)"]
    TAB --> POPIT["PopInTab"]
    
    DL --> ANS["ApplyNavigationState<br/>(newState)"]
    
    style NE fill:#fce4ec,stroke:#ad1457,stroke-width:2px
    style SCREEN fill:#e8f5e9,stroke:#2e7d32
    style MODAL fill:#bbdefb,stroke:#1565c0
    style TAB fill:#fff9c4,stroke:#f57f17
    style DL fill:#ffccbc,stroke:#d84315
    style P fill:#c8e6c9,stroke:#388e3c
    style POP fill:#c8e6c9,stroke:#388e3c
    style PTR fill:#c8e6c9,stroke:#388e3c
    style SM fill:#e1bee7,stroke:#7b1fa2
    style DM fill:#e1bee7,stroke:#7b1fa2
    style DAM fill:#e1bee7,stroke:#7b1fa2
    style DMU fill:#e1bee7,stroke:#7b1fa2
    style ST fill:#fff59d,stroke:#f9a825
    style PIT fill:#fff59d,stroke:#f9a825
    style POPIT fill:#fff59d,stroke:#f9a825
    style ANS fill:#ffccbc,stroke:#d84315
```

---

## Feature Integration

### Multi-Feature Navigation Architecture

```mermaid
graph TB
    subgraph "Core Module"
        AC["🎛️ AppCoordinator"]
        NR["🔀 NavigationReducer"]
        ND["🔄 NavigationDispatcher"]
    end
    
    subgraph "Feature: Restaurant"
        RVM["RestaurantListViewModel"]
        RNVM["RestaurantNavigationViewModel"]
        RDLH["RestaurantDeepLinkHandler"]
    end
    
    subgraph "Feature: Settings"
        SVM["SettingsViewModel"]
        SNVM["SettingsNavigationViewModel"]
        SDLH["SettingsDeepLinkHandler"]
    end
    
    subgraph "Feature: Reviews"
        RRVDLH["ReviewsDeepLinkHandler"]
    end
    
    subgraph "Platform: Android"
        ACompat["ComposeScreen"]
        ACS["Android CompositionLocal"]
    end
    
    subgraph "Platform: iOS"
        SwiftUI["SwiftUI View"]
        iOS_DI["iOS Dependency Injection"]
    end
    
    AC -->|coordinates| NR
    AC -->|coordinates| RDLH
    AC -->|coordinates| SDLH
    AC -->|coordinates| RRVDLH
    ND -->|wraps| AC
    
    RVM -->|uses| RNVM
    SVM -->|uses| SNVM
    RNVM -->|uses| ND
    SNVM -->|uses| ND
    
    ACompat -->|injects| RNVM
    ACS -->|provides| AC
    SwiftUI -->|injects| RNVM
    iOS_DI -->|provides| AC
    
    style AC fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    style NR fill:#f1f8e9,stroke:#689f38
    style ND fill:#e8f5e9,stroke:#2e7d32
    style RNVM fill:#f3e5f5,stroke:#6a1b9a
    style SNVM fill:#f3e5f5,stroke:#6a1b9a
    style RDLH fill:#bbdefb,stroke:#1565c0
    style SDLH fill:#bbdefb,stroke:#1565c0
    style RRVDLH fill:#bbdefb,stroke:#1565c0
    style ACompat fill:#e1f5ff,stroke:#0277bd
    style SwiftUI fill:#e1f5ff,stroke:#0277bd
```

---

## Additional Diagrams

### Screen Navigation Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Decision: App Starts

    Decision --> Restored: RestoreConditionDetector = true\n(crash / config-change)
    Decision --> Initial: RestoreConditionDetector = false\n(clean launch)

    Restored --> RestaurantList: Apply persisted state
    Initial --> RestaurantList: Default state

    RestaurantList --> RestaurantDetail: dispatch(Push)
    RestaurantDetail --> FilterModal: dispatch(ShowModal)
    FilterModal --> RestaurantDetail: dispatch(DismissModal)
    RestaurantDetail --> RestaurantList: dispatch(Pop)
    RestaurantList --> SettingsTab: dispatch(SelectTab)
    SettingsTab --> RestaurantList: dispatch(SelectTab)

    note right of Decision
        Android: savedInstanceState Bundle present?
        iOS: clean-exit flag absent?
    end note

    note right of Initial
        tabNavigation.activeTabId = "restaurants"
        stacksByTab["restaurants"] = [RestaurantListRoute]
        stacksByTab["settings"] = [SettingsRoute]
        modalStack = []
    end note

    note right of Restored
        Same state as last session
        (tabs, stacks, open modals)
        restoredFromCrash = true if crash
    end note

    note right of RestaurantDetail
        Stack: [RestaurantListRoute, RestaurantDetailRoute]
    end note

    note right of FilterModal
        modalStack: [FilterRoute]
        Tab stack unchanged
    end note

    note right of SettingsTab
        activeTabId = "settings"
        Restaurants stack preserved
    end note
```

---

## Restoration Decision

### When to Restore vs. Start Fresh

```mermaid
flowchart TD
    START(["App Starts"])
    DET{"RestoreConditionDetector\n.shouldRestoreNavigation()"}

    START --> DET

    DET -->|true| LOAD["Load persisted snapshot"]
    DET -->|false| FRESH["createDefaultNavigationState()"]

    LOAD --> VALID{"isValidSnapshot?"}
    VALID -->|yes| APPLY["Apply restored NavigationState\n(tabs, stacks, modals)"]
    VALID -->|no| FRESH

    FRESH --> INIT["Start from root\n(RestaurantListRoute)"]
    APPLY --> UI(["UI renders"])
    INIT --> UI

    subgraph "Android detector"
        AND_CHECK{"savedInstanceState\nBundle present?"}
        AND_YES["true — config change\nor process death"]
        AND_NO["false — cold start\nor clean exit"]
        AND_CHECK -->|yes| AND_YES
        AND_CHECK -->|no| AND_NO
    end

    subgraph "iOS detector"
        IOS_CHECK{"clean-exit flag\nabsent?"}
        IOS_YES["true — crash\nor OS kill"]
        IOS_NO["false — applicationWillTerminate\nwas called"]
        IOS_CHECK -->|yes| IOS_YES
        IOS_CHECK -->|no| IOS_NO
    end

    style START fill:#e8f5e9,stroke:#2e7d32
    style DET fill:#fff9c4,stroke:#f9a825,stroke-width:2px
    style LOAD fill:#e3f2fd,stroke:#1565c0
    style FRESH fill:#fce4ec,stroke:#c62828
    style VALID fill:#fff3e0,stroke:#e65100
    style APPLY fill:#e8f5e9,stroke:#2e7d32
    style INIT fill:#fce4ec,stroke:#c62828
    style UI fill:#ede7f6,stroke:#512da8,stroke-width:2px
```

### Clean Exit Cleanup Flow

```mermaid
sequenceDiagram
    participant User
    participant Platform
    participant Coordinator as AppCoordinator
    participant Store as PersistenceStore

    User->>Platform: Swipe app from recents (Android)\nor force-quit from switcher (iOS)

    alt Android
        Platform->>Coordinator: onDestroy(isFinishing=true,\nisChangingConfigurations=false)
        Coordinator->>Store: clearNavigationState()
        Note over Store: Snapshot deleted —\nnext launch gets fresh start
    else iOS
        Platform->>Coordinator: onApplicationWillTerminate()
        Coordinator->>Store: markCleanExit()
        Note over Store: Clean-exit flag written —\nIosRestoreConditionDetector\nreturns false on next launch
    end
```

---

**End of Mermaid Diagrams Reference**
