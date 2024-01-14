package me.aartikov.replica.algebra

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.algebra.normal.map
import me.aartikov.replica.algebra.utils.LoadingFailedException
import me.aartikov.replica.algebra.utils.MainCoroutineRule
import me.aartikov.replica.algebra.utils.ReplicaProvider
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingReason
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.currentState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapReplicaTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `receives mapped data from MappedReplica`() = runTest {
        val replica = replicaProvider.replica()
        val transform = { value: String -> value + "mapped" }

        val mappedReplica = replica.map(transform)
        val actualData = mappedReplica.getData()
        runCurrent()

        assertEquals(transform(ReplicaProvider.TEST_DATA), actualData)
    }

    @Test
    fun `observes mapped data from MappedReplica`() = runTest {
        val replica = replicaProvider.replica()
        val transform = { value: String -> value + "mapped" }

        val mappedReplica = replica.map(transform)
        val observer = mappedReplica.observe(TestScope(), MutableStateFlow(true))
        mappedReplica.refresh()
        runCurrent()

        assertEquals(transform(ReplicaProvider.TEST_DATA), observer.currentState.data)
    }

    @Test
    fun `observes error state with mapped data after throwing error`() = runTest {
        var isFirstRefresh = true
        val exception = LoadingFailedException()
        val replica = replicaProvider.replica(
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else {
                    throw  exception
                }
            }
        )
        val transform = { value: String -> value + "mapped" }

        val mappedReplica: Replica<String> = replica.map(transform)
        val observer = mappedReplica.observe(TestScope(), MutableStateFlow(true))
        mappedReplica.refresh()
        runCurrent()
        mappedReplica.refresh()
        runCurrent()

        assertEquals(
            Loadable(
                data = transform(ReplicaProvider.TEST_DATA),
                error = CombinedLoadingError(LoadingReason.Normal, exception)
            ),
            observer.currentState
        )
    }

    @Test
    fun `observes mapped data during refresh`() = runTest {
        var isFirstRefresh = true
        val newData = "new data"
        val replica = replicaProvider.replica(
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else {
                    delay(DEFAULT_DELAY)
                    newData
                }
            }
        )
        val transform = { value: String -> value + "mapped" }

        val mappedReplica = replica.map(transform)
        val observer = mappedReplica.observe(TestScope(), MutableStateFlow(true))
        mappedReplica.refresh()
        runCurrent()
        mappedReplica.refresh()
        delay(DEFAULT_DELAY - 1) // loading not complete yet

        assertEquals(
            Loadable(
                loading = true,
                data = transform(ReplicaProvider.TEST_DATA)
            ),
            observer.currentState
        )
    }
}