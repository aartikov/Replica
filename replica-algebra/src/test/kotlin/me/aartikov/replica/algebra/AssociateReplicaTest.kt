package me.aartikov.replica.algebra

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.algebra.utils.LoadingFailedException
import me.aartikov.replica.algebra.utils.MainCoroutineRule
import me.aartikov.replica.algebra.utils.ReplicaProvider
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssociateReplicaTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `receives data by definite key`() = runTest {
        val data = String(CharArray(26) { Char(it + 97) }) // latin alphabet in lowercase
        val replica = replicaProvider.replica(fetcher = { data })

        val associatedReplica = associate { index: Int ->
            replica.map { it.getOrNull(index)?.toString() ?: "no data" }
        }
        val actualData = associatedReplica.getData(DEFAULT_KEY)

        assertEquals(data[DEFAULT_KEY].toString(), actualData)
    }

    @Test
    fun `observes data by definite key`() = runTest {
        val data = String(CharArray(26) { Char(it + 97) }) // latin alphabet in lowercase
        val replica = replicaProvider.replica(fetcher = { data })

        val associatedReplica = associate { index: Int ->
            replica.map { it.getOrNull(index)?.toString() ?: "no data" }
        }
        val observer = associatedReplica.observe(
            TestScope(),
            MutableStateFlow(true),
            MutableStateFlow(DEFAULT_KEY)
        )
        associatedReplica.refresh(DEFAULT_KEY)
        runCurrent()

        assertEquals(Loadable(data = data[DEFAULT_KEY].toString()), observer.currentState)
    }

    @Test
    fun `observes error state when error throws`() = runTest {
        val data = String(CharArray(26) { Char(it + 97) }) // latin alphabet in lowercase
        val replica = replicaProvider.replica(fetcher = { data })
        val error = LoadingFailedException()
        val key = data.length + 1

        val associatedReplica = associate { index: Int ->
            replica.map { it.getOrElse(index) { throw  error } }
        }
        val observer = associatedReplica.observe(
            TestScope(),
            MutableStateFlow(true),
            MutableStateFlow(key)
        )
        associatedReplica.refresh(key)
        runCurrent()

        assertEquals(Loadable<String>(error = CombinedLoadingError(error)), observer.currentState)
    }

    @Test
    fun `observes new data when key is changed`() = runTest {
        val data = String(CharArray(26) { Char(it + 97) }) // latin alphabet in lowercase
        val replica = replicaProvider.replica(fetcher = { data })
        val changedKey = DEFAULT_KEY + 1
        val associatedReplica = associate { index: Int ->
            replica.map { it.getOrNull(index)?.toString() ?: "no data" }
        }

        val observerKey = MutableStateFlow(DEFAULT_KEY)
        val observer = associatedReplica.observe(
            TestScope(),
            MutableStateFlow(true),
            observerKey
        )
        associatedReplica.refresh(DEFAULT_KEY)
        observerKey.value = changedKey
        // associatedReplica.refresh(changedKey) TODO why is working with comment?
        runCurrent()

        assertEquals(
            Loadable(data = data[changedKey].toString()),
            observer.currentState
        )
    }

    @Test
    fun `observes changed data when original replica changed data`() = runTest {
        val data = String(CharArray(26) { Char(it + 97) }) // latin alphabet in lowercase
        val changedData = data.reversed()
        val replica = replicaProvider.replica(fetcher = { data })
        val associatedReplica = associate { index: Int ->
            replica.map { it.getOrNull(index)?.toString() ?: "no data" }
        }

        val observer = associatedReplica.observe(
            TestScope(),
            MutableStateFlow(true),
            MutableStateFlow(DEFAULT_KEY)
        )
        associatedReplica.refresh(DEFAULT_KEY)
        runCurrent()
        replica.setData(changedData)
        runCurrent()

        assertEquals(
            Loadable(data = changedData[DEFAULT_KEY].toString()),
            observer.currentState
        )
    }
}