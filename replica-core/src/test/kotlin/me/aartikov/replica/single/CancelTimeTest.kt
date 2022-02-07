package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CancelTimeTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `loading canceled if cancel time is passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        val observerScope = TestScope()
        replica.observe(observerScope, MutableStateFlow(true))
        delay(DEFAULT_DELAY)
        observerScope.cancel()
        delay(DEFAULT_DELAY + 1)

        assertFalse(replica.currentState.loading)
        assertNull(replica.currentState.data)
    }

    @Test
    fun `loading not canceled if cancel time is not passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        val observerScope = TestScope()
        replica.observe(observerScope, MutableStateFlow(true))
        delay(DEFAULT_DELAY)
        observerScope.cancel()
        delay(DEFAULT_DELAY - 1)

        assertTrue(replica.currentState.loading)
        assertNull(replica.currentState.data)
    }

    @Test
    fun `loading not canceled if it's preloading`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1)

        assertTrue(replica.currentState.loading)
        assertTrue(replica.currentState.preloading)
        assertNull(replica.currentState.data)
    }

    @Test
    fun `loading not canceled if active observer became inactive`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        val observerScope = TestScope()
        val observerActive = MutableStateFlow(true)
        replica.observe(observerScope, observerActive)
        delay(DEFAULT_DELAY)
        observerActive.update { false }
        delay(DEFAULT_DELAY + 1)

        assertTrue(replica.currentState.loading)
    }

    @Test
    fun `loading canceled if cancel time is passed and observer was inactive`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        val observer = replica.observe(TestScope(), MutableStateFlow(false))
        replica.refresh()
        delay(DEFAULT_DELAY)
        observer.cancelObserving()
        delay(DEFAULT_DELAY + 1)

        assertFalse(replica.currentState.loading)
    }

    @Test
    fun `loading not canceled if new observer starts observing`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        val observerScope = TestScope()
        replica.observe(observerScope, MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY)
        observerScope.cancel()
        delay(DEFAULT_DELAY - 1)
        replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY)

        assertTrue(replica.currentState.loading)
    }

    @Test
    fun `loading not canceled if data requested`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY)
        launch { replica.getRefreshedData() }
        observer.cancelObserving()
        delay(DEFAULT_DELAY * 2)

        assertTrue(replica.currentState.loading)
    }
}