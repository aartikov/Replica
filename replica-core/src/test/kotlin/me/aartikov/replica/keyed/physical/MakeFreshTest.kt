package me.aartikov.replica.keyed.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class MakeFreshTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `data is fresh after makeFresh call when there is stale data`() = runTest {
        val replica = replicaProvider.replica(
            childReplicaSettings = {
                ReplicaSettings(
                    staleTime = DEFAULT_DELAY.milliseconds
                )
            }
        )

        replica.refresh(DEFAULT_KEY)
        delay(DEFAULT_DELAY + 1) // waiting until data is stale
        replica.makeFresh(DEFAULT_KEY)

        assertTrue(replica.getCurrentState(DEFAULT_KEY)?.hasFreshData == true)
    }

    @Test
    fun `data is fresh after makeFresh call when there is fresh data`() = runTest {
        val replica = replicaProvider.replica(
            childReplicaSettings = {
                ReplicaSettings(
                    staleTime = DEFAULT_DELAY.milliseconds
                )
            }
        )

        replica.refresh(DEFAULT_KEY)
        runCurrent()
        replica.makeFresh(DEFAULT_KEY)

        assertTrue(replica.getCurrentState(DEFAULT_KEY)?.hasFreshData == true)
    }

    @Test
    fun `data isn't fresh after makeFresh call when there is no data`() = runTest {
        val replica = replicaProvider.replica()

        replica.makeFresh(DEFAULT_KEY)

        assertFalse(replica.getCurrentState(DEFAULT_KEY)?.hasFreshData == false)
    }
}