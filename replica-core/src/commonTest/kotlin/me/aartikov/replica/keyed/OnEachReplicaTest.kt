package me.aartikov.replica.keyed

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class OnEachReplicaTest {

    @Test
    fun removingCurrentChildDoesNotInterruptIteration() = withReplica { replica ->
        val original = replica.createChildren()
        runCurrent()
        val visited = mutableListOf<Int>()

        replica.onEachReplica { key ->
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

        replica.onEachReplica { key ->
            visited += key
            assertSame(original.getValue(key), this)
            if (visited.size == 1) {
                replica.setData(4, "data_4")
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
        var replacement: PhysicalReplica<String>? = null

        replica.onEachReplica { key ->
            visited += key
            assertSame(original.getValue(key), this)
            if (visited.size == 1) {
                replica.clear(replacedKey)
                runCurrent()
                assertNull(replica.getCurrentState(replacedKey))
                replica.onReplica(replacedKey) {
                    setData("data_$replacedKey")
                    replacement = this
                }
            }
        }

        assertEquals(original.keys.toList(), visited)
        assertNotSame(original.getValue(replacedKey), assertNotNull(replacement))
        replica.onReplica(replacedKey) {
            assertSame(replacement, this)
        }
    }

    @Test
    fun emptyReplicaDoesNotInvokeAction() = withReplica { replica ->
        replica.onEachReplica {
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

    private suspend fun KeyedPhysicalReplica<Int, String>.createChildren(): Map<Int, PhysicalReplica<String>> {
        val children = mutableMapOf<Int, PhysicalReplica<String>>()
        for (key in 1..3) {
            onReplica(key) {
                setData("data_$key")
                children[key] = this
            }
        }
        return children
    }

    private fun withReplica(
        action: suspend TestScope.(KeyedPhysicalReplica<Int, String>) -> Unit
    ) = runTest {
        val client = ReplicaClient(mainDispatcher = StandardTestDispatcher(testScheduler))
        try {
            val replica: KeyedPhysicalReplica<Int, String> = client.createKeyedReplica(
                name = "test",
                childName = { "child_$it" },
                settings = KeyedReplicaSettings(maxCount = Int.MAX_VALUE),
                childSettings = { ReplicaSettings.WithoutBehaviour },
                fetcher = { error("Unexpected fetch") }
            )
            action(replica)
        } finally {
            client.coroutineScope.cancel()
        }
    }
}
