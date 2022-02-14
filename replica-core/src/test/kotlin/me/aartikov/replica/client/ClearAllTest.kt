package me.aartikov.replica.client

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.ReplicaClientProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClearAllTest {

    private val clientProvider = ReplicaClientProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `all replicas clears`() = runTest {
        val client = clientProvider.client()
        val replicasCount = 10

        repeat(replicasCount) { i ->
            val replica = client.createReplica(
                "testReplica$i",
                settings = ReplicaSettings.WithoutBehaviour,
                fetcher = { "test" }
            )
            replica.refresh()
        }

        runCurrent()
        client.clearAll()

        client.onEachReplica {
            assertNull(currentState.data)
        }
    }

    @Test
    fun `all keyed replicas clears`() = runTest {
        val client = clientProvider.client()
        val keyedReplicasCount = 10
        val childReplicasCount = 10

        repeat(keyedReplicasCount) { i ->
            val keyedReplica = client.createKeyedReplica(
                "testKeyedReplica$i",
                childName = { k -> "testReplica$k" },
                childSettings = { ReplicaSettings.WithoutBehaviour },
                fetcher = { k: Int -> "test$k" }
            )
            repeat(childReplicasCount) { i ->
                keyedReplica.refresh(i)
            }
        }
        runCurrent()
        client.clearAll()

        client.onEachReplica {
            assertNotNull(currentState.data)
        }
    }

    @Test
    fun `all replicas clears when data of replicas is refreshing`() = runTest {
        val client = clientProvider.client()
        val replicasCount = 10

        repeat(replicasCount) {
            val replica = client.createReplica(
                "testReplica$it",
                settings = ReplicaSettings.WithoutBehaviour,
                fetcher = {
                    delay(DEFAULT_DELAY)
                    "test"
                }
            )
            replica.refresh()
        }
        delay(DEFAULT_DELAY - 1) // loading not complete yet
        client.clearAll()

        client.onEachReplica {
            assertNull(currentState.data)
        }
    }
}