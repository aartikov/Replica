package me.aartikov.replica.client

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.ReplicaClientProvider
import me.aartikov.replica.utils.TestReplicaTag
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClearByTagsTest {

    private val clientProvider = ReplicaClientProvider()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `replicas clears by single tag`() = runTest {
        val client = clientProvider.client()
        val replicasCount = 10
        val replicasCountWithTag = 10
        val tag = TestReplicaTag()

        repeat(replicasCount) { i ->
            val replica = client.createReplica(
                "testReplica$i",
                settings = ReplicaSettings.WithoutBehaviour,
                fetcher = { "test" }
            )
            replica.refresh()
        }
        repeat(replicasCountWithTag) { i ->
            val replica = client.createReplica(
                "testReplica$i",
                settings = ReplicaSettings.WithoutBehaviour,
                fetcher = { "test" },
                tags = setOf(tag)
            )
            replica.refresh()
        }
        runCurrent()
        client.clearByTags { tags -> tags.contains(tag) }

        client.onEachReplica {
            if (tags.contains(tag)) {
                assertNull(currentState.data)
            }
        }
    }

    @Test
    fun `replicas not clears if doesn't have tag for clear`() = runTest {
        val client = clientProvider.client()
        val replicasCount = 10
        val replicasCountWithTag = 10
        val tagForClear = TestReplicaTag()

        repeat(replicasCount) { i ->
            val replica = client.createReplica(
                "testReplica$i",
                settings = ReplicaSettings.WithoutBehaviour,
                fetcher = { "test" }
            )
            replica.refresh()
        }
        repeat(replicasCountWithTag) { i ->
            val replica = client.createReplica(
                "testReplica$i",
                settings = ReplicaSettings.WithoutBehaviour,
                fetcher = { "test" },
                tags = setOf(tagForClear)
            )
            replica.refresh()
        }
        runCurrent()
        client.clearByTags { tags -> tags.contains(tagForClear) }

        client.onEachReplica {
            if (!tags.contains(tagForClear)) {
                assertNotNull(currentState.data)
            }
        }
    }

    @Test
    fun `replicas clears by multiple tags`() = runTest {
        val client = clientProvider.client()
        val replicasCount = 10
        val replicasCountWithTagsForClearing = 10
        val tagsForClearing = setOf(
            TestReplicaTag("tagForClearing1"), TestReplicaTag("tagForClearing2")
        )
        val otherTags = setOf(TestReplicaTag("test1"), TestReplicaTag("test2"))

        repeat(replicasCount) { i ->
            val replica = client.createReplica(
                "testReplica$i",
                settings = ReplicaSettings.WithoutBehaviour,
                fetcher = { "test" },
                tags = otherTags
            )
            replica.refresh()
        }
        repeat(replicasCountWithTagsForClearing) { i ->
            val replica = client.createReplica(
                "testReplica$i",
                settings = ReplicaSettings.WithoutBehaviour,
                fetcher = { "test" },
                tags = tagsForClearing + otherTags
            )
            replica.refresh()
        }
        runCurrent()
        client.clearByTags { tags ->
            tags.containsAll(tagsForClearing)
        }

        client.onEachReplica {
            if (tags.containsAll(tagsForClearing)) {
                assertNull(currentState.data)
            }
        }
    }
}