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
class RefreshTest {

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

        replica.refresh(DEFAULT_KEY)
        runCurrent()

        val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
        assertNotNull(childReplicaState?.data)
        assertTrue(childReplicaState?.hasFreshData == true)
    }

    @Test
    fun `isn't load data if refreshing is canceled`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.refresh(DEFAULT_KEY)
        runCurrent()
        replica.cancel(DEFAULT_KEY)
        delay(DEFAULT_DELAY * 2) // waiting until loading time is complete

        assertNull(replica.getCurrentState(DEFAULT_KEY)?.data)
    }
}