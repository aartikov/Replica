package me.aartikov.replica.single.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.LoadingReason
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.LoadingFailedException
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoadingErrorObservingTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `active observer observes loading state`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.refresh()
        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(loading = true), state)
    }

    @Test
    fun `inactive observer doesn't observe loading, error state`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                throw LoadingFailedException()
            }
        )

        replica.refresh()
        val observer = replica.observe(TestScope(), MutableStateFlow(false))
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `active observer doesn't observe loading state when canceled observing`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        observer.cancelObserving()
        replica.refresh()
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `active observer doesn't observe loading state when scope canceled`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                throw LoadingFailedException()
            }
        )

        val scope = TestScope()
        val observer = replica.observe(scope, MutableStateFlow(true))
        scope.cancel()
        replica.refresh()
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `inactive observer observes loading state when became active`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                "test"
            }
        )

        replica.refresh()
        val observerActive = MutableStateFlow(false)
        val observer = replica.observe(TestScope(), observerActive)
        observerActive.update { true }
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(loading = true), state)
    }

    @Test
    fun `active observer observes error event`() = runTest {
        val exception = LoadingFailedException()
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                throw exception
            }
        )

        replica.refresh()
        val observer = replica.observe(TestScope(), MutableStateFlow(true))

        val errorEvent = observer.loadingErrorFlow.firstOrNull()
        delay(DEFAULT_DELAY * 2) // wait until loading has finished
        val state = observer.currentState
        assertEquals(
            Loadable<String>(error = CombinedLoadingError(LoadingReason.Normal, exception)),
            state
        )
        assertEquals(LoadingError(LoadingReason.Normal, exception), errorEvent)
    }

    @Test
    fun `inactive observer doesn't observe error event`() = runTest {
        val exception = LoadingFailedException()
        val replica = replicaProvider.replica(
            fetcher = {
                throw exception
            }
        )

        replica.refresh()
        val observer = replica.observe(TestScope(), MutableStateFlow(false))
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<String>(), state)
    }

    @Test
    fun `active observer observes multiple error events`() = runTest {
        val exception = LoadingFailedException()
        val errorsCount = 10
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                throw exception
            }
        )

        launch {
            repeat(errorsCount) {
                replica.refresh()
                delay(DEFAULT_DELAY * 2) // wait until loading has finished
            }
        }
        val observer = replica.observe(TestScope(), MutableStateFlow(true))

        val errorEvents = observer.loadingErrorFlow.take(errorsCount).toList()
        val state = observer.currentState
        assertEquals(
            List(errorsCount) { LoadingError(LoadingReason.Normal, exception) },
            errorEvents
        )
        assertEquals(Loadable<String>(error = CombinedLoadingError(LoadingReason.Normal, exception)), state)
    }

    @Test
    fun `observer observes error events only when active`() = runTest {
        var counter = 0
        val exception = { num: Int -> LoadingFailedException(num.toString()) }
        val errorsCount = 30
        val replica = replicaProvider.replica(
            fetcher = {
                delay(DEFAULT_DELAY)
                throw exception(counter)
            }
        )

        launch {
            repeat(errorsCount) {
                replica.refresh()
                counter++
                delay(DEFAULT_DELAY * 2)
            }
        }

        val observerActive = MutableStateFlow(false)
        val observer = replica.observe(TestScope(), observerActive)
        delay(DEFAULT_DELAY * errorsCount)
        observerActive.update { true }

        val errorEvents = observer.loadingErrorFlow.take(errorsCount / 2).toList()
        val state = observer.currentState
        val expectedErrorsEvents = ((errorsCount / 2 + 1)..errorsCount).map {
            LoadingError(LoadingReason.Normal, LoadingFailedException(it.toString()))
        }
        val expectedState = Loadable<String>(
            error = CombinedLoadingError(
                LoadingReason.Normal, LoadingFailedException(errorsCount.toString())
            )
        )
        assertEquals(expectedErrorsEvents, errorEvents)
        assertEquals(expectedState, state)
    }
}