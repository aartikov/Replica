package me.aartikov.replica.single.optimistic_updates

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommitOptimisticUpdateTest {

    private val replicaProvider = ReplicaProvider()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    companion object {
        private const val KEY = "key"
    }

    @Test
    fun `changes data with success`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        replica.beginOptimisticUpdate(optimisticUpdate, KEY)
        replica.commitOptimisticUpdate(KEY)

        assertEquals(
            emptyMap<Any, OptimisticUpdate<String>>(),
            replica.currentState.data?.optimisticUpdates
        )
        assertEquals(
            newData,
            replica.currentState.data?.value
        )
    }

    @Test
    fun `no changes data if there is no data`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()

        replica.beginOptimisticUpdate(optimisticUpdate, KEY)
        replica.commitOptimisticUpdate(KEY)

        assertNull(replica.currentState.data?.optimisticUpdates)
        assertNull(replica.currentState.data?.value)
    }
}