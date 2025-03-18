package me.aartikov.replica.single.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.TestObserverHost
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ClearDataTimeTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `is clear after clear time is passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until clear time is passed

        assertNull(replica.currentState.data)
    }

    @Test
    fun `isn't clear if clear time isn't passed yet`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearTime = DEFAULT_DELAY.milliseconds
            )
        )

        replica.refresh()
        delay(DEFAULT_DELAY - 1) // clear time isn't passed yet

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
    fun `not clearing if clear time is passed and then second refreshing happens`() = runTest {
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

    @Test
    fun `not clearing after clear time is passed and active observer observes`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearTime = DEFAULT_DELAY.milliseconds
            )
        )

        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until clear time is passed

        assertNotNull(replica.currentState.data)
    }

    @Test
    fun `not clearing after clear time is passed and inactive observer is added`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                clearTime = DEFAULT_DELAY.milliseconds
            )
        )

        val observerHost = TestObserverHost(active = false)
        replica.observe(observerHost)
        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until clear time is passed

        assertNotNull(replica.currentState.data)
    }
}