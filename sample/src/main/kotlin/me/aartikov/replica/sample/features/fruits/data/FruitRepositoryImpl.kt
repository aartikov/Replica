package me.aartikov.replica.sample.features.fruits.data

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.keyed.withOptimisticUpdate
import me.aartikov.replica.sample.features.fruits.domain.Fruit
import me.aartikov.replica.sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import kotlin.time.Duration.Companion.seconds

class FruitRepositoryImpl(
    replicaClient: ReplicaClient,
    private val api: FruitApi
) : FruitRepository {

    override val fruitsReplica: PhysicalReplica<List<Fruit>> = replicaClient.createReplica(
        name = "fruits",
        settings = ReplicaSettings(staleTime = 30.seconds)
    ) {
        api.getFruits().map { it.toDomain() }
    }

    override suspend fun setFruitLiked(fruitId: FruitId, liked: Boolean) {
        val updateFruitLiked = OptimisticUpdate<List<Fruit>> { fruits ->
            fruits.map {
                if (it.id == fruitId) it.copy(liked = liked) else it
            }
        }

        withOptimisticUpdate(updateFruitLiked, fruitsReplica) {
            if (liked) {
                api.likeFruit(fruitId.value)
            } else {
                api.dislikeFruit(fruitId.value)
            }
        }
    }
}