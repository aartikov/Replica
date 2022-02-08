package me.aartikov.replica.single

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.MainCoroutineRule
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
        val replica = replicaProvider.replica()

        replica.refresh()
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
        val previousData = replica.currentState
        replica.refresh()
        runCurrent()

        assertEquals(ReplicaProvider.TEST_DATA, previousData.data?.value)
        assertTrue(previousData.hasFreshData)
        val currentState = replica.currentState
        assertEquals(newData, currentState.data?.value)
        assertTrue(currentState.hasFreshData)
    }
}