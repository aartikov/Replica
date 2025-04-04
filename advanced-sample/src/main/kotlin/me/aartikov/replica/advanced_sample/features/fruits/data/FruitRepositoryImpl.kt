package me.aartikov.replica.advanced_sample.features.fruits.data

import kotlinx.coroutines.delay
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendActionWithOptimisticUpdate
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.provideOptimisticUpdate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class FruitRepositoryImpl(
    private val replicaClient: ReplicaClient,
    private val api: FruitApi
) : FruitRepository {

    override val fruitsReplica: PhysicalReplica<List<Fruit>> = replicaClient.createReplica(
        name = "fruits",
        settings = ReplicaSettings(staleTime = 30.seconds),
        fetcher = {
            api.getFruits().map { it.toDomain() }
        },
        behaviours = listOf(
            ReplicaBehaviour.provideOptimisticUpdate { action: SetFruitLikedAction ->
                OptimisticUpdate { fruits ->
                    fruits.map {
                        if (it.id == action.fruitId) it.copy(liked = action.liked) else it
                    }
                }
            }
        )
    )

    override suspend fun setFruitLiked(fruitId: FruitId, liked: Boolean, debounceDelay: Duration) {
        replicaClient.sendActionWithOptimisticUpdate(
            action = SetFruitLikedAction(fruitId, liked)
        ) {
            delay(debounceDelay)
            if (liked) {
                api.likeFruit(fruitId.value)
            } else {
                api.dislikeFruit(fruitId.value)
            }
        }
    }
}