package me.aartikov.replica.single.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.VirtualTimeProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class StaleTimeTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @OptIn(ExperimentalTime::class)
    @Test
    fun `is fresh if no stale time setup`() = runTest {
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()

        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `is not fresh initially`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            )
        )

        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `is fresh after refreshing`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = (DEFAULT_DELAY * 2).milliseconds
            )
        )

        replica.refresh()
        runCurrent()

        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `is stale after stale time passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // stale time is passed

        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `is not fresh if data is cleared`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds
            )
        )

        replica.refresh()
        runCurrent()
        replica.clear()
        runCurrent()

        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `is fresh if new data is loaded`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // stale time is passed
        replica.refresh()
        runCurrent()

        assertTrue(replica.currentState.hasFreshData)
    }

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
    fun `cancels stale behaviour job when keyed child replica is removed`() = runTest {
        val behaviourScheduler = TestCoroutineScheduler()
        val replicaClient = ReplicaClient(
            timeProvider = VirtualTimeProvider(this),
            mainDispatcher = StandardTestDispatcher(testScheduler),
            behaviourDispatcher = StandardTestDispatcher(behaviourScheduler)
        )
        val replica = replicaClient.createKeyedReplica(
            name = "test",
            childName = { "child_$it" },
            settings = KeyedReplicaSettings<Int, String>(maxCount = 1),
            childSettings = { ReplicaSettings(staleTime = DEFAULT_DELAY.milliseconds) },
            fetcher = KeyedFetcher { key -> "test_$key" }
        )

        replica.refresh(0)
        runCurrent()
        behaviourScheduler.runCurrent()
        var removedChildReplica: PhysicalReplica<String>? = null
        replica.onReplica(0) {
            removedChildReplica = this
        }

        assertTrue(removedChildReplica?.currentState?.hasFreshData == true)

        replica.refresh(1)
        runCurrent()
        advanceTimeBy(101.milliseconds)
        runCurrent()
        behaviourScheduler.advanceTimeBy((DEFAULT_DELAY + 1).milliseconds)
        behaviourScheduler.runCurrent()
        runCurrent()

        assertTrue(removedChildReplica?.currentState?.hasFreshData == true)
    }
}
