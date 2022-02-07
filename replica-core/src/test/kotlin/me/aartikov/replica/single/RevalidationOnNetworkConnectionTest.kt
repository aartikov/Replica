package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.LoadingFailedException
import me.aartikov.replica.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RevalidationOnNetworkConnectionTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `refreshing on connection`() = runTest {
        val isConnected = MutableStateFlow(false)
        val networkConnectivityProvider = FakeNetworkConnectivityProvider(isConnected)
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = null,
                revalidateOnNetworkConnection = true,
            ),
            networkConnectivityProvider = networkConnectivityProvider,
            fetcher = {
                if (isConnected.value) {
                    ReplicaProvider.TEST_DATA
                } else throw LoadingFailedException()
            }
        )

        replica.observe(TestScope(), MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY)
        isConnected.update { true }
        delay(DEFAULT_DELAY)

        val state = replica.currentState
        assertNull(state.error)
        assertNotNull(state.data)
        assertTrue(state.hasFreshData)
    }

    @Test
    fun `not refreshing on connection if no observers`() = runTest {
        val isConnected = MutableStateFlow(false)
        val networkConnectivityProvider = FakeNetworkConnectivityProvider(isConnected)
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = null,
                revalidateOnNetworkConnection = true,
            ),
            networkConnectivityProvider = networkConnectivityProvider,
            fetcher = {
                if (isConnected.value) {
                    ReplicaProvider.TEST_DATA
                } else throw LoadingFailedException()
            }
        )

        replica.refresh()
        delay(DEFAULT_DELAY)
        isConnected.update { true }
        delay(DEFAULT_DELAY)

        val state = replica.currentState
        assertNotNull(state.error)
        assertFalse(state.loading)
        assertNull(state.data)
    }

    @Test
    fun `not refreshing on connection if inactive observer observes`() = runTest {
        val isConnected = MutableStateFlow(false)
        val networkConnectivityProvider = FakeNetworkConnectivityProvider(isConnected)
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = null,
                revalidateOnNetworkConnection = true,
            ),
            networkConnectivityProvider = networkConnectivityProvider,
            fetcher = {
                if (isConnected.value) {
                    ReplicaProvider.TEST_DATA
                } else throw LoadingFailedException()
            }
        )

        replica.observe(TestScope(), MutableStateFlow(false))
        replica.refresh()
        delay(DEFAULT_DELAY)
        isConnected.update { true }
        delay(DEFAULT_DELAY)

        val state = replica.currentState
        assertNotNull(state.error)
        assertFalse(state.loading)
        assertNull(state.data)
    }

    @Test
    fun `refreshing on connection if inactive observer became active`() = runTest {
        val isConnected = MutableStateFlow(false)
        val networkConnectivityProvider = FakeNetworkConnectivityProvider(isConnected)
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = null,
                revalidateOnNetworkConnection = true,
            ),
            networkConnectivityProvider = networkConnectivityProvider,
            fetcher = {
                if (isConnected.value) {
                    ReplicaProvider.TEST_DATA
                } else throw LoadingFailedException()
            }
        )

        val observerActive = MutableStateFlow(false)
        replica.observe(TestScope(), observerActive)
        replica.refresh()
        delay(DEFAULT_DELAY)
        isConnected.update { true }
        delay(DEFAULT_DELAY)
        observerActive.update { true }
        delay(DEFAULT_DELAY)

        val state = replica.currentState
        assertNull(state.error)
        assertNotNull(state.data)
    }

    @Test
    fun `no refreshing on connection if active observer is canceled`() = runTest {
        val isConnected = MutableStateFlow(false)
        val networkConnectivityProvider = FakeNetworkConnectivityProvider(isConnected)
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = null,
                revalidateOnNetworkConnection = true,
            ),
            networkConnectivityProvider = networkConnectivityProvider,
            fetcher = {
                if (isConnected.value) {
                    ReplicaProvider.TEST_DATA
                } else throw LoadingFailedException()
            }
        )

        val observerScope = TestScope()
        replica.observe(observerScope, MutableStateFlow(true))
        replica.refresh()
        delay(DEFAULT_DELAY)
        observerScope.cancel()
        delay(DEFAULT_DELAY)
        isConnected.update { true }
        delay(DEFAULT_DELAY)

        val state = replica.currentState
        assertNotNull(state.error)
        assertNull(state.data)
    }
}