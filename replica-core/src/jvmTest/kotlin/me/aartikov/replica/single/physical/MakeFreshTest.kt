package me.aartikov.replica.single.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class MakeFreshTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `data is fresh after makeFresh call when there is stale data`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until data is stale
        replica.makeFresh()

        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `data is fresh after makeFresh call when there is fresh data`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        runCurrent()
        replica.makeFresh()

        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `data isn't fresh after makeFresh call when there is no data`() = runTest {
        val replica = replicaProvider.replica()

        replica.makeFresh()

        assertFalse(replica.currentState.hasFreshData)
    }
}