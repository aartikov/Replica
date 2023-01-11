package me.aartikov.replica.single.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
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
    fun `refreshing when active observer starts observing and revalidation turn on`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = null,
                revalidateOnActiveObserverAdded = true
            ),
        )

        replica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()

        assertNotNull(replica.currentState.data)
    }

    @Test
    fun `no refreshing when active observer starts observing and revalidation turn off`() =
        runTest {
            val replica = replicaProvider.replica(
                replicaSettings = ReplicaSettings(
                    staleTime = null,
                    revalidateOnActiveObserverAdded = false
                ),
            )

            replica.observe(TestScope(), MutableStateFlow(true))
            runCurrent()

            assertNull(replica.currentState.data)
        }

    @Test
    fun `refreshing if stale time is passed, active observer starts observing, revalidation turn on`() =
        runTest {
            val replica = replicaProvider.replica(
                replicaSettings = ReplicaSettings(
                    staleTime = DEFAULT_DELAY.milliseconds,
                    revalidateOnActiveObserverAdded = true
                ),
            )

            replica.refresh()
            delay(DEFAULT_DELAY + 1) // stale time is passed
            replica.observe(TestScope(), MutableStateFlow(true))
            delay(DEFAULT_DELAY - 1) // stale time isn't passed yet

            assertTrue(replica.currentState.hasFreshData)
        }

    @Test
    fun `no refreshing if stale time is passed, active observer starts observing, revalidation turn off`() =
        runTest {
            val replica = replicaProvider.replica(
                replicaSettings = ReplicaSettings(
                    staleTime = DEFAULT_DELAY.milliseconds,
                    revalidateOnActiveObserverAdded = false
                ),
            )

            replica.refresh()
            delay(DEFAULT_DELAY + 1) // stale time is passed
            replica.observe(TestScope(), MutableStateFlow(true))
            delay(DEFAULT_DELAY - 1) // stale time isn't passed yet

            assertFalse(replica.currentState.hasFreshData)
        }

    @Test
    fun `no refreshing if inactive observer is added, revalidation turn on`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
        )

        replica.observe(TestScope(), MutableStateFlow(false))
        runCurrent()

        assertFalse(replica.currentState.hasFreshData)
    }

    @Test
    fun `refreshing if inactive observer became active, revalidation turn on`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
        )

        val observerActive = MutableStateFlow(false)
        replica.observe(TestScope(), observerActive)
        observerActive.update { true }
        runCurrent()

        assertTrue(replica.currentState.hasFreshData)
    }

    @Test
    fun `revalidation if inactive observer became active, revalidation turn on`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds,
                revalidateOnActiveObserverAdded = true
            ),
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until stale time passed
        val observerActive = MutableStateFlow(false)
        replica.observe(TestScope(), observerActive)
        observerActive.update { true }
        runCurrent()

        assertTrue(replica.currentState.hasFreshData)
    }
}