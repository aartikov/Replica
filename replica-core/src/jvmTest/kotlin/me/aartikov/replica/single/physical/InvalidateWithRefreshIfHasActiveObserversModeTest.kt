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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class InvalidateWithRefreshIfHasActiveObserversModeTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `data refreshed after invalidate call and active observer`() =
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
            val observerHost = TestObserverHost(active = true)
            replica.observe(observerHost)
            replica.invalidate(InvalidationMode.RefreshIfHasActiveObservers)
            runCurrent()

            assertTrue(replica.currentState.hasFreshData)
            assertEquals(2, counter)
        }

    @Test
    fun `data isn't refreshed after invalidate call and inactive observer`() =
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
            replica.invalidate(InvalidationMode.RefreshIfHasActiveObservers)
            runCurrent()

            assertFalse(replica.currentState.hasFreshData)
            assertEquals(1, counter)
        }

    @Test
    fun `data isn't refreshed after invalidate call and active observer became inactive`() =
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
            val observerHost = TestObserverHost(active = true)
            replica.observe(observerHost)
            observerHost.active = false
            replica.invalidate(InvalidationMode.RefreshIfHasActiveObservers)
            runCurrent()

            assertFalse(replica.currentState.hasFreshData)
            assertEquals(1, counter)
        }

    @Test
    fun `data isn't refreshed after invalidate call and active observer is canceled`() =
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
            val observerHost = TestObserverHost(active = true)
            replica.observe(observerHost)
            runCurrent()
            observerHost.cancelCoroutineScope()
            replica.invalidate(InvalidationMode.RefreshIfHasActiveObservers)
            runCurrent()

            assertFalse(replica.currentState.hasFreshData)
            assertEquals(1, counter)
        }
}