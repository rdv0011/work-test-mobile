# KMP Navigation Architecture Analysis: Scaling for Multiple Features

**Status**: Analysis Complete (RESEARCH MODE - No Implementation)  
**Date**: 2026-02-09  
**Scope**: How to refactor feature route handling to support scalable multi-feature architecture

---

## Executive Summary

The current KMP app uses a **centralized, monolithic navigation pattern** where:
- **iOS**: A single `Route` enum + switch statements in `NavigationCoordinator` and `AppNavigationView`
- **Android**: Hardcoded `NavHost` + monolithic `when` expressions in `RouteRegistry` and `handleNavigationEvent`
- **Problem**: Adding a new feature requires modifying 4-5 files in the app layer
- **Solution**: Implement feature-based route handlers that allow features to own their routing logic

---

## Current State Analysis

### iOS Architecture

#### Route Definition (Bottleneck #1)
**File**: `iosApp/iosApp/Navigation/Route.swift`

```swift
enum Route: Hashable {
    case restaurantList
    case restaurantDetail(String)
    
    var key: String {
        switch self {
        case .restaurantList:
            return "RestaurantList"
        case .restaurantDetail(let restaurantId):
            return "RestaurantDetail_\(restaurantId)"
        }
    }
    
    var isRootRoute: Bool {
        switch self {
        case .restaurantList: return true
        case .restaurantDetail: return false
        }
    }
    
    static var rootRoutes: [Route] {
        [.restaurantList]
    }
}
```

**Issue**: 
- Concrete `enum` → requires modification every time a new feature adds routes
- All route cases defined in one place → central coupling point
- No extensibility mechanism for feature modules

#### Route → ViewModel Mapping (Bottleneck #2)
**File**: `iosApp/iosApp/Navigation/RouteHolderRegistry.swift`

```swift
func restaurantListHolder() -> RestaurantListViewModelHolder { ... }
func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder { ... }
```

**Issue**:
- Hardcoded methods for each route's holder
- Tightly coupled to specific ViewModel holder types
- No registry pattern for dynamic holder creation

#### Event → Route Translation (Bottleneck #3)
**File**: `iosApp/iosApp/Navigation/NavigationCoordinator.swift`

```swift
private func handlePush(destination: shared.Destination) {
    switch destination {
    case is Destination.RestaurantList:
        break
    case let detail as Destination.RestaurantDetail:
        let route = Route.restaurantDetail(detail.restaurantId)
        routeStack.append(route)
        path.append(route)
        updateActiveRoutes()
    default:
        break
    }
}
```

**Issue**:
- Each `Destination` type requires a case in the switch
- Manual route construction for each destination type
- Logic scattered between multiple functions

#### View Instantiation (Bottleneck #4)
**File**: `iosApp/iosApp/App/AppNavigationView.swift`

```swift
@ViewBuilder
private func destinationView(for route: Route) -> some View {
    switch route {
    case .restaurantDetail(let restaurantId):
        let holder = navigator.restaurantDetailHolder(restaurantId: restaurantId)
        RestaurantDetailView(
            restaurantId: restaurantId,
            coordinator: navigator.coordinator,
            viewModel: holder.viewModel,
            holder: holder
        )
    case .restaurantList:
        EmptyView()
    }
}
```

**Issue**:
- Direct switch on `Route` enum for view instantiation
- Requires importing specific feature views
- Creates tight coupling between app and features

---

### Android Architecture

#### NavHost Registration (Bottleneck #1)
**File**: `androidApp/src/main/kotlin/io/umain/munchies/android/navigation/AppNavigation.kt`

```kotlin
@Composable
fun AppNavigation(coordinator: AppCoordinator) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Destination.ROUTE_RESTAURANT_LIST
    ) {
        composable(Destination.ROUTE_RESTAURANT_LIST) {
            RestaurantListScreen(coordinator)
        }
        
        composable(
            route = Destination.ROUTE_RESTAURANT_DETAIL,
            arguments = listOf(
                navArgument(Destination.ARG_RESTAURANT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString(Destination.ARG_RESTAURANT_ID) ?: ""
            RestaurantDetailScreen(restaurantId, coordinator)
        }
    }
}
```

**Issue**:
- All routes hardcoded in single `NavHost` block
- Direct imports of feature screens required
- No way for features to self-register their routes

#### Route Factory (Bottleneck #2)
**File**: `androidApp/src/main/kotlin/io/umain/munchies/android/navigation/RouteRegistry.kt`

```kotlin
private fun createOwner(route: Route): ScopedViewModelOwner {
    val koin = GlobalContext.get()
    return when (route) {
        is RestaurantListRoute -> {
            val scope = koin.getScopeOrNull(route.key)
                ?: koin.createScope(
                    scopeId = route.key,
                    qualifier = named("RestaurantListScope")
                )
            ScopedViewModelOwner(scope)
        }
        is RestaurantDetailRoute -> {
            val scope = koin.getScopeOrNull(route.key)
                ?: koin.createScope(
                    scopeId = route.key,
                    qualifier = named(RestaurantDetailScope("").qualifierName)
                )
            val viewModel: RestaurantDetailViewModel = scope.get(
                parameters = { parametersOf(route.restaurantId) }
            )
            ScopedViewModelOwner(scope)
        }
        else -> throw IllegalArgumentException("No owner registered for route $route")
    }
}
```

**Issue**:
- Hardcoded `when` expression for each route type
- Violates Open-Closed Principle (OCP)
- Direct dependency on specific `RestaurantDetailScope` and `RestaurantDetailViewModel`
- Adding a new feature requires modifying this file

#### Event Handling (Bottleneck #3)
**File**: `androidApp/src/main/kotlin/io/umain/munchies/android/navigation/AppNavigation.kt`

```kotlin
private fun handleNavigationEvent(
    event: NavigationEvent,
    navController: NavHostController,
    trackedRoutes: androidx.compose.runtime.MutableState<Set<String>>
) {
    when (event) {
        is NavigationEvent.Push -> {
            when (event.destination) {
                is Destination.RestaurantDetail -> {
                    val detail = event.destination as Destination.RestaurantDetail
                    trackedRoutes.value = trackedRoutes.value + 
                        RestaurantDetailRoute(detail.restaurantId).key
                }
                else -> {}
            }
            val route = event.destination.toRoute()
            navController.navigate(route)
        }
        // ... other cases
    }
}
```

**Issue**:
- Nested `when` statements for route-specific logic
- Manual tracking of specific route types
- Each destination requires special handling

---

### Shared (Core) Architecture

#### Route Interface (Partially Good)
**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/Route.kt`

```kotlin
interface Route : Comparable<Route> {
    val key: String
    val isRootRoute: Boolean
        get() = false
    
    companion object {
        val rootRoutes: List<Route>
            get() = listOf(RestaurantListRoute())
    }
}
```

**Good**:
- Route is an interface (extensible ✓)
- Has `isRootRoute` property (metadata ✓)
- Has `rootRoutes` static property (centralized knowledge)

**Issue**:
- `rootRoutes` companion property hardcodes list (still coupled)
- No factory or handler registration mechanism

#### Destination Definition (Hard to Extend)
**File**: `core/src/commonMain/kotlin/io/umain/munchies/navigation/Destination.kt`

```kotlin
sealed class Destination {
    data object RestaurantList : Destination()
    data class RestaurantDetail(val restaurantId: String) : Destination()
    
    fun toRoute(): String = when (this) {
        is RestaurantList -> "restaurant_list"
        is RestaurantDetail -> "restaurant_detail/$restaurantId"
    }
    
    companion object {
        const val ROUTE_RESTAURANT_LIST = "restaurant_list"
        const val ROUTE_RESTAURANT_DETAIL_BASE = "restaurant_detail"
        const val ARG_RESTAURANT_ID = "restaurantId"
        const val ROUTE_RESTAURANT_DETAIL = "$ROUTE_RESTAURANT_DETAIL_BASE/{$ARG_RESTAURANT_ID}"
    }
}
```

**Issue**:
- `sealed class` → cannot be extended by feature modules
- `toRoute()` requires modification when new destinations added
- Constants are scattered

---

## Scalability Bottleneck Summary

| Bottleneck | iOS | Android | Shared | Problem |
|---|---|---|---|---|
| **Route Definition** | `Route` enum | `Route` interface ✓ | `Route` interface ✓ | iOS enum prevents extension |
| **Destination Mapping** | Manual in coordinator | Manual in event handler | Sealed class | Cannot add destinations in features |
| **ViewModel/Owner Factory** | Hardcoded methods | Hardcoded `when` | N/A | Each route needs explicit factory |
| **View/Screen Instantiation** | Switch in AppNav | Hardcoded in NavHost | N/A | Features can't self-register |
| **Root Route Definition** | Hardcoded list | `Route.rootRoutes` computed | Partial (still hardcoded) | Central knowledge required |

---

## How to Add a New Feature Today (Current Pattern)

### Example: Adding "UserProfile" Feature

**Files That Must Be Modified**:

1. **Core** (`core/src/commonMain/`):
   - Add to `Destination` sealed class
   - Add `UserProfileRoute` data class to `Routes.kt`
   - Update `Route.rootRoutes` if it's a root route

2. **iOS** (`iosApp/iosApp/`):
   - Add case to `Route` enum in `Route.swift`
   - Add `userProfileHolder()` to `RouteHolderRegistry`
   - Add mapping in `NavigationCoordinator.handlePush()`
   - Add case in `AppNavigationView.destinationView()`

3. **Android** (`androidApp/src/main/`):
   - Add `composable()` entry to `AppNavigation.kt` NavHost
   - Add case to `RouteRegistry.createOwner()` when expression
   - Add tracking logic to `handleNavigationEvent()` if needed

4. **Feature** (`feature-user/`):
   - Define UserProfileViewModel
   - Define DI module with Koin scopes
   - Create iOS `UserProfileViewModelHolder`
   - Create screens/views

**Total Files Modified: 8-10 (across multiple modules)**

---

## Proposed Solution: Feature-Based Route Handlers

### Goals

1. ✅ Allow features to own their routing logic
2. ✅ Eliminate hardcoded switch statements in app layer
3. ✅ Support both iOS (enum-less routing) and Android (dynamic NavGraph)
4. ✅ Maintain type-safe routing
5. ✅ Keep shared `Route` and `Destination` interfaces clean

### Architecture Pattern: Feature Navigation Providers

#### Phase 1: Shared Layer (Core Module)

**1.1 Define Route Handler Interface**

```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/navigation/RouteHandler.kt

interface RouteHandler {
    val route: Route
    
    /**
     * Convert a Route to platform-specific representation.
     * iOS: Used to identify which view to show
     * Android: Route string for NavController.navigate()
     */
    fun toRouteString(): String
    
    /**
     * Check if this handler can handle the given destination
     */
    fun canHandle(destination: Destination): Boolean
    
    /**
     * Convert destination to a Route instance
     */
    fun destinationToRoute(destination: Destination): Route?
}
```

**1.2 Define Route Provider Interface**

```kotlin
// core/src/commonMain/kotlin/io/umain/munchies/navigation/RouteProvider.kt

interface RouteProvider {
    /**
     * Return all routes this feature provides
     */
    fun getRoutes(): List<RouteHandler>
}
```

**1.3 Update Route Marker Interface**

```kotlin
// Existing Route interface stays the same, but add:
interface Route : Comparable<Route> {
    val key: String
    val isRootRoute: Boolean
        get() = false
    
    /**
     * Feature-provided metadata for route handling
     */
    val handler: RouteHandler?
        get() = null
}
```

#### Phase 2: Android Implementation

**2.1 Feature Navigation API**

```kotlin
// feature-restaurant/src/main/kotlin/io/umain/munchies/feature/restaurant/navigation/RestaurantNavigation.kt

class RestaurantRouteProvider : RouteProvider {
    override fun getRoutes(): List<RouteHandler> = listOf(
        RestaurantListRouteHandler,
        RestaurantDetailRouteHandler
    )
}

object RestaurantListRouteHandler : RouteHandler {
    override val route = RestaurantListRoute()
    
    override fun toRouteString() = "restaurant_list"
    
    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantList
    
    override fun destinationToRoute(destination: Destination): Route? {
        return if (canHandle(destination)) RestaurantListRoute() else null
    }
}

object RestaurantDetailRouteHandler : RouteHandler {
    override val route = RestaurantDetailRoute("")  // template
    
    override fun toRouteString() = "restaurant_detail/{restaurantId}"
    
    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantDetail
    
    override fun destinationToRoute(destination: Destination): Route? {
        return (destination as? Destination.RestaurantDetail)?.let {
            RestaurantDetailRoute(it.restaurantId)
        }
    }
}
```

**2.2 Refactored AppNavigation (Android)**

```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/navigation/AppNavigation.kt

@Composable
fun AppNavigation(
    coordinator: AppCoordinator,
    routeProviders: List<RouteProvider> = listOf(RestaurantRouteProvider())
) {
    val navController = rememberNavController()
    val registry = remember { RouteRegistry(routeProviders) }
    
    NavHost(
        navController = navController,
        startDestination = "restaurant_list"
    ) {
        // Dynamically build graph from route providers
        routeProviders.forEach { provider ->
            buildNavGraphForProvider(provider, registry, navController)
        }
    }
}

private fun NavGraphBuilder.buildNavGraphForProvider(
    provider: RouteProvider,
    registry: RouteRegistry,
    navController: NavHostController
) {
    provider.getRoutes().forEach { handler ->
        when (handler.route) {
            is RestaurantListRoute -> {
                composable("restaurant_list") {
                    RestaurantListScreen(coordinator)
                }
            }
            is RestaurantDetailRoute -> {
                composable("restaurant_detail/{restaurantId}") { backStack ->
                    val restaurantId = backStack.arguments?.getString("restaurantId") ?: ""
                    RestaurantDetailScreen(restaurantId, coordinator)
                }
            }
        }
    }
}
```

**2.3 Refactored RouteRegistry (Android)**

```kotlin
// androidApp/src/main/kotlin/io/umain/munchies/android/navigation/RouteRegistry.kt

class RouteRegistry(
    private val routeProviders: List<RouteProvider>
) {
    private val holders = mutableMapOf<String, Closeable>()
    private val handlerMap = buildHandlerMap()
    
    private fun buildHandlerMap(): Map<KClass<out Route>, ScopeFactory> {
        return routeProviders
            .flatMap { it.getRoutes() }
            .associateBy { it.route::class }
            .mapValues { (_, handler) -> createScopeFactory(handler) }
    }
    
    private fun createScopeFactory(handler: RouteHandler): ScopeFactory {
        return when (handler.route) {
            is RestaurantListRoute -> RestaurantListScopeFactory()
            is RestaurantDetailRoute -> RestaurantDetailScopeFactory()
            else -> throw IllegalArgumentException("Unknown route: ${handler.route}")
        }
    }
}

interface ScopeFactory {
    fun createScope(route: Route): Scope
}
```

#### Phase 3: iOS Implementation

**3.1 Feature Navigation API (iOS)**

```swift
// feature-restaurant/src/iosMain/swift/RestaurantNavigation.swift

protocol RestaurantRouteProvider: RouteProvider {
    func restaurantListHolder() -> RestaurantListViewModelHolder
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder
}

class DefaultRestaurantRouteProvider: RestaurantRouteProvider {
    func getRoutes() -> [Route] {
        return [.restaurantList, .restaurantDetail("template")]
    }
    
    func canHandle(destination: Destination) -> Bool {
        return destination is Destination.RestaurantList || 
               destination is Destination.RestaurantDetail
    }
    
    func destinationToRoute(destination: Destination) -> Route? {
        switch destination {
        case is Destination.RestaurantList:
            return .restaurantList
        case let detail as Destination.RestaurantDetail:
            return .restaurantDetail(detail.restaurantId)
        default:
            return nil
        }
    }
    
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        return RestaurantListViewModelHolder()
    }
    
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        return RestaurantDetailViewModelHolder(restaurantId: restaurantId)
    }
}
```

**3.2 Refactored NavigationCoordinator (iOS)**

```swift
class NavigationCoordinator: ObservableObject {
    @Published var path = NavigationPath()
    
    let coordinator: AppCoordinator
    private let registry: RouteHolderRegistry
    private var routeStack: [Route] = []
    private let routeProviders: [RouteProvider]
    
    init(coordinator: AppCoordinator, routeProviders: [RouteProvider] = [DefaultRestaurantRouteProvider()]) {
        self.coordinator = coordinator
        self.routeProviders = routeProviders
        self.registry = RouteHolderRegistry(providers: routeProviders)
        observeNavigationEvents()
    }
    
    private func handlePush(destination: shared.Destination) {
        // Delegate to providers to find matching handler
        for provider in routeProviders {
            if provider.canHandle(destination: destination),
               let route = provider.destinationToRoute(destination: destination) {
                routeStack.append(route)
                path.append(route)
                updateActiveRoutes()
                return
            }
        }
        print("Warning: No handler found for destination \(destination)")
    }
}
```

**3.3 Refactored RouteHolderRegistry (iOS)**

```swift
class RouteHolderRegistry {
    private var holders: [String: AnyObject] = [:]
    private let providers: [RouteProvider]
    
    init(providers: [RouteProvider]) {
        self.providers = providers
    }
    
    func holderFor(route: Route) -> AnyObject? {
        let key = route.key
        
        if let existing = holders[key] {
            return existing
        }
        
        // Find provider that can create this route
        for provider in providers as? [RestaurantRouteProvider] ?? [] {
            if let holder = createHolder(provider: provider, route: route) {
                holders[key] = holder
                return holder
            }
        }
        
        return nil
    }
    
    private func createHolder(provider: RestaurantRouteProvider, route: Route) -> AnyObject? {
        switch route {
        case .restaurantList:
            return provider.restaurantListHolder()
        case .restaurantDetail(let restaurantId):
            return provider.restaurantDetailHolder(restaurantId: restaurantId)
        }
    }
}
```

---

## Implementation Phases

### Phase 1: Shared Contracts (Low Risk)
- Add `RouteHandler` and `RouteProvider` interfaces to core
- Add `RouteFactory` interface for iOS/Android specific factories
- No changes to existing code - purely additive

### Phase 2: Android Refactor (Medium Risk)
- Update `AppNavigation.kt` to accept `RouteProvider` list
- Refactor `RouteRegistry.createOwner()` to use handler map
- Update `handleNavigationEvent()` to delegate to handlers
- Minimal impact: Still works with existing routes

### Phase 3: iOS Refactor (Medium Risk)
- Create `RouteProvider` protocol in iOS
- Update `NavigationCoordinator.handlePush()` to use providers
- Refactor `RouteHolderRegistry` to use dynamic lookup
- Update `AppNavigationView` if needed

### Phase 4: Enable Feature Modules (High Value)
- Create `RestaurantRouteProvider` in feature-restaurant
- Add `UserRouteProvider` in new feature-user module
- Update app layer to inject providers from DI
- Features can now self-register routes

---

## Benefits of This Pattern

| Benefit | Current | Proposed |
|---------|---------|----------|
| **Add new feature** | Modify 8-10 app files | Add 2-3 feature files + 1 DI binding |
| **Feature isolation** | Routes in app module | Routes owned by feature |
| **Code reuse** | Duplicated switch logic | Centralized handler logic |
| **Testability** | Tightly coupled | Handler can be mocked |
| **Parallel development** | Features block app changes | Features work independently |
| **IDE support** | Manual switch tracking | IDE can generate handlers |

---

## Migration Path (Non-Breaking)

1. **Keep existing code** as-is in Phase 1
2. **Add provider interfaces** to core (new interfaces, no changes)
3. **Create handlers** for restaurant feature (new files)
4. **Inject providers** into navigation (add parameter, use default)
5. **Gradually migrate** switch statements to use providers
6. **Deprecate** old methods after all providers added
7. **Full migration**: App layer only knows about `RouteProvider` interface

---

## Comparison: Current vs Proposed

### Adding "Settings" Feature

#### Current Approach (7 files modified)

```
1. core/Destination.kt - Add SettingsDestination
2. core/Routes.kt - Add SettingsRoute
3. iosApp/Route.swift - Add .settings case
4. iosApp/RouteHolderRegistry.swift - Add settingsHolder()
5. iosApp/NavigationCoordinator.swift - Add handleSettings case
6. iosApp/AppNavigationView.swift - Add .settings view case
7. androidApp/AppNavigation.kt - Add composable(settings)
8. androidApp/RouteRegistry.kt - Add when case for SettingsRoute
9. feature-settings/ - Create feature module
```

#### Proposed Approach (3 files)

```
1. feature-settings/SettingsRouteProvider.kt - Implement handler
2. feature-settings/SettingsRouteProvider.swift - Implement handler
3. app/build.gradle.kts - Add feature-settings dependency (automatic injection via DI)
```

**Reduction: 70% fewer files modified! 🎯**

---

## Appendix: External Research Findings

### Production Examples & Real-World Patterns

#### 1. **Google's Official Sample: Now in Android**
- **GitHub**: https://github.com/android/nowinandroid
- **Pattern**: Navigation 3 with `EntryProvider` + API/Impl Module Split
- **Why Study It**: Official Google sample, production-tested, modular architecture

**Key Pattern**:
```kotlin
// feature-restaurant/api
@Serializable
object RestaurantNavKey : NavKey

// feature-restaurant/impl
fun EntryProviderScope<NavKey>.restaurantEntry(navigator: Navigator) {
    entry<RestaurantNavKey> {
        RestaurantScreen(
            onDetailClick = { id -> navigator.navigate(RestaurantDetailKey(id)) }
        )
    }
}

// app
NavHost(navigator) {
    provider { restaurantEntry(navigator) }
    provider { profileEntry(navigator) }
}
```

**Benefits**: No monolithic switch statements, each feature owns entry point, clean dependency inversion.

---

#### 2. **Bitwarden Android**
- **GitHub**: https://github.com/bitwarden/android
- **Pattern**: NavGraphBuilder extensions with custom transitions
- **Why Study It**: Production app handling complex navigation with animations

**Pattern**:
```kotlin
inline fun <reified T : Any> NavGraphBuilder.composableWithSlideTransitions(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        enterTransition = fadeIn,
        exitTransition = fadeOut,
        content = content,
    )
}

// Feature registration
fun NavGraphBuilder.walletNavigation(navController: NavHostController) {
    composableWithSlideTransitions<WalletRoute> { WalletScreen() }
    composableWithSlideTransitions<VaultRoute> { VaultScreen() }
}
```

---

#### 3. **Decompose: Alternative KMP Navigation**
- **GitHub**: https://github.com/arkivanov/Decompose
- **Docs**: https://arkivanov.github.io/Decompose/
- **Why Study It**: True KMP navigation (not just Compose), works with SwiftUI + Compose

**Pattern**:
```kotlin
class DefaultRootComponent(componentContext: ComponentContext) :
    RootComponent, ComponentContext by componentContext {
    
    private val navigation = StackNavigation<Config>()
    
    override val childStack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.RestaurantList,
        childFactory = ::createChild,
    )
    
    private fun createChild(config: Config, ctx: ComponentContext) =
        when (config) {
            is Config.RestaurantList -> RestaurantListChild(restaurantList(ctx))
            is Config.RestaurantDetail -> RestaurantDetailChild(restaurantDetail(ctx, config))
        }
}
```

**Why Better for KMP**: Lifecycle-aware, works with native iOS UI, component-based instead of route-based.

---

#### 4. **FeatureApi Pattern: JetComposeNavMultimodule**
- **GitHub**: https://github.com/mmarashan/JetComposeNavMultimodule
- **Pattern**: Simple FeatureApi interface for graph registration
- **Why Study It**: Easiest pattern to implement, educational reference

**Pattern**:
```kotlin
// core/feature-api
interface FeatureApi {
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier = Modifier
    )
}

fun NavGraphBuilder.register(
    featureApi: FeatureApi,
    navController: NavHostController,
    modifier: Modifier = Modifier
) = featureApi.registerGraph(this, navController, modifier)

// feature-restaurant/impl
class RestaurantFeatureImpl : FeatureApi {
    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable("restaurant_list") {
            RestaurantListScreen(
                onItemClick = { id ->
                    navController.navigate("restaurant_detail/$id")
                }
            )
        }
        
        navGraphBuilder.composable("restaurant_detail/{id}") { backStack ->
            val id = backStack.arguments?.getString("id") ?: ""
            RestaurantDetailScreen(id = id)
        }
    }
}

// app
NavHost(navController, startDestination = "restaurant_list") {
    register(RestaurantFeatureImpl(), navController)
    register(ProfileFeatureImpl(), navController)
}
```

---

### Recommended Pattern for Your Project

#### **Option 1: Navigation 3 EntryProvider (Best Long-Term)**

**When to use**: Building a large, scalable app with many features

**Pros**:
- Official Google pattern
- Fully modular (api/impl split)
- Type-safe routes with serialization
- Easy testing
- Future-proof

**Cons**:
- Requires Kotlin Serialization setup
- Slightly more boilerplate initially

**Implementation time**: 3-4 weeks

---

#### **Option 2: FeatureApi Pattern (Best Short-Term)**

**When to use**: Need scalability quickly, minimal changes to current structure

**Pros**:
- Can implement immediately
- Minimal boilerplate
- Works with current Route/Destination structure
- Easy to understand

**Cons**:
- Not as type-safe as Navigation 3
- Requires manual DI wiring

**Implementation time**: 1-2 weeks

---

#### **Option 3: Decompose (If True iOS-Native UI Needed)**

**When to use**: Need SwiftUI on iOS + Compose on Android, shared navigation logic

**Pros**:
- Works with native iOS UI
- Component lifecycle management
- Best KMP experience
- Scales to desktop/web later

**Cons**:
- Requires architectural rethinking
- Learning curve (component model vs routes)
- New dependency

**Implementation time**: 4-6 weeks (includes rewrite of iOS navigation)

---

## Next Steps

### Option A: Implement Full Pattern (4-6 weeks)
- Implement all 4 phases
- Fully modular architecture
- Support unlimited features

### Option B: Implement Partial Pattern (1-2 weeks)
- Implement Phase 1 (shared contracts) only
- Prepare for future refactoring
- Low risk, establishes patterns

### Option C: Implement Android Only (2 weeks)
- Android gets full feature-based routing
- iOS remains centralized (for now)
- Gradual iOS migration later

### Option D: Start with Feature Layer (1 week)
- Create `SettingsRouteProvider` structure
- Don't integrate yet (in branch)
- Prove pattern works before full refactor

---

## Files to Create/Modify

### To Create
- `core/.../navigation/RouteHandler.kt` ✨
- `core/.../navigation/RouteProvider.kt` ✨
- `core/.../navigation/ScopeFactory.kt` ✨
- `feature-restaurant/RestaurantRouteProvider.kt` ✨ (new abstraction layer)
- `feature-restaurant/RestaurantRouteProvider.swift` ✨ (new abstraction layer)

### To Modify (with compatibility layer)
- `iosApp/.../Navigation/NavigationCoordinator.swift` (add provider support, keep old methods)
- `iosApp/.../Navigation/RouteHolderRegistry.swift` (add provider-based lookup)
- `androidApp/.../AppNavigation.kt` (accept provider list, iterate instead of hardcode)
- `androidApp/.../RouteRegistry.kt` (use handler map instead of when expression)
- `core/.../Route.kt` (add optional handler property)

---

## Risk Assessment

| Phase | Risk | Mitigation |
|-------|------|-----------|
| **Phase 1** | Shared contract changes | Additive only, no breaking changes |
| **Phase 2** | Android refactor | Keep old code path as fallback initially |
| **Phase 3** | iOS refactor | Use protocol extension for compatibility |
| **Phase 4** | Feature module changes | Gradual - one feature at a time |

---

## Success Criteria

✅ Adding a new feature requires modifying only feature module files (+ 1 DI binding)  
✅ App module doesn't import or know about specific feature screens  
✅ Route handling is delegated to feature providers  
✅ Both iOS and Android use same pattern  
✅ Existing features continue working (no breaking changes in Phase 1-2)

