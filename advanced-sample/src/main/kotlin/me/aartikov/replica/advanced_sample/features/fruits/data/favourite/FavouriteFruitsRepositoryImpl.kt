package me.aartikov.replica.advanced_sample.features.fruits.data.favourite

import me.aartikov.replica.advanced_sample.features.fruits.data.SetFruitIsFavouriteAction
import me.aartikov.replica.advanced_sample.features.fruits.data.api.FruitApi
import me.aartikov.replica.advanced_sample.features.fruits.data.dto.toDomain
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.advanced_sample.features.fruits.domain.withUpdatedIsFavourite
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.standard.provideOptimisticUpdate
import kotlin.time.Duration.Companion.seconds

class FavouriteFruitsRepositoryImpl(
    private val replicaClient: ReplicaClient,
    private val api: FruitApi
) : FavouriteFruitsRepository {
    override val favouriteFruitsReplica = replicaClient.createReplica(
        name = "favouriteFruits",
        settings = ReplicaSettings(staleTime = 30.seconds),
        fetcher = {
            api.getFavouriteFruits().map { it.toDomain() }
        },
        behaviours = listOf(
            ReplicaBehaviour.provideOptimisticUpdate { action: SetFruitIsFavouriteAction ->
                OptimisticUpdate { fruits ->
                    fruits.withUpdatedIsFavourite(action.fruitId, action.isFavourite)
                }
            },
            ReplicaBehaviour.doOnAction { action: SetFruitIsFavouriteAction ->
                when (action.isFavourite) {
                    true -> refresh()
                    false -> mutateData { favourites ->
                        favourites.filter { it.id != action.fruitId }
                    }
                }
            }
        )
    )

    override suspend fun removeFromFavourite(fruitId: FruitId) {
        api.dislikeFruit(fruitId.value)
        replicaClient.sendAction(
            SetFruitIsFavouriteAction(
                fruitId = fruitId,
                isFavourite = false
            )
        )
    }
}