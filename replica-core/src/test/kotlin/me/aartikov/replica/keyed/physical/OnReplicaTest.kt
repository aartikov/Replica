package me.aartikov.replica.keyed.physical

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnReplicaTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `makes action on existing replica`() = runTest {
        val replica = replicaProvider.replica()

        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        replica.onReplica(DEFAULT_KEY) { clear() }

        val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
        assertNull(childReplicaState?.data?.value)
    }

    @Test
    fun `create child replica than makes action on it`() = runTest {
        val replica = replicaProvider.replica()

        replica.onReplica(DEFAULT_KEY) { setData(KeyedReplicaProvider.testData(DEFAULT_KEY)) }

        val childReplicaState = replica.getCurrentState(DEFAULT_KEY)
        assertNotNull(childReplicaState?.data?.value)
    }
}