package me.aartikov.replica.keyed.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.currentState
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MaxCountTest {

    private val replicaProvider = KeyedReplicaProvider()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `no more replicas are stored than maxCount`() = runTest {
        val replicasCount = 10
        val maxCount = 3
        val replica = replicaProvider.replica(
            replicaSettings = KeyedReplicaSettings(
                maxCount = maxCount
            )
        )

        repeat(replicasCount) { i ->
            replica.setData(i, KeyedReplicaProvider.testData(i))
        }
        delay(101) // waiting until LimitChildCount.ClearingDebounceTime is passed

        assertEquals(maxCount, replica.currentState.replicaCount)
    }

    @Test
    fun `no replicas are stored if maxCount is zero`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = KeyedReplicaSettings(
                maxCount = 0
            )
        )

        replica.setData(0, KeyedReplicaProvider.testData(0))
        replica.setData(1, KeyedReplicaProvider.testData(1))
        delay(101) // waiting until LimitChildCount.ClearingDebounceTime is passed

        assertEquals(0, replica.currentState.replicaCount)
    }

    @Test
    fun `no replicas are removed if all replicas have observers and maxCount is setup`() = runTest {
        val maxCount = 0
        val numOfReplicas = 5
        val replica = replicaProvider.replica(
            replicaSettings = KeyedReplicaSettings(
                maxCount = maxCount
            )
        )

        repeat(numOfReplicas) {
            replica.setData(it, KeyedReplicaProvider.testData(it))
            replica.observe(TestScope(), MutableStateFlow(false), MutableStateFlow(it))
        }
        delay(101) // waiting until LimitChildCount.ClearingDebounceTime is passed

        assertEquals(numOfReplicas, replica.currentState.replicaCount)
    }

    @Test
    fun `firstly remove replicas without observers if maxCount is setup`() = runTest {
        val maxCount = 2
        val replica = replicaProvider.replica(
            replicaSettings = KeyedReplicaSettings(
                maxCount = maxCount
            )
        )
        val replicaKeyWithObserver1 = 0
        val replicaKeyWithObserver2 = 1
        val replicaKeyWithoutObserver = 2

        replica.setData(
            replicaKeyWithObserver1,
            KeyedReplicaProvider.testData(replicaKeyWithObserver1)
        )
        replica.observe(
            observerCoroutineScope = TestScope(),
            observerActive = MutableStateFlow(true),
            key = MutableStateFlow(replicaKeyWithObserver1)
        )
        replica.setData(
            replicaKeyWithObserver2,
            KeyedReplicaProvider.testData(replicaKeyWithObserver2)
        )
        replica.observe(
            observerCoroutineScope = TestScope(),
            observerActive = MutableStateFlow(true),
            key = MutableStateFlow(replicaKeyWithObserver2)
        )
        replica.setData(
            replicaKeyWithoutObserver,
            KeyedReplicaProvider.testData(replicaKeyWithoutObserver)
        )
        delay(101) // waiting until LimitChildCount.ClearingDebounceTime is passed

        assertEquals(maxCount, replica.currentState.replicaCount)
        assertNotNull(replica.getCurrentState(replicaKeyWithObserver1))
        assertNotNull(replica.getCurrentState(replicaKeyWithObserver2))
    }
}