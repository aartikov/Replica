package me.aartikov.replica.single.optimistic_updates

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class BeginOptimisticUpdateTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `adds optimistic update when there is data`() = runTest {
        val optimisticUpdate = OptimisticUpdate<String> { "new data" }
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        replica.beginOptimisticUpdate(optimisticUpdate)

        assertEquals(listOf(optimisticUpdate), replica.currentState.data?.optimisticUpdates)
    }

    @Test
    fun `doesn't add optimistic update when there is no data`() = runTest {
        val optimisticUpdate = OptimisticUpdate<String> { "new data" }
        val replica = replicaProvider.replica()

        replica.beginOptimisticUpdate(optimisticUpdate)

        assertNull(replica.currentState.data?.optimisticUpdates)
    }

    @Test
    fun `adds optimistic update when there is stale data`() = runTest {
        val optimisticUpdate = OptimisticUpdate<String> { "new data" }
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until data is stale
        replica.beginOptimisticUpdate(optimisticUpdate)

        assertEquals(
            listOf(optimisticUpdate),
            replica.currentState.data?.optimisticUpdates
        )
    }
}