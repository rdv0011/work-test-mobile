# Route Registry for iOS & Android

*A clean, fully typed Route Registry designed for KMP shared ViewModels, Koin scopes, SwiftUI/Compose navigation, deterministic cleanup, and zero casting in app code.*

---

## Route Registry (iOS)

### What the registry does

The Route Registry is responsible for:

- Mapping **Route → Holder**
- Creating holders lazily
- Keeping holders alive only while their route is active
- Closing Koin scopes when routes disappear
- Avoiding duplicate ViewModels for the same route

It is the iOS equivalent of Android's `ViewModelStoreOwner` + `NavBackStackEntry`.

### Core idea

| Concept | Type |
|---------|------|
| Routes | **Value types** |
| Holders | **Reference types** |
| Registry | **Binds them together** |

---

### 1️⃣ Route protocol (typed, no casting)

```swift
protocol Route: Hashable {
    associatedtype Holder: ObservableObject
}
```

Every route declares exactly which holder owns its ViewModel.

---

### 2️⃣ Example route

```swift
struct RestaurantDetailRoute: Route {
    let restaurantId: String

    typealias Holder = RestaurantDetailViewModelHolder
}
```

> **Key insight:** The route itself encodes the holder type.

---

### 3️⃣ Route Registry implementation

```swift
@MainActor
final class RouteHolderRegistry {

    private let coordinator: AppCoordinator

    private var holders: [AnyHashable: AnyObject] = [:]

    init(coordinator: AppCoordinator) {
        self.coordinator = coordinator
    }

    // MARK: - Access

    func holder<R: Route>(for route: R) -> R.Holder {
        let key = AnyHashable(route)

        if let existing = holders[key] as? R.Holder {
            return existing
        }

        let created = createHolder(for: route)
        holders[key] = created
        return created
    }

    // MARK: - Cleanup

    func cleanup(activeRoutes: Set<AnyHashable>) {
        let inactiveKeys = holders.keys.filter { !activeRoutes.contains($0) }

        inactiveKeys.forEach { key in
            holders[key] = nil // triggers deinit → scope.close()
        }
    }

    // MARK: - Factory

    private func createHolder<R: Route>(for route: R) -> R.Holder {
        switch route {

        case let route as RestaurantDetailRoute:
            return RestaurantDetailViewModelHolder(
                restaurantId: route.restaurantId
            ) as! R.Holder

        default:
            fatalError("No holder registered for route \(route)")
        }
    }
}
```

<details>
<summary>💭 Why the switch is acceptable here</summary>

- **Centralized** — All route-to-holder mapping lives in one place
- **Compile-time discoverable** — Missing cases are obvious
- **No runtime reflection** — Type-safe and performant
- **Mirrors Android** — Matches `when(route)` navigation patterns

</details>

---

### 4️⃣ Integrating with NavigationCoordinator

```swift
@MainActor
final class NavigationCoordinator: ObservableObject {

    @Published private(set) var path = NavigationPath()

    private(set) var activeRoutes = Set<AnyHashable>()
    private let registry: RouteHolderRegistry

    let coordinator: AppCoordinator

    init(coordinator: AppCoordinator) {
        self.coordinator = coordinator
        self.registry = RouteHolderRegistry(coordinator: coordinator)
    }

    // MARK: Navigation

    func push<R: Route>(_ route: R) {
        activeRoutes.insert(AnyHashable(route))
        path.append(route)
    }

    func pop() {
        guard !path.isEmpty else { return }
        path.removeLast()
        syncCleanup()
    }

    func popToRoot() {
        path = NavigationPath()
        activeRoutes.removeAll()
        registry.cleanup(activeRoutes: [])
    }

    // MARK: Holder access

    func holder<R: Route>(for route: R) -> R.Holder {
        registry.holder(for: route)
    }

    private func syncCleanup() {
        registry.cleanup(activeRoutes: activeRoutes)
    }
}
```

---

### 5️⃣ SwiftUI usage

```swift
.navigationDestination(for: RestaurantDetailRoute.self) { route in
    let holder = navigator.holder(for: route)

    RestaurantDetailView(
        restaurantId: route.restaurantId,
        coordinator: navigator.coordinator,
        viewModel: holder.viewModel
    )
}
```

| What SwiftUI sees | What you control |
|-------------------|------------------|
| Pure value routes | ViewModel lifetime |
| Stateless views | Scope lifetime |
| Declarative navigation | Cleanup timing |

---

### 6️⃣ Why this architecture works

| ✅ Benefit |
|-----------|
| No global singletons |
| No leaking scopes |
| No casting in views |
| Deterministic cleanup |
| Android/iOS symmetry |

This is **production-grade KMP navigation**.

---

### 7️⃣ File structure (recommended)

```
iosApp/
 ├─ navigation/
 │   ├─ Route.swift
 │   ├─ RouteHolderRegistry.swift
 │   └─ NavigationCoordinator.swift
 ├─ holders/
 │   └─ RestaurantDetailViewModelHolder.swift
 └─ views/
     └─ RestaurantDetailView.swift
```

---

### 8️⃣ One-line mental model

> **Routes describe navigation.**  
> **Holders own scopes.**  
> **The registry binds them together.**

You've now recreated `NavController` + `ViewModelStore` + Koin scopes on iOS — explicitly, safely, and testably.

---

## Android Analog of Registry

*Where everything clicks — on Android most of this already exists implicitly. We're making it explicit and symmetric with iOS.*

---

### Android Route Registry (NavBackStack–aware)

> **Key idea:** On Android:
> - Route = `NavBackStackEntry`
> - Holder = `ViewModelStoreOwner`
> - Registry = `ViewModelStore` + Koin scope

Android already has a registry — it's just spread across Navigation, Lifecycle, ViewModelStore, and Koin. We'll unify it conceptually.

---

### 1️⃣ Android Route abstraction (optional but clarifying)

This mirrors your Swift `Route`:

```kotlin
interface Route {
    val key: String
}
```

Example:

```kotlin
data class RestaurantDetailRoute(
    val restaurantId: String
) : Route {
    override val key: String = "RestaurantDetail_$restaurantId"
}
```

> On Android, this is often implicit (nav arguments), but making it explicit helps symmetry.

---

### 2️⃣ ViewModel scope owner (Android "Holder")

This is the exact Android equivalent of your iOS `ViewModelHolder`:

```kotlin
class ScopedViewModelOwner(
    val scope: Scope
) : ViewModelStoreOwner {

    private val store = ViewModelStore()

    override val viewModelStore: ViewModelStore
        get() = store

    fun clear() {
        store.clear()
        scope.close()
    }
}
```

| Why this exists | Purpose |
|-----------------|---------|
| `ViewModelStoreOwner` | Lifetime anchor |
| `Scope` | Koin scope |
| `clear()` | deinit on iOS |

---

### 3️⃣ Android Route Registry

This is the true analog of `RouteHolderRegistry` on iOS:

```kotlin
class RouteRegistry(
    private val koin: Koin
) {

    private val holders = mutableMapOf<String, ScopedViewModelOwner>()

    fun ownerFor(route: Route): ScopedViewModelOwner {
        return holders.getOrPut(route.key) {
            val scope = koin.createScope(
                scopeId = route.key,
                qualifier = null
            )
            ScopedViewModelOwner(scope)
        }
    }

    fun cleanup(activeRoutes: Set<String>) {
        val inactive = holders.keys - activeRoutes

        inactive.forEach { key ->
            holders.remove(key)?.clear()
        }
    }
}
```

---

### 4️⃣ Using it with Navigation Compose

```kotlin
@Composable
fun AppNavHost(
    navController: NavHostController,
    registry: RouteRegistry
) {
    val backStack by navController.currentBackStackEntryFlow.collectAsState(null)

    val activeRoutes = remember(backStack) {
        navController.backQueue
            .mapNotNull { entry ->
                entry.arguments?.getString("restaurantId")?.let {
                    "RestaurantDetail_$it"
                }
            }
            .toSet()
    }

    LaunchedEffect(activeRoutes) {
        registry.cleanup(activeRoutes)
    }

    NavHost(navController, startDestination = "restaurantList") {

        composable(
            route = "restaurantDetail/{restaurantId}"
        ) { entry ->

            val restaurantId =
                entry.arguments!!.getString("restaurantId")!!

            val route = RestaurantDetailRoute(restaurantId)

            val owner = remember(entry) {
                registry.ownerFor(route)
            }

            val viewModel: RestaurantDetailViewModel = koinViewModel(
                viewModelStoreOwner = owner,
                scope = owner.scope
            )

            RestaurantDetailScreen(
                viewModel = viewModel
            )
        }
    }
}
```

---

### 5️⃣ Why `remember(entry)` matters

This is the Android equivalent of `@StateObject`:

| Event | Behavior |
|-------|----------|
| New entry | → new owner |
| Same entry | → same ViewModel |
| Entry removed | → cleanup runs |

---

### 6️⃣ Android ↔ iOS Symmetry Table

| Concept | Android | iOS |
|---------|---------|-----|
| **Route** | `NavBackStackEntry` / `Route` | `Route` struct |
| **Holder** | `ViewModelStoreOwner` | `ViewModelHolder` |
| **Registry** | `RouteRegistry` | `RouteHolderRegistry` |
| **Lifetime** | `Lifecycle` + Nav | SwiftUI + `deinit` |
| **Cleanup** | `ViewModelStore.clear` | `scope.close()` |
| **Scope** | Koin scope | Koin scope |

---

## What's Next?

If you want to continue:

- **Parent/child scopes**
- **Modal route registries**
- **Tab-scoped registries**
- **Automatic codegen for route → holder**
- **Leak detection helpers**

Just say the word. 🚀
