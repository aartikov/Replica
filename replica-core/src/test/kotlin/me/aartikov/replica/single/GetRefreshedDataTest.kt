package me.aartikov.replica.single

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.utils.LoadingFailedException
import me.aartikov.replica.single.utils.MainCoroutineRule
import me.aartikov.replica.single.utils.ReplicaProvider
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class GetRefreshedDataTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `initially loads fresh data`() = runTest {
        val replica = replicaProvider.replica()

        val data = replica.getRefreshedData()

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

        val actualData = replica.getRefreshedData()
        val actualNewData = replica.getRefreshedData()

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

        val actualData = replica.getData()
        delay(DEFAULT_DELAY + 1) // waiting until data is stale
        val actualNewData = replica.getData()

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

        replica.getRefreshedData()
    }
}