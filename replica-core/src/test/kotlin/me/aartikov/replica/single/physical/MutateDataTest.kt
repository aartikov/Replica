package me.aartikov.replica.single.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.LoadingFailedException
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MutateDataTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `doesn't mutate empty data`() = runTest {
        val newData = "new data"
        val replica = replicaProvider.replica()

        replica.mutateData { newData }

        assertNull(replica.currentState.data?.value)
    }

    @Test
    fun `doesn't mutate empty data with error state`() = runTest {
        val newData = "new data"
        val replica = replicaProvider.replica(
            fetcher = { throw LoadingFailedException() }
        )

        replica.refresh()
        runCurrent()
        replica.mutateData { newData }

        assertNull(replica.currentState.data?.value)
    }

    @Test
    fun `mutates existing data`() = runTest {
        val newData = "new data"
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        replica.mutateData { it + newData }

        assertEquals(ReplicaProvider.TEST_DATA + newData, replica.currentState.data?.value)
    }

    @Test
    fun `mutate data call doesn't cancel loading`() = runTest {
        val newData = "new data"
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.setData("initial data")
        replica.refresh()
        delay(DEFAULT_DELAY / 2)
        replica.mutateData { newData }
        val intermediateState = replica.currentState
        delay((DEFAULT_DELAY / 2) + 1) // loading is completed

        assertEquals(newData, intermediateState.data?.value)
        assertTrue(newData, intermediateState.loading)
        assertEquals("test", replica.currentState.data?.value)
    }
}