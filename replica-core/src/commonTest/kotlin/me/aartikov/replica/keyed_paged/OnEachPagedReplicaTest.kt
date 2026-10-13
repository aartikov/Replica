package me.aartikov.replica.keyed_paged

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class OnEachPagedReplicaTest {

    @Test
    fun removingCurrentChildDoesNotInterruptIteration() = withReplica { replica ->
        val original = replica.createChildren()
        runCurrent()
        val visited = mutableListOf<Int>()

        replica.onEachPagedReplica { key ->
            visited += key
            assertSame(original.getValue(key), this)
            clear()
            runCurrent()
            assertNull(replica.getCurrentState(key))
            assertEquals(original.size - visited.size, replica.currentState.replicaCount)
        }

        assertEquals(original.keys.toList(), visited)
        assertEquals(0, replica.currentState.replicaCount)
    }

    @Test
    fun childAddedDuringIterationIsNotVisited() = withReplica { replica ->
        val original = replica.createChildren()
        val visited = mutableListOf<Int>()

        replica.onEachPagedReplica { key ->
            visited += key
            assertSame(original.getValue(key), this)
            if (visited.size == 1) {
                replica.setData(4, listOf(TestPage(listOf("data_4"))))
            }
        }

        assertEquals(original.keys.toList(), visited)
        assertNotNull(replica.getCurrentState(4)?.data)
        assertEquals(4, replica.currentState.replicaCount)
    }

    @Test
    fun recreatedChildDoesNotReplaceSnapshotReference() = withReplica { replica ->
        val original = replica.createChildren()
        runCurrent()
        val visited = mutableListOf<Int>()
        val replacedKey = original.keys.last()
        var replacement: PagedPhysicalReplica<String, TestPage>? = null

        replica.onEachPagedReplica { key ->
            visited += key
            assertSame(original.getValue(key), this)
            if (visited.size == 1) {
                replica.clear(replacedKey)
                runCurrent()
                assertNull(replica.getCurrentState(replacedKey))
                replica.onPagedReplica(replacedKey) {
                    setData(listOf(TestPage(listOf("data_$replacedKey"))))
                    replacement = this
                }
            }
        }

        assertEquals(original.keys.toList(), visited)
        assertNotSame(original.getValue(replacedKey), assertNotNull(replacement))
        replica.onPagedReplica(replacedKey) {
            assertSame(replacement, this)
        }
    }

    @Test
    fun emptyReplicaDoesNotInvokeAction() = withReplica { replica ->
        replica.onEachPagedReplica {
            error("No child should be visited")
        }
    }

    @Test
    fun clearAllRemovesEveryChild() = withReplica { replica ->
        val original = replica.createChildren()
        runCurrent()

        replica.clearAll()
        runCurrent()

        original.keys.forEach { key ->
            assertNull(replica.getCurrentState(key))
        }
        assertEquals(0, replica.currentState.replicaCount)
    }

    private suspend fun KeyedPagedPhysicalReplica<Int, String, TestPage>.createChildren()
        : Map<Int, PagedPhysicalReplica<String, TestPage>> {
        val children = mutableMapOf<Int, PagedPhysicalReplica<String, TestPage>>()
        for (key in 1..3) {
            onPagedReplica(key) {
                setData(listOf(TestPage(listOf("data_$key"))))
                children[key] = this
            }
        }
        return children
    }

    private fun withReplica(
        action: suspend TestScope.(KeyedPagedPhysicalReplica<Int, String, TestPage>) -> Unit
    ) = runTest {
        val client = ReplicaClient(mainDispatcher = StandardTestDispatcher(testScheduler))
        try {
            val replica = client.createKeyedPagedReplica(
                name = "test",
                childName = { "child_$it" },
                settings = KeyedPagedReplicaSettings(maxCount = Int.MAX_VALUE),
                childSettings = { PagedReplicaSettings.WithoutBehaviour },
                idExtractor = null,
                fetcher = object : KeyedPagedFetcher<Int, String, TestPage> {
                    override suspend fun fetchFirstPage(key: Int): TestPage =
                        error("Unexpected fetch")

                    override suspend fun fetchNextPage(
                        key: Int,
                        currentData: PagedData<String, TestPage>
                    ): TestPage = error("Unexpected fetch")
                }
            )
            action(replica)
        } finally {
            client.coroutineScope.cancel()
        }
    }

    private data class TestPage(override val items: List<String>) : Page<String> {
        override val hasNextPage = false
        override val hasPreviousPage = false
    }
}
