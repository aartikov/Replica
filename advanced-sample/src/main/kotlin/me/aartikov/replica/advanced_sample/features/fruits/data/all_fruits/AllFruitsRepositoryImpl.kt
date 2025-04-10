package me.aartikov.replica.advanced_sample.features.fruits.data.all_fruits

import kotlinx.coroutines.delay
import me.aartikov.replica.advanced_sample.features.fruits.data.SetFruitIsFavouriteAction
import me.aartikov.replica.advanced_sample.features.fruits.data.api.FruitApi
import me.aartikov.replica.advanced_sample.features.fruits.data.dto.toDomain
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.advanced_sample.features.fruits.domain.withUpdatedIsFavourite
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendActionWithOptimisticUpdate
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.standard.provideOptimisticUpdate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AllFruitsRepositoryImpl(
    private val replicaClient: ReplicaClient,
    private val api: FruitApi
) : AllFruitsRepository {

    override val fruitsReplica: PhysicalReplica<List<Fruit>> = replicaClient.createReplica(
        name = "fruits",
        settings = ReplicaSettings(staleTime = 30.seconds),
        fetcher = {
            api.getFruits().map { it.toDomain() }
        },
        behaviours = listOf(
            ReplicaBehaviour.provideOptimisticUpdate { action: SetFruitIsFavouriteAction ->
                OptimisticUpdate { fruits ->
                    fruits.withUpdatedIsFavourite(action.fruitId, action.isFavourite)
                }
            },
            ReplicaBehaviour.doOnAction { action: SetFruitIsFavouriteAction ->
                mutateData {
                    it.withUpdatedIsFavourite(action.fruitId, action.isFavourite)
                }
            }
        )
    )

    override suspend fun setFruitFavourite(
        fruitId: FruitId,
        isFavourite: Boolean,
        debounceDelay: Duration
    ) {
        replicaClient.sendActionWithOptimisticUpdate(
            action = SetFruitIsFavouriteAction(fruitId, isFavourite)
        ) {
            delay(debounceDelay)
            if (isFavourite) {
                api.likeFruit(fruitId.value)
            } else {
                api.dislikeFruit(fruitId.value)
            }
        }
    }
}