package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.MainCoroutineRule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ClearingTimeTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `clear after clear time is passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1)

        assertNull(replica.currentState.data)
    }

    @Test
    fun `isn't clear before clear time is passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY - 1)

        assertNotNull(replica.currentState.data)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `not clearing if no clear time is set`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings.WithoutBehaviour
        )

        replica.refresh()
        delay(30.seconds)

        assertNotNull(replica.currentState.data)
    }

    @Test
    fun `isn't clear after second refreshing`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1)
        replica.refresh()
        delay(DEFAULT_DELAY - 1)

        assertNotNull(replica.currentState.data)
    }
}