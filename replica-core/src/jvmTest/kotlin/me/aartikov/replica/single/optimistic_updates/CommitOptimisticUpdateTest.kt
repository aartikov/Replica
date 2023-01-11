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

    @Test
    fun `changes data with success`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        replica.beginOptimisticUpdate(optimisticUpdate)
        replica.commitOptimisticUpdate(optimisticUpdate)

        assertEquals(
            emptyList<OptimisticUpdate<String>>(),
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

        replica.beginOptimisticUpdate(optimisticUpdate)
        replica.commitOptimisticUpdate(optimisticUpdate)

        assertNull(replica.currentState.data?.optimisticUpdates)
        assertNull(replica.currentState.data?.value)
    }
}