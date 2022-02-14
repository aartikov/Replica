package me.aartikov.replica.keyed.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.utils.LoadingFailedException
import me.aartikov.replica.single.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MutateDataTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `doesn't mutate empty data in child replica`() = runTest {
        val replica = replicaProvider.replica()

        replica.mutateData(DEFAULT_KEY) { KeyedReplicaProvider.testData(DEFAULT_KEY) }

        val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
        assertNull(childReplicaState?.data?.value)
    }

    @Test
    fun `doesn't mutate empty data in child replica with error state`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = { throw LoadingFailedException() }
        )

        replica.refresh(DEFAULT_KEY)
        runCurrent()
        replica.mutateData(DEFAULT_KEY) { KeyedReplicaProvider.testData(DEFAULT_KEY) }

        val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
        assertNull(childReplicaState?.data?.value)
    }

    @Test
    fun `mutates existing data in child replica `() = runTest {
        val newData = "new data"
        val replica = replicaProvider.replica()

        replica.refresh(DEFAULT_KEY)
        runCurrent()
        replica.mutateData(DEFAULT_KEY) { it + newData }

        val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
        assertEquals(
            KeyedReplicaProvider.testData(DEFAULT_KEY) + newData,
            childReplicaState?.data?.value
        )
    }
}