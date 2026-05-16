package me.aartikov.replica.client

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.currentState
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.doOnStateCondition
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.VirtualTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class BehaviourDispatcherTest {

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `does not execute stale behaviour when only main scheduler advances until idle`() = runTest {
        val behaviourScheduler = TestCoroutineScheduler()
        val replicaClient = ReplicaClient(
            timeProvider = VirtualTimeProvider(this),
            mainDispatcher = StandardTestDispatcher(testScheduler),
            behaviourDispatcher = StandardTestDispatcher(behaviourScheduler)
        )
        val replica = replicaClient.createReplica(
            name = "test",
            settings = ReplicaSettings(staleTime = DEFAULT_DELAY.milliseconds),
            fetcher = Fetcher { ReplicaProvider.TEST_DATA }
        )

        behaviourScheduler.runCurrent()
        replica.refresh()
        runCurrent()
        behaviourScheduler.runCurrent()

        assertTrue(replica.currentState.hasFreshData)

        testScheduler.advanceUntilIdle()

        assertTrue(replica.currentState.hasFreshData)

        advanceTimeBy((DEFAULT_DELAY + 1).milliseconds)
        behaviourScheduler.advanceTimeBy((DEFAULT_DELAY + 1).milliseconds)
        behaviourScheduler.runCurrent()
        runCurrent()

        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `cancels delayed behaviour job when keyed child replica is removed`() = runTest {
        val behaviourScheduler = TestCoroutineScheduler()
        val replicaClient = ReplicaClient(
            timeProvider = VirtualTimeProvider(this),
            mainDispatcher = StandardTestDispatcher(testScheduler),
            behaviourDispatcher = StandardTestDispatcher(behaviourScheduler)
        )
        var calls = 0
        val replica = replicaClient.createKeyedReplica(
            name = "test",
            childName = { "child_$it" },
            settings = KeyedReplicaSettings<Int, String>(maxCount = 1),
            childSettings = { ReplicaSettings.WithoutBehaviour },
            childBehaviours = { key ->
                if (key == 0) {
                    listOf(
                        ReplicaBehaviour.doOnStateCondition(
                            condition = { true },
                            startDelay = DEFAULT_DELAY.milliseconds
                        ) {
                            calls++
                        }
                    )
                } else {
                    emptyList()
                }
            },
            fetcher = KeyedFetcher { key -> "test_$key" }
        )

        replica.setData(0, "test_0")
        runCurrent()
        behaviourScheduler.runCurrent()

        assertEquals(0, calls)

        replica.setData(1, "test_1")
        runCurrent()
        advanceTimeBy(101.milliseconds)
        runCurrent()

        assertEquals(1, replica.currentState.replicaCount)

        behaviourScheduler.advanceTimeBy((DEFAULT_DELAY + 1).milliseconds)
        behaviourScheduler.runCurrent()
        runCurrent()

        assertEquals(0, calls)
    }
}
