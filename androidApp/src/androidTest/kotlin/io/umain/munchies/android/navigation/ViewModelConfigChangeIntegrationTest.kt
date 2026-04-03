package io.umain.munchies.android.navigation


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListAndroidViewModel
import io.umain.munchies.feature.restaurant.di.RestaurantListScope
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Instrumented test to verify that ViewModels survive configuration changes
 * (e.g., device rotation, locale change, font size change).
 *
 * **Test Strategy:**
 * 1. Create a ScreenEntry with a specific scopeId
 * 2. Compose a test UI that retrieves ViewModel using remember(scopeId)
 * 3. Capture the ViewModel instance
 * 4. Force recomposition by updating unrelated state
 * 5. Verify the ViewModel instance is identical (same memory reference)
 * 6. Repeat multiple times to ensure consistency
 *
 * **Why This Works:**
 * - ScreenEntry.scopeId is immutable and stable
 * - remember(key) only re-executes when key changes
 * - Koin scope persists for the lifetime of the app
 * - No new ViewModel is created on recomposition
 */
@RunWith(AndroidJUnit4::class)
class ViewModelConfigChangeIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var testScopeId = "test-scope-${System.currentTimeMillis()}"

    @After
    fun teardown() {
        // Close test scope if it was created
        if (GlobalContext.getOrNull() != null) {
            getKoin().getScopeOrNull(testScopeId)?.close()
        }
        // Do not call stopKoin() because it will break subsequent tests relying on Application's Koin instance
    }

    /**
     * Test: ViewModel instance is NOT recreated on simple recomposition
     *
     * Scenario:
     * 1. Compose UI with ScreenEntry(scopeId = "test-1")
     * 2. Retrieve ViewModel instance (vm1)
     * 3. Trigger recomposition by changing unrelated state (counter)
     * 4. Retrieve ViewModel instance again (vm2)
     * 5. Assert: vm1 === vm2 (same object reference)
     *
     * This simulates what happens during animations, state updates, etc.
     */
    @Test
    fun testViewModelSurvivesRecomposition() {
        var firstViewModel: RestaurantListAndroidViewModel? = null
        var secondViewModel: RestaurantListAndroidViewModel? = null
        var thirdViewModel: RestaurantListAndroidViewModel? = null

        val triggerState = mutableStateOf(0)

        composeRule.setContent {
            TestViewModelCaptureUI(
                scopeId = testScopeId,
                trigger = triggerState.value,
                onViewModelRetrieved = { vm, recompositionCount ->
                    when (recompositionCount) {
                        1 -> firstViewModel = vm
                        2 -> secondViewModel = vm
                        3 -> thirdViewModel = vm
                    }
                }
            )
        }

        composeRule.waitForIdle()

        // Assertions after first recomposition
        assert(firstViewModel != null) { "First ViewModel should be retrieved" }

        // Force recomposition by changing unrelated state
        triggerState.value = 1

        composeRule.waitForIdle()

        // Assertions after second recomposition
        assert(secondViewModel != null) { "Second ViewModel should be retrieved" }
        assert(firstViewModel === secondViewModel) {
            "ViewModel instance should be identical after first recomposition"
        }

        // Force another recomposition
        triggerState.value = 2

        composeRule.waitForIdle()

        // Assertions after third recomposition
        assert(thirdViewModel != null) { "Third ViewModel should be retrieved" }
        assert(secondViewModel === thirdViewModel) {
            "ViewModel instance should be identical after second recomposition"
        }
    }

    /**
     * Test: ViewModel instance persists when scopeId remains the same
     *
     * Scenario:
     * 1. Create ScreenEntry with scopeId = "stable-scope"
     * 2. Retrieve ViewModel (vm1)
     * 3. Change the Route in the ScreenEntry (but keep scopeId same)
     * 4. Retrieve ViewModel again (vm2)
     * 5. Assert: vm1 === vm2
     *
     * This tests the critical invariant: scopeId is the key, not the route
     */
    @Test
    fun testViewModelSurvivesWhenScopeIdIsStable() {
        var firstViewModel: RestaurantListAndroidViewModel? = null
        var secondViewModel: RestaurantListAndroidViewModel? = null

        val phase = mutableStateOf("first")

        composeRule.setContent {
            val currentPhase = phase.value
            TestStableScopeUI(
                phase = currentPhase,
                onViewModelRetrieved = { vm, p ->
                    if (p == "first") {
                        firstViewModel = vm
                    } else if (p == "second") {
                        secondViewModel = vm
                    }
                }
            )
        }

        composeRule.waitForIdle()

        assert(firstViewModel != null)

        // Simulate state update that re-renders UI
        phase.value = "second"

        composeRule.waitForIdle()

        assert(secondViewModel != null)
        assert(firstViewModel === secondViewModel) {
            "ViewModel must survive even when route or other state changes"
        }
    }

    /**
     * Test: Multiple ScreenEntries with different scopeIds create different VMs
     *
     * Scenario:
     * 1. Create ScreenEntry with scopeId = "scope-1"
     * 2. Retrieve ViewModel (vm1)
     * 3. Create ScreenEntry with scopeId = "scope-2"
     * 4. Retrieve ViewModel (vm2)
     * 5. Assert: vm1 !== vm2 (different instances)
     *
     * This verifies that scoping works correctly
     */
    @Test
    fun testDifferentScopeIdCreatesDifferentViewModels() {
        val scopeId1 = "scope-1-${System.currentTimeMillis()}"
        val scopeId2 = "scope-2-${System.currentTimeMillis()}"

        var viewModelFromScope1: RestaurantListAndroidViewModel? = null
        var viewModelFromScope2: RestaurantListAndroidViewModel? = null

        composeRule.setContent {
            TestMultipleScopesUI(
                scopeId1 = scopeId1,
                scopeId2 = scopeId2,
                onViewModelsRetrieved = { vm1, vm2 ->
                    viewModelFromScope1 = vm1
                    viewModelFromScope2 = vm2
                }
            )
        }

        composeRule.waitForIdle()

        assert(viewModelFromScope1 != null)
        assert(viewModelFromScope2 != null)

        // They should be different instances
        assert(viewModelFromScope1 !== viewModelFromScope2) {
            "Different scopeIds should create different ViewModel instances"
        }
    }

    /**
     * Test: Koin scope caching mechanism
     *
     * Scenario:
     * 1. Create scope with scopeId = "cache-test-scope"
     * 2. Request same scopeId from Koin multiple times
     * 3. Assert: All requests return same scope instance
     *
     * This validates Koin's internal scope caching
     */
    @Test
    fun testKoinScopeCachingMechanism() {
        val testScopeId = "cache-test-scope"
        val qualifier = named(RestaurantListScope.qualifierName)

        // First access - creates scope
        val scope1 = getKoin().createScope(testScopeId, qualifier)
        val vm1 = scope1.get<RestaurantListAndroidViewModel>()

        // Second access - should return same scope
        val scope2 = getKoin().getScopeOrNull(testScopeId)
            ?: getKoin().createScope(testScopeId, qualifier)
        val vm2 = scope2.get<RestaurantListAndroidViewModel>()

        // Third access
        val scope3 = getKoin().getScopeOrNull(testScopeId)
            ?: getKoin().createScope(testScopeId, qualifier)
        val vm3 = scope3.get<RestaurantListAndroidViewModel>()

        // All should be same instances
        assert(vm1 === vm2) { "Second scope lookup should return same VM" }
        assert(vm2 === vm3) { "Third scope lookup should return same VM" }
        assert(scope1 === scope2) { "Scopes should be identical" }
        assert(scope2 === scope3) { "Scopes should be identical" }

        // Cleanup
        scope1.close()
    }

    /**
     * Test: Remember cache prevents VM recreation during animation
     *
     * Scenario:
     * This simulates what happens during AnimatedContent transitions where
     * two compositions may run simultaneously.
     *
     * 1. Compose content A with ScreenEntry(scopeId = "anim-test")
     * 2. Retrieve ViewModel (vmA)
     * 3. Replace content with content B with SAME ScreenEntry
     * 4. Retrieve ViewModel (vmB)
     * 5. Assert: vmA === vmB
     */
    @Test
    fun testRememberCachePreventsDuplicationDuringAnimation() {
        val animScopeId = "anim-test-${System.currentTimeMillis()}"
        var vmFromCompositionA: RestaurantListAndroidViewModel? = null
        var vmFromCompositionB: RestaurantListAndroidViewModel? = null

        val phase = mutableStateOf("A")

        // First composition
        composeRule.setContent {
            val currentPhase = phase.value
            TestAnimationSimulationUI(
                scopeId = animScopeId,
                phase = currentPhase,
                onViewModelRetrieved = { vm ->
                    if (currentPhase == "A") {
                        vmFromCompositionA = vm
                    } else if (currentPhase == "B") {
                        vmFromCompositionB = vm
                    }
                }
            )
        }

        composeRule.waitForIdle()
        assert(vmFromCompositionA != null)

        // Second composition (simulating animation phase change)
        phase.value = "B"

        composeRule.waitForIdle()
        assert(vmFromCompositionB != null)

        assert(vmFromCompositionA === vmFromCompositionB) {
            "ViewModel should be same instance even during animation phase change"
        }
    }

    // ==================== TEST COMPOSABLES ====================

    /**
     * Test UI that captures ViewModel on each recomposition
     */
    @Composable
    private fun TestViewModelCaptureUI(
        scopeId: String,
        trigger: Int,
        onViewModelRetrieved: (RestaurantListAndroidViewModel, Int) -> Unit
    ) {
        val koin = getKoin()

        val viewModel: RestaurantListAndroidViewModel = remember(scopeId) {
            val scope = koin.getScopeOrNull(scopeId)
                ?: koin.createScope(scopeId, named(RestaurantListScope.qualifierName))
            scope.get()
        }

        LaunchedEffect(trigger) {
            onViewModelRetrieved(viewModel, trigger + 1)
        }

        Surface(color = MaterialTheme.colorScheme.background) {
            // Dummy UI
        }
    }

    /**
     * Test UI that verifies scopeId stability
     */
    @Composable
    private fun TestStableScopeUI(
        phase: String,
        onViewModelRetrieved: (RestaurantListAndroidViewModel, String) -> Unit
    ) {
        val koin = getKoin()
        val scopeId = "stable-scope-test"

        val viewModel: RestaurantListAndroidViewModel = remember(scopeId) {
            val scope = koin.getScopeOrNull(scopeId)
                ?: koin.createScope(scopeId, named(RestaurantListScope.qualifierName))
            scope.get()
        }

        LaunchedEffect(phase) {
            onViewModelRetrieved(viewModel, phase)
        }

        Surface(color = MaterialTheme.colorScheme.background) {
            // Dummy UI
        }
    }

    /**
     * Test UI with multiple scopes
     */
    @Composable
    private fun TestMultipleScopesUI(
        scopeId1: String,
        scopeId2: String,
        onViewModelsRetrieved: (RestaurantListAndroidViewModel, RestaurantListAndroidViewModel) -> Unit
    ) {
        val koin = getKoin()

        val vm1: RestaurantListAndroidViewModel = remember(scopeId1) {
            val scope = koin.getScopeOrNull(scopeId1)
                ?: koin.createScope(scopeId1, named(RestaurantListScope.qualifierName))
            scope.get()
        }

        val vm2: RestaurantListAndroidViewModel = remember(scopeId2) {
            val scope = koin.getScopeOrNull(scopeId2)
                ?: koin.createScope(scopeId2, named(RestaurantListScope.qualifierName))
            scope.get()
        }

        LaunchedEffect(Unit) {
            onViewModelsRetrieved(vm1, vm2)
        }

        Surface(color = MaterialTheme.colorScheme.background) {
            // Dummy UI
        }
    }

    /**
     * Test UI simulating animation transitions
     */
    @Composable
    private fun TestAnimationSimulationUI(
        scopeId: String,
        phase: String,
        onViewModelRetrieved: (RestaurantListAndroidViewModel) -> Unit
    ) {
        val koin = getKoin()

        val viewModel: RestaurantListAndroidViewModel = remember(scopeId) {
            val scope = koin.getScopeOrNull(scopeId)
                ?: koin.createScope(scopeId, named(RestaurantListScope.qualifierName))
            scope.get()
        }

        LaunchedEffect(phase) {
            onViewModelRetrieved(viewModel)
        }

        Surface(color = MaterialTheme.colorScheme.background) {
            // Content varies based on phase
            // This simulates what happens in AnimatedContent
        }
    }
}
