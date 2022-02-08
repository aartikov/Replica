package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RequestDeduplicationTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `deduplication during multiple refresh calls`() = runTest {
        var counter = 0
        val refreshCount = 5
        val replica = replicaProvider.replica(
            fetcher = {
                counter++
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )

        repeat(refreshCount) {
            replica.refresh()
            runCurrent()
        }

        assertEquals(1, counter)
    }

    @Test
    fun `duplication during multiple refresh if previous requests is completed`() = runTest {
        var counter = 0
        val refreshCount = 5
        val replica = replicaProvider.replica(
            fetcher = {
                counter++
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )

        repeat(refreshCount) {
            replica.refresh()
            delay(DEFAULT_DELAY + 1) // waiting until request is completed
        }

        assertEquals(refreshCount, counter)
    }

    @Test
    fun `deduplication during refresh and get data calls`() = runTest {
        var counter = 0
        val replica = replicaProvider.replica(
            fetcher = {
                counter++
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )

        replica.refresh()
        replica.getData()

        assertEquals(1, counter)
    }

    @Test
    fun `deduplication during refresh and get refreshed data calls`() = runTest {
        var counter = 0
        val replica = replicaProvider.replica(
            fetcher = {
                counter++
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )

        replica.refresh()
        replica.getRefreshedData()

        assertEquals(1, counter)
    }

    @Test
    fun `deduplication during refresh and revalidate calls`() = runTest {
        var counter = 0
        val replica = replicaProvider.replica(
            fetcher = {
                counter++
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )

        replica.refresh()
        replica.revalidate()
        runCurrent()

        assertEquals(1, counter)
    }

    @Test
    fun `deduplication during multiple get data calls`() = runTest {
        var counter = 0
        val requestsCount = 5

        val replica = replicaProvider.replica(
            fetcher = {
                counter++
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )

        repeat(requestsCount) {
            launch { replica.getData() }
        }
        runCurrent()

        assertEquals(1, counter)
    }

    @Test
    fun `deduplication during get data and revalidate calls`() = runTest {
        var counter = 0

        val replica = replicaProvider.replica(
            fetcher = {
                counter++
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )

        launch { replica.getData() }
        replica.revalidate()
        runCurrent()

        assertEquals(1, counter)
    }
}