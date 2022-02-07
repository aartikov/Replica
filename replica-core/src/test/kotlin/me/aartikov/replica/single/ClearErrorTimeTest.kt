package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.LoadingFailedException
import me.aartikov.replica.MainCoroutineRule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ClearErrorTimeTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `is clear after clear time is passed`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1)

        assertNull(replica.currentState.error)
    }

    @Test
    fun `isn't clear before clear time is passed`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.refresh()
        delay(DEFAULT_DELAY - 1)

        assertNotNull(replica.currentState.error)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `not clearing if no clear time is set`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings.WithoutBehaviour,
            fetcher = { throw error }
        )

        replica.refresh()
        delay(30.seconds)

        assertNotNull(replica.currentState.error)
    }

    @Test
    fun `isn't clear after second refreshing`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1)
        replica.refresh()
        delay(DEFAULT_DELAY - 1)

        assertNotNull(replica.currentState.error)
    }

    @Test
    fun `isn't clear if active observer observes`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY)
        replica.refresh()
        delay(DEFAULT_DELAY + 1)

        assertNotNull(replica.currentState.error)
    }

    @Test
    fun `isn't clear if inactive observer observes`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.observe(TestScope(), MutableStateFlow(false))
        delay(DEFAULT_DELAY)
        replica.refresh()
        delay(DEFAULT_DELAY + 1)

        assertNotNull(replica.currentState.error)
    }

    @Test
    fun `is clear if observer is canceled`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        val observerScope = TestScope()
        replica.observe(observerScope, MutableStateFlow(false))
        delay(DEFAULT_DELAY)
        replica.refresh()
        delay(DEFAULT_DELAY + 1)
        observerScope.cancel()
        delay(DEFAULT_DELAY)

        assertNull(replica.currentState.error)
    }
}