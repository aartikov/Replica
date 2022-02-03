package me.aartikov.replica.single

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.client.ReplicaClient
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ObserverCountTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `is initially no active observers`() = runTest {
        val replicaClient = ReplicaClient()
        val replica = replicaClient.createReplica(
            name = "test",
            settings = ReplicaSettings(staleTime = 1.seconds)
        ) { Any() }

        val activeObserverCount = replica.stateFlow.firstOrNull()
            ?.observingState
            ?.activeObserverCount

        assertEquals(activeObserverCount, 0)
    }
}