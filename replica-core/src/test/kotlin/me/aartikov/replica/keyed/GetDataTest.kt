package me.aartikov.replica.keyed

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.utils.LoadingFailedException
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class GetDataTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `initially loads fresh data`() = runTest {
        val replica = replicaProvider.replica()

        val data = replica.getData(DEFAULT_KEY)

        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), data)
    }

    @Test
    fun `returns fresh data if there is stale data`() = runTest {
        var isFirstRefresh = true
        val expectedNewData = "new data"
        val replica = replicaProvider.replica(
            childReplicaSettings = {
                ReplicaSettings(
                    staleTime = DEFAULT_DELAY.milliseconds
                )
            },
            fetcher = {
                if (isFirstRefresh) {
                    isFirstRefresh = false
                    KeyedReplicaProvider.testData(it)
                } else {
                    expectedNewData
                }
            }
        )

        val actualData = replica.getData(DEFAULT_KEY)
        delay(DEFAULT_DELAY + 1) // waiting until data is stale
        val actualNewData = replica.getData(DEFAULT_KEY)

        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), actualData)
        assertEquals(expectedNewData, actualNewData)
    }

    @Test(expected = LoadingFailedException::class)
    fun `throws exception if fetcher throws exception`() = runTest {
        val replica = replicaProvider.replica(
            fetcher = {
                throw LoadingFailedException()
            }
        )

        replica.getData(DEFAULT_KEY)
    }
}