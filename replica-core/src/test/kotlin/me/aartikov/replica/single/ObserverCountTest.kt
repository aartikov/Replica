package me.aartikov.replica.single

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.client.ReplicaClient
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ObserverCountTest {

    val dispatcher = StandardTestDispatcher()

    @Test
    fun simpleTest() = runTest(dispatcher) {

        val replicaClient = ReplicaClient(
            coroutineDispatcher = dispatcher,
            coroutineScope = this
        )
        val replica = replicaClient.createReplica(
            "simple",
            settings = ReplicaSettings(staleTime = 1.seconds)
        ) {
            return@createReplica "string"
        }
        // val b = async {
        //     replica.observe(this, MutableStateFlow(true)).stateFlow.collect {
        //         println(it)
        //     }
        // }
        // println("here nothing ")
        // val a: ReplicaState<String>? = replica.stateFlow.firstOrNull()
        // b.await()
        // println("some here $a")
        replica.cancel()
    }
    // assertEquals(a?.observingState?.activeObserverCount, 0)
    // println("assert ended")

    @Test
    fun exampleTest() = runTest(dispatcher) {
        val deferred = async {
            delay(1_000)
            async {
                delay(1_000)
            }.await()
        }
        deferred.await() // result available immediately
    }

    private fun CoroutineScope.cancelJobs() {
        val job = coroutineContext[Job]!!
        job.children.drop(1)    // drop DeferredCoroutine from internals of runBlockingTest
            .forEach { it.cancel() }
    }
}