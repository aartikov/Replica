package me.aartikov.replica.keyed.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class InvalidateTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `data is stale after invalidate call when there is fresh data`() =
        runTest {
            val replica = replicaProvider.replica()

            replica.refresh(DEFAULT_KEY)
            runCurrent()
            replica.invalidate(DEFAULT_KEY, InvalidationMode.DontRefresh)

            val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
            assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), childReplicaState?.data?.value)
            assertFalse(childReplicaState?.hasFreshData == true)
        }

    @Test
    fun `data is stale after invalidate call when there is stale data`() =
        runTest {
            val replica = replicaProvider.replica(
                childReplicaSettings = {
                    ReplicaSettings(
                        staleTime = DEFAULT_DELAY.milliseconds
                    )
                }
            )

            replica.refresh(DEFAULT_KEY)
            delay(DEFAULT_DELAY + 1) // waiting until data is stale
            replica.invalidate(DEFAULT_KEY)

            assertFalse(replica.getCurrentState(DEFAULT_KEY)?.hasFreshData == true)
        }

    @Test
    fun `data is refreshed after invalidate call with RefreshAlways mode`() =
        runTest {
            var counter = 0
            val replica = replicaProvider.replica(
                childReplicaSettings = {
                    ReplicaSettings(staleTime = DEFAULT_DELAY.milliseconds)
                },
                fetcher = {
                    counter++
                    KeyedReplicaProvider.testData(it)
                }
            )

            replica.refresh(DEFAULT_KEY)
            replica.invalidate(DEFAULT_KEY, InvalidationMode.RefreshAlways)
            runCurrent()

            assertTrue(replica.getCurrentState(DEFAULT_KEY)?.hasFreshData == true)
            assertEquals(2, counter)
        }

    @Test
    fun `data isn't refreshed after invalidate call with DontRefresh mode`() =
        runTest {
            var counter = 0
            val replica = replicaProvider.replica(
                childReplicaSettings = {
                    ReplicaSettings(staleTime = DEFAULT_DELAY.milliseconds)
                },
                fetcher = {
                    counter++
                    KeyedReplicaProvider.testData(it)
                }
            )

            replica.refresh(DEFAULT_KEY)
            replica.invalidate(DEFAULT_KEY, InvalidationMode.DontRefresh)
            runCurrent()

            assertTrue(replica.getCurrentState(DEFAULT_KEY)?.hasFreshData == true)
            assertEquals(1, counter)
        }

    @Test
    fun `data isn't refreshed after invalidate call with RefreshIfHasObservers mode and no observers`() =
        runTest {
            var counter = 0
            val replica = replicaProvider.replica(
                childReplicaSettings = {
                    ReplicaSettings(staleTime = DEFAULT_DELAY.milliseconds)
                },
                fetcher = {
                    counter++
                    KeyedReplicaProvider.testData(it)
                }
            )

            replica.refresh(DEFAULT_KEY)
            replica.invalidate(DEFAULT_KEY, InvalidationMode.RefreshIfHasObservers)
            runCurrent()

            assertTrue(replica.getCurrentState(DEFAULT_KEY)?.hasFreshData == true)
            assertEquals(1, counter)
        }
}