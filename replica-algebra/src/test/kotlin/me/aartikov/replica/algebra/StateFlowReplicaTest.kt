package me.aartikov.replica.algebra

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.algebra.utils.MainCoroutineRule
import me.aartikov.replica.algebra.utils.ReplicaProvider
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StateFlowReplicaTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `receives new data initially`() = runTest {
        val stateFlow = MutableStateFlow(ReplicaProvider.TEST_DATA)

        val stateFlowReplica = stateFlowReplica(stateFlow)
        val actualData = stateFlowReplica.getData()

        assertEquals(stateFlow.value, actualData)
    }

    @Test
    fun `observes data initially`() = runTest {
        val stateFlow = MutableStateFlow(ReplicaProvider.TEST_DATA)

        val stateFlowReplica = stateFlowReplica(stateFlow)
        val observer = stateFlowReplica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()

        assertEquals(Loadable(data = ReplicaProvider.TEST_DATA), observer.currentState)
    }

    @Test
    fun `observes new data when in stateFlow changed data`() = runTest {
        val stateFlow = MutableStateFlow(ReplicaProvider.TEST_DATA)
        val newData = "newData"

        val stateFlowReplica = stateFlowReplica(stateFlow)
        val observer = stateFlowReplica.observe(TestScope(), MutableStateFlow(true))
        stateFlow.value = newData
        runCurrent()

        assertEquals(newData, observer.currentState.data)
    }
}