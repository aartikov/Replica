package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.LoadingFailedException
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RevalidateTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `initially loads fresh data`() = runTest {
        val replica = replicaProvider.replica()

        replica.revalidate()
        runCurrent()

        assertNotNull(replica.currentState.data)
        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `isn't load data if it is canceled`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.revalidate()
        runCurrent()
        replica.cancel()
        delay(DEFAULT_DELAY * 2) // waiting until loading time is complete

        assertNull(replica.currentState.data)
    }

    @Test
    fun `returns existing data if there is fresh data and revalidate is called`() = runTest {
        var isFirstRefresh = true
        val newData = "new data"
        val replica = replicaProvider.replica(
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else {
                    newData
                }
            }
        )

        replica.revalidate()
        runCurrent()
        val previousState = replica.currentState
        replica.revalidate()
        runCurrent()

        assertEquals(ReplicaProvider.TEST_DATA, previousState.data?.value)
        assertTrue(previousState.hasFreshData)
        val currentState = replica.currentState
        assertEquals(ReplicaProvider.TEST_DATA, currentState.data?.value)
    }

    @Test
    fun `returns fresh data if there is stale data and revalidate is called`() = runTest {
        var isFirstRefresh = true
        val newData = "new data"
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else {
                    newData
                }
            }
        )

        replica.revalidate()
        delay(DEFAULT_DELAY + 1) // waiting until data is stale
        replica.revalidate()
        runCurrent()

        val currentState = replica.currentState
        assertEquals(newData, currentState.data?.value)
        assertTrue(currentState.hasFreshData)
    }

    @Test
    fun `returns fresh data if there is error state and revalidate is called`() = runTest {
        var isFirstRefresh = true
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    throw LoadingFailedException()
                } else {
                    ReplicaProvider.TEST_DATA
                }
            }
        )

        replica.revalidate()
        runCurrent()
        replica.revalidate()
        runCurrent()

        val currentState = replica.currentState
        assertEquals(ReplicaProvider.TEST_DATA, currentState.data?.value)
        assertTrue(currentState.hasFreshData)
    }
}