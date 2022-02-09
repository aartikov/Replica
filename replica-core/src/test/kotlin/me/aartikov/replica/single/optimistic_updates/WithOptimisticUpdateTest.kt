package me.aartikov.replica.single.optimistic_updates

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.keyed.withOptimisticUpdate
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.LoadingFailedException
import me.aartikov.replica.single.utils.MainCoroutineRule
import me.aartikov.replica.single.utils.ReplicaProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WithOptimisticUpdateTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `completes with success`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()
        var isSuccess = false
        var isError = false
        var isCanceled = false
        var isCompleted = false

        replica.refresh()
        runCurrent()
        withOptimisticUpdate(
            optimisticUpdate,
            replica,
            onSuccess = { isSuccess = true },
            onError = { isError = true },
            onCanceled = { isCanceled = true },
            onCompleted = { isCompleted = true }
        ) {
            // Nothing
        }
        runCurrent()

        assertTrue(isSuccess)
        assertFalse(isError)
        assertFalse(isCanceled)
        assertTrue(isCompleted)
        assertEquals(
            newData,
            replica.currentState.data?.value
        )
    }

    @Test
    fun `completes with error`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()
        var isSuccess = false
        var isError = false
        var isCanceled = false
        var isCompleted = false

        replica.refresh()
        runCurrent()
        try {
            withOptimisticUpdate(
                update = optimisticUpdate,
                replica = replica,
                onSuccess = { isSuccess = true },
                onError = { isError = true },
                onCanceled = { isCanceled = true },
                onCompleted = { isCompleted = true }) {
                delay(DEFAULT_DELAY)
                throw LoadingFailedException()
            }
        } catch (e: LoadingFailedException) {
            // Nothing
        }

        assertFalse(isSuccess)
        assertTrue(isError)
        assertFalse(isCanceled)
        assertTrue(isCompleted)
        assertEquals(ReplicaProvider.TEST_DATA, replica.currentState.data?.value)
    }

    @Test
    fun `completes with cancel`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()
        var isSuccess = false
        var isError = false
        var isCanceled = false
        var isCompleted = false

        replica.refresh()
        runCurrent()
        launch {
            withOptimisticUpdate(
                update = optimisticUpdate,
                replica = replica,
                onSuccess = { isSuccess = true },
                onError = { isError = true },
                onCanceled = { isCanceled = true },
                onCompleted = { isCompleted = true }
            ) { cancel() }
        }
        runCurrent()

        assertFalse(isSuccess)
        assertFalse(isError)
        assertTrue(isCanceled)
        assertTrue(isCompleted)
        assertEquals(ReplicaProvider.TEST_DATA, replica.currentState.data?.value)
    }

    @Test
    fun `multiple optimistic updates complete with success`() = runTest {
        val updatesCount = 20
        val updates = List(updatesCount) { index -> OptimisticUpdate<String> { index.toString() } }
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        updates.forEach { update ->
            launch {
                withOptimisticUpdate(
                    update = update,
                    replica = replica,
                ) {
                    // Nothing
                }
            }
        }
        runCurrent()

        assertEquals((updatesCount - 1).toString(), replica.currentState.data?.value)
    }

    @Test
    fun `shows first update data when second update is failed`() = runTest {
        val firstUpdateData = "first"
        val secondUpdateData = "second"
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        launch {
            withOptimisticUpdate(
                update = { firstUpdateData },
                replica = replica,
            ) {
                delay(DEFAULT_DELAY)
            }
        }
        delay(DEFAULT_DELAY / 2)
        try {
            withOptimisticUpdate(
                update = { secondUpdateData },
                replica = replica,
            ) {
                delay(DEFAULT_DELAY)
                throw LoadingFailedException()
            }
        } catch (e: LoadingFailedException) {
            // Nothing
        }
        delay(DEFAULT_DELAY + 1) // waiting until second update is complete

        assertEquals(firstUpdateData, replica.currentState.data?.value)
    }

    @Test
    fun `shows first update data when second update is canceled`() = runTest {
        val firstUpdateData = "first"
        val secondUpdateData = "second"
        val replica = replicaProvider.replica()

        replica.refresh()
        runCurrent()
        launch {
            withOptimisticUpdate(
                update = { firstUpdateData },
                replica = replica,
            ) {
                delay(DEFAULT_DELAY)
            }
        }
        delay(DEFAULT_DELAY / 2)
        launch {
            withOptimisticUpdate(
                update = { secondUpdateData },
                replica = replica,
            ) {
                delay(DEFAULT_DELAY)
                cancel()
            }
        }
        delay(DEFAULT_DELAY + 1) // waiting until second update is complete

        assertEquals(firstUpdateData, replica.currentState.data?.value)
    }
}