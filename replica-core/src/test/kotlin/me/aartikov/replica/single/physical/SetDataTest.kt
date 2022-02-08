package me.aartikov.replica.single.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.MainCoroutineRule
import me.aartikov.replica.single.utils.ReplicaProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetDataTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `sets data initially`() = runTest {
        val newData = "new data"
        val replica = replicaProvider.replica()

        replica.setData(newData)

        assertEquals(newData, replica.currentState.data?.value)
    }

    @Test
    fun `changes existing data`() = runTest {
        val newData = "new data"
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        replica.setData(newData)

        assertEquals(newData, replica.currentState.data?.value)
    }

    @Test
    fun `set data call doesn't cancel loading`() = runTest {
        val newData = "new data"
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.refresh()
        delay(DEFAULT_DELAY / 2)
        replica.setData(newData)
        val intermediateState = replica.currentState
        delay((DEFAULT_DELAY / 2) + 1) // loading is completed

        assertEquals(newData, intermediateState.data?.value)
        assertTrue(newData, intermediateState.loading)
        assertEquals("test", replica.currentState.data?.value)
    }
}