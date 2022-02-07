package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.MainCoroutineRule
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
        delay(30.seconds)

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
        delay(DEFAULT_DELAY)

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
        delay(DEFAULT_DELAY + 1)

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
        delay(DEFAULT_DELAY)
        replica.clear()
        delay(DEFAULT_DELAY)

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
        delay(DEFAULT_DELAY + 1)
        replica.refresh()
        delay(DEFAULT_DELAY / 2)

        assertTrue(replica.currentState.hasFreshData)
    }
}