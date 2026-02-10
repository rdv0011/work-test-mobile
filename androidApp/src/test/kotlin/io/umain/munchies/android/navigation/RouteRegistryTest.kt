package io.umain.munchies.android.navigation

import io.umain.munchies.core.lifecycle.RouteLifetime
import org.junit.Before
import org.junit.Test
import org.koin.core.scope.Scope
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RouteRegistryTest {
    private lateinit var registry: RouteRegistry
    private val createdScopes = mutableListOf<Scope>()

    @Before
    fun setUp() {
        registry = RouteRegistry()
        createdScopes.clear()
    }

    @Test
    fun `lifetimeFor creates new RouteLifetime on first access`() {
        val routeId = "detail-123"
        val mockScope = createMockScope()
        
        val lifetime = registry.lifetimeFor(routeId) { mockScope }
        
        assertNotNull(lifetime)
        assertEquals(mockScope, lifetime.scope)
        assertEquals(1, registry.lifetimes.size)
    }

    @Test
    fun `lifetimeFor returns existing RouteLifetime on subsequent access`() {
        val routeId = "detail-123"
        val mockScope1 = createMockScope()
        val mockScope2 = createMockScope()
        
        val lifetime1 = registry.lifetimeFor(routeId) { mockScope1 }
        val lifetime2 = registry.lifetimeFor(routeId) { mockScope2 }
        
        // Same lifetime, not recreated
        assertEquals(lifetime1, lifetime2)
        // Uses the first scope, not the second
        assertEquals(mockScope1, lifetime2.scope)
        assertEquals(1, registry.lifetimes.size)
    }

    @Test
    fun `cleanup removes inactive routes and keeps active ones`() {
        val route1 = "detail-1"
        val route2 = "detail-2"
        val route3 = "detail-3"
        
        registry.lifetimeFor(route1) { createMockScope() }
        registry.lifetimeFor(route2) { createMockScope() }
        registry.lifetimeFor(route3) { createMockScope() }
        
        assertEquals(3, registry.lifetimes.size)
        
        // Keep only route1 and route2, remove route3
        registry.cleanup(setOf(route1, route2))
        
        assertEquals(2, registry.lifetimes.size)
        assertNotNull(registry.lifetimes[route1])
        assertNotNull(registry.lifetimes[route2])
        assertNull(registry.lifetimes[route3])
    }

    @Test
    fun `cleanup with empty set removes all routes`() {
        registry.lifetimeFor("route-1") { createMockScope() }
        registry.lifetimeFor("route-2") { createMockScope() }
        
        registry.cleanup(emptySet())
        
        assertEquals(0, registry.lifetimes.size)
    }

    @Test
    fun `cleanup does nothing when called with same active routes`() {
        val route1 = "detail-1"
        val route2 = "detail-2"
        
        registry.lifetimeFor(route1) { createMockScope() }
        registry.lifetimeFor(route2) { createMockScope() }
        
        val initialLifetime1 = registry.lifetimes[route1]
        val initialLifetime2 = registry.lifetimes[route2]
        
        registry.cleanup(setOf(route1, route2))
        
        assertEquals(2, registry.lifetimes.size)
        assertEquals(initialLifetime1, registry.lifetimes[route1])
        assertEquals(initialLifetime2, registry.lifetimes[route2])
    }

    @Test
    fun `clearAll removes all routes`() {
        registry.lifetimeFor("route-1") { createMockScope() }
        registry.lifetimeFor("route-2") { createMockScope() }
        registry.lifetimeFor("route-3") { createMockScope() }
        
        assertEquals(3, registry.lifetimes.size)
        
        registry.clearAll()
        
        assertEquals(0, registry.lifetimes.size)
    }

    @Test
    fun `multiple route operations maintain correct state`() {
        // Simulate a navigation sequence:
        // 1. Open list
        // 2. Open detail
        // 3. Go back (close detail)
        // 4. Open different detail
        // 5. Go back (close detail)
        
        val listRoute = "list"
        val detail1Route = "detail-1"
        val detail2Route = "detail-2"
        
        registry.lifetimeFor(listRoute) { createMockScope() }
        assertEquals(1, registry.lifetimes.size)
        
        registry.lifetimeFor(detail1Route) { createMockScope() }
        assertEquals(2, registry.lifetimes.size)
        
        registry.cleanup(setOf(listRoute))
        assertEquals(1, registry.lifetimes.size)
        
        registry.lifetimeFor(detail2Route) { createMockScope() }
        assertEquals(2, registry.lifetimes.size)
        
        registry.cleanup(setOf(listRoute))
        assertEquals(1, registry.lifetimes.size)
    }

    private fun createMockScope(): Scope {
        // Create a minimal mock scope for testing
        // In a real scenario, this would be created by Koin
        return object : Scope {
            override val scopeQualifier = null
            override val id: String = "mock-scope-${createdScopes.size}"
            override val logger = null
            override val instanceRegistry = null
            override fun createEagerInstances() {}
            override fun close() {}
            override fun getOrNull(clazz: Class<*>, parameters: Any?, qualifier: Any?) = null
            override fun get(clazz: Class<*>, parameters: Any?, qualifier: Any?) = null
            override fun getAll(clazz: Class<*>, parameters: Any?, qualifier: Any?) = emptyList<Any>()
            override fun getOrNull(clazz: Class<*>) = null
            override fun get(clazz: Class<*>) = null
            override fun getAll(clazz: Class<*>) = emptyList<Any>()
            override fun reloadDefinitions(modules: Any) {}
            override fun unloadDefinitions(modules: Any) {}
            override fun links(other: Any) {}
        }.also { createdScopes.add(it) }
    }
}
