package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `initially loads fresh data`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.refresh()
        delay(DEFAULT_DELAY * 2) // waiting until loading time is complete
        runCurrent()

        assertNotNull(replica.currentState.data)
        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `isn't load data if refreshing is canceled`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.refresh()
        runCurrent()
        replica.cancel()
        delay(DEFAULT_DELAY * 2) // waiting until loading time is complete
        runCurrent()

        assertNull(replica.currentState.data)
    }

    @Test
    fun `returns fresh data if fresh data is present and refresh called`() = runTest {
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

        replica.refresh()
        runCurrent()
        val previousState = replica.currentState
        replica.refresh()
        runCurrent()

        assertEquals(ReplicaProvider.TEST_DATA, previousState.data?.value)
        assertTrue(previousState.hasFreshData)
        val currentState = replica.currentState
        assertEquals(newData, currentState.data?.value)
        assertTrue(currentState.hasFreshData)
    }
}