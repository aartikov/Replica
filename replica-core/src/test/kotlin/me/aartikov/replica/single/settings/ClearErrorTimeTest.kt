package me.aartikov.replica.single.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.LoadingFailedException
import me.aartikov.replica.single.utils.MainCoroutineRule
import me.aartikov.replica.single.utils.ReplicaProvider
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
    fun `is clear after clear error time is passed`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until clear error time is passed

        assertNull(replica.currentState.error)
    }

    @Test
    fun `isn't clear before clear error time is passed`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.refresh()
        delay(DEFAULT_DELAY - 1) // clear error time isn't passed yet

        assertNotNull(replica.currentState.error)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `not clearing if no clear error time is set`() = runTest {
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
    fun `not clearing after second refreshing`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until clear error time is passed
        replica.refresh()
        delay(DEFAULT_DELAY - 1) // clear error time isn't passed yet

        assertNotNull(replica.currentState.error)
    }

    @Test
    fun `not clearing if clear error time is passed and active observer observes`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until clear error time is passed

        assertNotNull(replica.currentState.error)
    }

    @Test
    fun `not clearing if clear error time is passed and inactive observer is added`() = runTest {
        val error = LoadingFailedException()
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearErrorTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = { throw error }
        )

        replica.observe(TestScope(), MutableStateFlow(false))
        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until clear error time is passed

        assertNotNull(replica.currentState.error)
    }

    @Test
    fun `clearing if observer is canceled observing and then clear error time is passed`() =
        runTest {
            val error = LoadingFailedException()
            val replica = replicaProvider.replica(
                replicaSettings = ReplicaSettings(
                    staleTime = 30.seconds,
                    clearErrorTime = DEFAULT_DELAY.milliseconds
                ),
                fetcher = { throw error }
            )

            val observerScope = TestScope()
            replica.observe(observerScope, MutableStateFlow(true))
            replica.refresh()
            observerScope.cancel()
            delay(DEFAULT_DELAY + 1) // waiting until error time is passed

            assertNull(replica.currentState.error)
        }
}