package me.aartikov.replica.single.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.FakeNetworkConnectivityProvider
import me.aartikov.replica.utils.LoadingFailedException
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.TestObserverHost
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RevalidationOnNetworkConnectionTest {

    private val replicaProvider = ReplicaProvider()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `refreshing on connection, revalidation turn on`() = runTest {
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

        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        replica.refresh()
        isConnected.update { true }
        runCurrent()

        val state = replica.currentState
        assertNull(state.error)
        assertNotNull(state.data)
        assertTrue(state.hasFreshData)
    }

    @Test
    fun `no refreshing on connection if no observers, revalidation turn on`() = runTest {
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
        runCurrent()
        isConnected.update { true }
        runCurrent()

        val state = replica.currentState
        assertNotNull(state.error)
        assertFalse(state.loading)
        assertNull(state.data)
    }

    @Test
    fun `not refreshing on connection if inactive observer is added, revalidation turn on`() =
        runTest {
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

            val observerHost = TestObserverHost(active = false)
            replica.observe(observerHost)
            replica.refresh()
            runCurrent()
            isConnected.update { true }
            runCurrent()

            val state = replica.currentState
            assertNotNull(state.error)
            assertFalse(state.loading)
            assertNull(state.data)
        }

    @Test
    fun `no refreshing on connection if active observer is canceled, revalidation turn on`() =
        runTest {
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

            val observerHost = TestObserverHost(active = true)
            replica.observe(observerHost)
            replica.refresh()
            runCurrent()
            observerHost.cancelCoroutineScope()

            isConnected.update { true }
            runCurrent()

            val state = replica.currentState
            assertNotNull(state.error)
            assertNull(state.data)
        }
}