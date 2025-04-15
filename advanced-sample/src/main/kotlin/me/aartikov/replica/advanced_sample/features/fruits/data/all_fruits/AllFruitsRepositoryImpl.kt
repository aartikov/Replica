package me.aartikov.replica.advanced_sample.features.fruits.data.all_fruits

import me.aartikov.replica.advanced_sample.features.fruits.data.SetFruitFavouriteAction
import me.aartikov.replica.advanced_sample.features.fruits.data.api.FruitApi
import me.aartikov.replica.advanced_sample.features.fruits.data.dto.toDomain
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.withUpdatedIsFavourite
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.mutateOnAction
import kotlin.time.Duration.Companion.seconds

class AllFruitsRepositoryImpl(
    replicaClient: ReplicaClient,
    private val api: FruitApi
) : AllFruitsRepository {

    override val fruitsReplica: PhysicalReplica<List<Fruit>> = replicaClient.createReplica(
        name = "fruits",
        settings = ReplicaSettings(staleTime = 30.seconds),
        fetcher = {
            api.getFruits().map { it.toDomain() }
        },
        behaviours = listOf(
            ReplicaBehaviour.mutateOnAction { action: SetFruitFavouriteAction, fruits ->
                fruits.withUpdatedIsFavourite(action.fruitId, action.isFavourite)
            }
        )
    )
}