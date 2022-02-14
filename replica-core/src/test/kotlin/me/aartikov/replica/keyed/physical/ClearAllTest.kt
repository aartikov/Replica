package me.aartikov.replica.keyed.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.utils.LoadingFailedException
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClearAllTest {

    private val replicaProvider = KeyedReplicaProvider()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `clears all replicas data`() = runTest {
        val numOfReplicas = 10
        val replica = replicaProvider.replica()

        repeat(numOfReplicas) { i ->
            replica.setData(i, KeyedReplicaProvider.testData(i))
        }
        replica.clearAll()

        repeat(numOfReplicas) { i ->
            assertNull(replica.getCurrentState(i)?.data?.value)
        }
    }

    @Test
    fun `clears all replicas error`() = runTest {
        val numOfReplicas = 10
        val replica = replicaProvider.replica(
            fetcher = {
                throw LoadingFailedException()
            }
        )

        repeat(numOfReplicas) { i ->
            replica.refresh(i)
        }
        runCurrent()
        replica.clearAll()

        repeat(numOfReplicas) { i ->
            assertNull(replica.getCurrentState(i)?.error)
        }
    }
}