package me.aartikov.replica.keyed.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetDataTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `sets data in child replica initially`() = runTest {
        val replica = replicaProvider.replica()

        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))

        val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), childReplicaState?.data?.value)
    }

    @Test
    fun `changes existing data in child replica`() = runTest {
        val replica = replicaProvider.replica()

        replica.refresh(DEFAULT_KEY)
        runCurrent()
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))

        val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), childReplicaState?.data?.value)
    }

    @Test
    fun `set data in child replica call doesn't cancel loading`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.refresh(DEFAULT_KEY)
        delay(DEFAULT_DELAY / 2)
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        val intermediateState = replica.getCurrentState(DEFAULT_KEY)
        delay((DEFAULT_DELAY / 2) + 1) // loading is completed

        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), intermediateState?.data?.value)
        assertTrue(intermediateState?.loading == true)
        assertEquals("test", replica.getCurrentState(DEFAULT_KEY)?.data?.value)
    }
}