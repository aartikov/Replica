package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RevalidationOnActiveObserverTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `refreshing on first active observer`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = null,
                revalidateOnActiveObserverAdded = true
            ),
        )

        replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY)

        assertNotNull(replica.currentState.data)
    }

    @Test
    fun `no refreshing on first active observer`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = null,
                revalidateOnActiveObserverAdded = false
            ),
        )

        replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY)

        assertNull(replica.currentState.data)
    }

    @Test
    fun `revalidation if stale time is passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
        )

        delay(DEFAULT_DELAY + 1)
        replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY - 1)

        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `no revalidation if stale time is passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = false
            ),
        )

        delay(DEFAULT_DELAY + 1)
        replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY - 1)

        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `no refresh if inactive observer observes`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
        )

        replica.observe(TestScope(), MutableStateFlow(false))
        delay(DEFAULT_DELAY)

        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `refreshing if inactive observer became active`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
        )

        val observerActive = MutableStateFlow(false)
        replica.observe(TestScope(), observerActive)
        observerActive.update { true }
        delay(DEFAULT_DELAY - 1)

        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `revalidation if inactive observer became active`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
        )

        delay(DEFAULT_DELAY + 1)
        val observerActive = MutableStateFlow(false)
        replica.observe(TestScope(), observerActive)
        observerActive.update { true }
        delay(DEFAULT_DELAY - 1)

        assertTrue(replica.currentState.hasFreshData)
    }
}