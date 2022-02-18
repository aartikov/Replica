package me.aartikov.replica.single.optimistic_updates

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.keyed.withOptimisticUpdate
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.LoadingFailedException
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

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
    fun `observing new data with began optimistic update`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()

        replica.refresh()
        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()
        launch {
            withOptimisticUpdate(
                update = optimisticUpdate,
                replica = replica,
            ) {
                delay(DEFAULT_DELAY)
            }
        }
        runCurrent()

        assertEquals(newData, observer.currentState.data)
    }

    @Test
    fun `observing new data with committed optimistic update`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()

        replica.refresh()
        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()
        launch {
            withOptimisticUpdate(
                update = optimisticUpdate,
                replica = replica,
            ) {
                delay(DEFAULT_DELAY)
            }
        }
        delay(DEFAULT_DELAY + 1) // waiting until optimistic update completes

        assertEquals(newData, observer.currentState.data)
    }

    @Test
    fun `observing old data with failed optimistic update`() = runTest {
        val newData = "new data"
        val optimisticUpdate = OptimisticUpdate<String> { newData }
        val replica = replicaProvider.replica()

        replica.refresh()
        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()
        launch {
            try {
                withOptimisticUpdate(
                    update = optimisticUpdate,
                    replica = replica,
                ) {
                    delay(DEFAULT_DELAY)
                    throw LoadingFailedException()
                }
            } catch (e: LoadingFailedException) {
                // Nothing
            }
        }
        delay(DEFAULT_DELAY + 1) // waiting until optimistic update completes

        assertEquals(ReplicaProvider.TEST_DATA, observer.currentState.data)
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

    @Test
    fun `multiple independence optimistic updates begins`() = runTest {
        val updatesCount = 20
        val data = String(CharArray(updatesCount) { Char(it + 97) }) // to latin small letters
        val replica = replicaProvider.replica(
            fetcher = { data }
        )

        replica.refresh()
        runCurrent()
        for (i in 0 until updatesCount) {
            launch {
                withOptimisticUpdate(
                    update = {
                        StringBuilder(it)
                            .also { stringBuilder ->
                                stringBuilder[i] = it[i].uppercaseChar()
                            }
                            .toString()
                    },
                    replica = replica,
                ) {
                    delay(DEFAULT_DELAY)
                }
            }
        }
        runCurrent()

        assertEquals(data.uppercase(), replica.getData())
    }

    @Test
    fun `multiple independence optimistic updates commits`() = runTest {
        val updatesCount = 20
        val data = String(CharArray(updatesCount) { Char(it + 97) }) // to latin small letters
        val replica = replicaProvider.replica(
            fetcher = { data }
        )

        replica.refresh()
        runCurrent()
        for (i in 0 until updatesCount) {
            launch {
                withOptimisticUpdate(
                    update = {
                        StringBuilder(it)
                            .also { stringBuilder ->
                                stringBuilder[i] = it[i].uppercaseChar()
                            }
                            .toString()
                    },
                    replica = replica,
                ) {
                    delay(DEFAULT_DELAY)
                }
            }
        }
        delay(DEFAULT_DELAY + 1) // waiting until optimistic updates commits

        assertEquals(data.uppercase(), replica.getData())
    }

    @Test
    fun `some independence optimistic updates fails`() = runTest {
        val updatesCount = 20
        val data = String(CharArray(updatesCount) { Char(it + 97) }) // to latin small letters
        val fails = List(updatesCount) { Random.nextBoolean() }
        val replica = replicaProvider.replica(
            fetcher = { data }
        )

        replica.refresh()
        runCurrent()
        for (i in 0 until updatesCount) {
            launch {
                try {
                    withOptimisticUpdate(
                        update = {
                            if (fails[i]) {
                                throw LoadingFailedException()
                            } else {
                                StringBuilder(it)
                                    .also { stringBuilder ->
                                        stringBuilder[i] = it[i].uppercaseChar()
                                    }
                                    .toString()
                            }
                        },
                        replica = replica,
                    ) {
                        delay(DEFAULT_DELAY)
                    }
                } catch (e: LoadingFailedException) {
                    // Nothing
                }
            }
        }

        delay(DEFAULT_DELAY + 1) // waiting until optimistic updates commits

        val expectedData = String(
            data
                .mapIndexed { i, c ->
                    if (fails[i]) c else c.uppercaseChar()
                }.toCharArray()
        )
        assertEquals(expectedData, replica.getData())
    }
}