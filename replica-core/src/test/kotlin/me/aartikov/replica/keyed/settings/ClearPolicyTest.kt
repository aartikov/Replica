package me.aartikov.replica.keyed.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.ClearOrder
import me.aartikov.replica.keyed.ClearPolicy
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.currentState
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.utils.MainCoroutineRule
import me.aartikov.replica.utils.VirtualTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)

class ClearPolicyTest {

    private val replicaProvider = KeyedReplicaProvider()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    companion object {
        const val DEFAULT_DELAY = 100L
    }

    @Test
    fun `not removing replica with privileged key with max count setup`() = runTest {
        val maxCount = 2
        val privilegedKey = 0
        val replica = replicaProvider.replica(
            replicaSettings = KeyedReplicaSettings(
                maxCount = maxCount,
                clearPolicy = ClearPolicy(
                    privilegedKeys = setOf(privilegedKey)
                )
            )
        )

        repeat(maxCount + 1) { i ->
            replica.setData(i, KeyedReplicaProvider.testData(i))
        }
        delay(101) // waiting until LimitChildCount.ClearingDebounceTime is passed

        assertEquals(maxCount, replica.currentState.replicaCount)
        assertNotNull(replica.getCurrentState(privilegedKey))
    }

    @Test
    fun `removing replicas with privileged keys if replicas with privileged keys more then maxCount`() =
        runTest {
            val maxCount = 0
            val numOfPrivilegedKeys = 10
            val numOfKeys = 10
            val privilegedKeys = (0..numOfPrivilegedKeys).toSet()
            val replica = replicaProvider.replica(
                replicaSettings = KeyedReplicaSettings(
                    maxCount = maxCount,
                    clearPolicy = ClearPolicy(
                        privilegedKeys = privilegedKeys
                    )
                )
            )

            repeat(numOfPrivilegedKeys + numOfKeys) { i ->
                replica.setData(i, KeyedReplicaProvider.testData(i))
            }

            delay(101) // waiting until LimitChildCount.ClearingDebounceTime is passed

            for (key in privilegedKeys) {
                assertNull(replica.getCurrentState(key))
            }
            assertEquals(maxCount, replica.currentState.replicaCount)
        }

    @Test
    fun `replicas removes by data changing order if it is setup and maxCount is setup`() = runTest {
        val maxCount = 2
        val numOfReplicas = maxCount * 2
        val replica = replicaProvider.replica(
            timeProvider = VirtualTimeProvider(this),
            replicaSettings = KeyedReplicaSettings(
                maxCount = maxCount,
                clearPolicy = ClearPolicy(
                    clearOrder = ClearOrder.ByDataChangingTime
                )
            )
        )

        repeat(numOfReplicas) { i ->
            replica.setData(i, KeyedReplicaProvider.testData(i))
        }
        for (i in numOfReplicas downTo 0) {
            replica.onExistingReplica(i) {
                setData(KeyedReplicaProvider.testData(i))
                delay(DEFAULT_DELAY) // for different observingTime
            }
        }
        delay(101) // waiting until LimitChildCount.ClearingDebounceTime is passed

        assertEquals(maxCount, replica.currentState.replicaCount)
        for (i in 0 until numOfReplicas / 2) {
            assertNull(replica.getCurrentState(i)?.observingState)
        }
        for (i in numOfReplicas / 2 until numOfReplicas) {
            assertNotNull(replica.getCurrentState(i))
        }
    }
}