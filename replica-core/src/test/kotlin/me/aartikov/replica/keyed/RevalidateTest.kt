package me.aartikov.replica.keyed

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RevalidateTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `initially loads fresh data`() = runTest {
        val replica = replicaProvider.replica()

        replica.revalidate(DEFAULT_KEY)
        runCurrent()

        assertNotNull(replica.getCurrentState(DEFAULT_KEY))
        assertTrue(replica.getCurrentState(DEFAULT_KEY)?.hasFreshData == true)
    }

    @Test
    fun `isn't load data if it is canceled`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.revalidate(DEFAULT_KEY)
        runCurrent()
        replica.cancel(DEFAULT_KEY)
        delay(DEFAULT_DELAY * 2) // waiting until loading time is complete

        assertNull(replica.getCurrentState(DEFAULT_KEY)?.data)
    }

    @Test
    fun `returns existing data if there is fresh data and revalidate is called`() = runTest {
        var isFirstRefresh = true
        val newData = "new data"
        val replica = replicaProvider.replica(
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    KeyedReplicaProvider.testData(it)
                } else {
                    newData
                }
            }
        )

        replica.revalidate(0)
        runCurrent()
        val previousState = replica.getCurrentState(DEFAULT_KEY)
        replica.revalidate(0)
        runCurrent()

        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), previousState?.data?.value)
        assertTrue(previousState?.hasFreshData == true)
        val currentState = replica.getCurrentState(DEFAULT_KEY)
        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), currentState?.data?.value)
    }
}