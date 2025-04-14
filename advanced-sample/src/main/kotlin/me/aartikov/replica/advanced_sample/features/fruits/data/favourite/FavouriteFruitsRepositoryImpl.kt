package me.aartikov.replica.advanced_sample.features.fruits.data.favourite

import kotlinx.coroutines.delay
import me.aartikov.replica.advanced_sample.features.fruits.data.SetFruitFavouriteAction
import me.aartikov.replica.advanced_sample.features.fruits.data.api.FruitApi
import me.aartikov.replica.advanced_sample.features.fruits.data.dto.toDomain
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.advanced_sample.features.fruits.domain.withUpdatedIsFavourite
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.sendActionWithOptimisticUpdate
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.standard.provideOptimisticUpdate
import kotlin.time.Duration
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
            ReplicaBehaviour.provideOptimisticUpdate { action: SetFruitFavouriteAction ->
                OptimisticUpdate { fruits ->
                    fruits.withUpdatedIsFavourite(action.fruitId, action.isFavourite)
                }
            },
            ReplicaBehaviour.doOnAction { action: SetFruitFavouriteAction ->
                when (action.isFavourite) {
                    true -> invalidate()
                    false -> mutateData { favourites ->
                        favourites.filter { it.id != action.fruitId }
                    }
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
            action = SetFruitFavouriteAction(fruitId, isFavourite)
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