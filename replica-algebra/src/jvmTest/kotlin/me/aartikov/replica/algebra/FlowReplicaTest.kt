package me.aartikov.replica.algebra

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.algebra.normal.flowReplica
import me.aartikov.replica.algebra.utils.MainCoroutineRule
import me.aartikov.replica.algebra.utils.ReplicaProvider
import me.aartikov.replica.algebra.utils.TestObserverHost
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowReplicaTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `returns data from flow by getData`() = runTest {
        val flow = MutableStateFlow(ReplicaProvider.TEST_DATA)

        val flowReplica = flowReplica(flow)
        val actualData = flowReplica.getData()

        assertEquals(flow.value, actualData)
    }

    @Test
    fun `observes initial value`() = runTest {
        val flow = MutableStateFlow(ReplicaProvider.TEST_DATA)

        val flowReplica = flowReplica(flow)
        val observerHost = TestObserverHost(active = true)
        val observer = flowReplica.observe(observerHost)
        runCurrent()

        assertEquals(Loadable(data = ReplicaProvider.TEST_DATA), observer.currentState)
    }

    @Test
    fun `observes new data when data in flow is changed`() = runTest {
        val flow = MutableStateFlow(ReplicaProvider.TEST_DATA)
        val newData = "newData"

        val flowReplica = flowReplica(flow)
        val observerHost = TestObserverHost(active = true)
        val observer = flowReplica.observe(observerHost)
        flow.value = newData
        runCurrent()

        assertEquals(newData, observer.currentState.data)
    }
}