package me.aartikov.replica.single.optimistic_updates

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RollbackOptimisticUpdate {

    private val replicaProvider = ReplicaProvider()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `rollbacks optimistic update`() = runTest {
        val optimisticUpdate = OptimisticUpdate<String> { "new data" }
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        replica.beginOptimisticUpdate(optimisticUpdate)
        replica.rollbackOptimisticUpdate(optimisticUpdate)

        assertEquals(
            emptyList<OptimisticUpdate<String>>(),
            replica.currentState.data?.optimisticUpdates
        )
    }
}