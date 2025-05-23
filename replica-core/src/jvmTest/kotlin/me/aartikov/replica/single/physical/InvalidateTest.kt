package me.aartikov.replica.single.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.TestObserverHost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class InvalidateTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `data is stale after invalidate call when there is fresh data`() = runTest {
        val replica = replicaProvider.replica()

        replica.refresh()
        delay(1) // waiting until data is loaded
        replica.invalidate(InvalidationMode.DontRefresh)

        assertEquals(ReplicaProvider.TEST_DATA, replica.currentState.data?.value)
        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `no data after invalidate call if there is no data`() = runTest {
        val replica = replicaProvider.replica()

        replica.invalidate(InvalidationMode.DontRefresh)

        assertFalse(replica.currentState.hasFreshData)
        assertNull(replica.currentState.data?.value)
    }

    @Test
    fun `data is stale after invalidate call when there is stale data`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until data is stale
        replica.invalidate()

        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `data is refreshed after invalidate call with RefreshAlways mode`() = runTest {
        var counter = 0
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(staleTime = null),
            fetcher = {
                counter++
                ReplicaProvider.TEST_DATA
            }
        )

        replica.refresh()
        delay(1) // waiting until data is loaded
        replica.invalidate(InvalidationMode.RefreshAlways)
        runCurrent()

        assertTrue(replica.currentState.hasFreshData)
        assertEquals(2, counter)
    }

    @Test
    fun `data isn't refreshed after invalidate call with DontRefresh mode`() = runTest {
        var counter = 0
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(staleTime = null),
            fetcher = {
                counter++
                ReplicaProvider.TEST_DATA
            }
        )

        replica.refresh()
        delay(1) // waiting until data is loaded
        replica.invalidate(InvalidationMode.DontRefresh)
        runCurrent()

        assertFalse(replica.currentState.hasFreshData)
        assertEquals(1, counter)
    }

    @Test
    fun `data isn't refreshed after invalidate call with RefreshIfHasObservers mode and no observers`() =
        runTest {
            var counter = 0
            val replica = replicaProvider.replica(
                replicaSettings = ReplicaSettings(
                    staleTime = DEFAULT_DELAY.milliseconds
                ),
                fetcher = {
                    counter++
                    ReplicaProvider.TEST_DATA
                }
            )

            replica.refresh()
            delay(1) // waiting until data is loaded
            replica.invalidate(InvalidationMode.RefreshIfHasObservers)
            runCurrent()

            assertFalse(replica.currentState.hasFreshData)
            assertEquals(1, counter)
        }

    @Test
    fun `data refreshed after invalidate call with RefreshIfHasObservers mode and inactive observer is added`() =
        runTest {
            var counter = 0
            val replica = replicaProvider.replica(
                replicaSettings = ReplicaSettings(
                    staleTime = DEFAULT_DELAY.milliseconds,
                    revalidateOnActiveObserverAdded = false
                ),
                fetcher = {
                    counter++
                    ReplicaProvider.TEST_DATA
                }
            )

            replica.refresh()
            delay(1) // waiting until data is loaded
            val observerHost = TestObserverHost(active = false)
            replica.observe(observerHost)
            replica.invalidate(InvalidationMode.RefreshIfHasObservers)
            runCurrent()

            assertTrue(replica.currentState.hasFreshData)
            assertEquals(2, counter)
        }

    @Test
    fun `data isn't refreshed after invalidate call with RefreshIfHasObservers mode and inactive observer is canceled`() =
        runTest {
            var counter = 0
            val replica = replicaProvider.replica(
                replicaSettings = ReplicaSettings(
                    staleTime = DEFAULT_DELAY.milliseconds,
                    revalidateOnActiveObserverAdded = false
                ),
                fetcher = {
                    counter++
                    ReplicaProvider.TEST_DATA
                }
            )

            replica.refresh()
            delay(1) // waiting until data is loaded
            val observerHost = TestObserverHost(active = false)
            replica.observe(observerHost)
            runCurrent()
            observerHost.cancelCoroutineScope()
            replica.invalidate(InvalidationMode.RefreshIfHasObservers)
            runCurrent()

            assertFalse(replica.currentState.hasFreshData)
            assertEquals(1, counter)
        }
}