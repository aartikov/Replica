package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.LoadingFailedException
import me.aartikov.replica.MainCoroutineRule
import me.aartikov.replica.common.CombinedLoadingError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObservingDataTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `shows data when it is loaded`() = runTest {
        val replica = replicaProvider.replica()

        replica.refresh()
        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY)

        replica.getRefreshedData()
        val state = observer.currentState
        assertEquals(Loadable(data = ReplicaProvider.TEST_DATA), state)
    }

    @Test
    fun `shows new data after second refresh`() = runTest {
        val newData = "new data"
        var isFirstRefresh = true
        val replica = replicaProvider.replica(
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else newData
            }
        )

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY)
        replica.refresh()
        delay(DEFAULT_DELAY)

        assertEquals(Loadable(data = newData), observer.currentState)
    }

    @Test
    fun `shows loading during second refresh`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY * 2)
        replica.refresh()
        delay(DEFAULT_DELAY)

        assertEquals(
            Loadable(data = ReplicaProvider.TEST_DATA, loading = true),
            observer.currentState
        )
    }

    @Test
    fun `shows previous data when second refresh is error`() = runTest {
        val error = LoadingFailedException()
        var isFirstRefresh = true
        val replica = replicaProvider.replica(
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else {
                    throw error
                }
            }
        )

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY)
        replica.refresh()
        delay(DEFAULT_DELAY)

        assertEquals(
            Loadable(
                data = ReplicaProvider.TEST_DATA,
                error = CombinedLoadingError(error)
            ),
            observer.currentState
        )
    }

    @Test
    fun `doesn't show data when refreshing data is canceled`() = runTest {
        var isFirstRefresh = true
        val replica = replicaProvider.replica()

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        replica.cancel()
        delay(DEFAULT_DELAY)

        assertEquals(
            Loadable<String>(),
            observer.currentState
        )
    }

    @Test
    fun `shows previous data when second refreshing is canceled`() = runTest {
        var isFirstRefresh = true

        val replica = replicaProvider.replica(
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else {
                    "new data"
                }
            }
        )

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY)
        replica.refresh()
        replica.cancel()
        delay(DEFAULT_DELAY)

        replica.clearError()
        assertEquals(
            Loadable(data = ReplicaProvider.TEST_DATA),
            observer.currentState
        )
    }

    @Test
    fun `shows no data if it is cleared from storage`() = runTest {
        val replica = replicaProvider.replica()

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY)
        replica.clear()
        delay(DEFAULT_DELAY)

        assertEquals(
            Loadable<Any>(),
            observer.currentState
        )
    }

    @Test
    fun `shows data if it isn't cleared from storage`() = runTest {
        val replica = replicaProvider.replica()

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY)
        replica.clear(removeFromStorage = false)

        assertEquals(
            Loadable<Any>(data = ReplicaProvider.TEST_DATA),
            observer.currentState
        )
    }
}