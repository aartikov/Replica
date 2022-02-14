package me.aartikov.replica.keyed.storage

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedFakeStorage
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KeyedStorageTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `writes data in storage after refresh call`() = runTest {
        val storage = KeyedFakeStorage()
        val replica = replicaProvider.replica(storage = storage)

        replica.refresh(DEFAULT_KEY)
        runCurrent()

        val storageData = storage.read(DEFAULT_KEY)
        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), storageData)
    }

    @Test
    fun `clears data in storage after clear call with removeFromStorage flag`() = runTest {
        val storage = KeyedFakeStorage()
        val replica = replicaProvider.replica(storage = storage)

        replica.refresh(DEFAULT_KEY)
        runCurrent()
        replica.clear(key = DEFAULT_KEY, removeFromStorage = true)

        val storageData = storage.read(DEFAULT_KEY)
        assertNull(storageData)
    }

    @Test
    fun `clears all storage after clearAll call`() = runTest {
        val storage = KeyedFakeStorage()
        val replica = replicaProvider.replica(storage = storage)
        val replicasCount = 10

        repeat(replicasCount) { i ->
            replica.refresh(i)
        }
        runCurrent()
        replica.clearAll()

        repeat(replicasCount) { i ->
            assertNull(storage.read(i))
        }
    }

    @Test
    fun `not clears data in storage after clear call without removeFromStorage flag`() = runTest {
        val storage = KeyedFakeStorage()
        val replica = replicaProvider.replica(storage = storage)

        replica.refresh(DEFAULT_KEY)
        runCurrent()
        replica.clear(key = DEFAULT_KEY, removeFromStorage = false)

        val storageData = storage.read(DEFAULT_KEY)
        assertEquals(KeyedReplicaProvider.testData(DEFAULT_KEY), storageData)
    }

    @Test
    fun `new replica receives stale data from storage`() = runTest {
        val storage = KeyedFakeStorage()
        val replica = replicaProvider.replica(storage = storage)
        val data = "data"

        replica.setData(DEFAULT_KEY, data)
        val newReplica = replicaProvider.replica(
            storage = storage,
            fetcher = {
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )
        newReplica.refresh(DEFAULT_KEY)
        delay(DEFAULT_DELAY / 2) // loading not complete yet

        val newReplicaChildState = newReplica.getCurrentState(DEFAULT_KEY)
        assertFalse(newReplicaChildState?.hasFreshData == true)
        assertEquals(data, newReplicaChildState?.data?.value)
    }
}