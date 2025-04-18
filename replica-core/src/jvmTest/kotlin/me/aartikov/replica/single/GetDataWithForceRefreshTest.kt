package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.LoadingFailedException
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class GetDataWithForceRefreshTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `initially loads fresh data`() = runTest {
        val replica = replicaProvider.replica()

        val data = replica.getData(forceRefresh = true)

        assertEquals(ReplicaProvider.TEST_DATA, data)
    }

    @Test
    fun `returns fresh data if there is fresh data`() = runTest {
        var isFirstRefresh = true
        val newData = "new data"
        val replica = replicaProvider.replica(
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else {
                    newData
                }
            }
        )

        val actualData = replica.getData(forceRefresh = true)
        val actualNewData = replica.getData(forceRefresh = true)

        assertEquals(ReplicaProvider.TEST_DATA, actualData)
        assertEquals(newData, actualNewData)
    }

    @Test
    fun `returns fresh data if there is stale data`() = runTest {
        var isFirstRefresh = true
        val expectedNewData = "new data"
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    ReplicaProvider.TEST_DATA
                } else {
                    expectedNewData
                }
            }
        )

        val actualData = replica.getData(forceRefresh = true)
        delay(DEFAULT_DELAY + 1) // waiting until data is stale
        val actualNewData = replica.getData(forceRefresh = true)

        assertEquals(ReplicaProvider.TEST_DATA, actualData)
        assertEquals(expectedNewData, actualNewData)
    }

    @Test(expected = LoadingFailedException::class)
    fun `throws exception if fetcher throws exception`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                throw LoadingFailedException()
            }
        )

        replica.getData(forceRefresh = true)
    }
}