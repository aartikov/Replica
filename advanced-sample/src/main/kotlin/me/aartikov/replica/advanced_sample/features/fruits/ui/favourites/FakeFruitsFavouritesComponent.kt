package me.aartikov.replica.advanced_sample.features.fruits.ui.favourites

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Loadable

class FakeFruitsFavouritesComponent : FruitsFavouritesComponent {
    override val fruitsState: StateFlow<Loadable<List<Fruit>>> = MutableStateFlow(
        Loadable(
            loading = true,
            data = listOf(
                Fruit(
                    id = FruitId("1"),
                    name = "Banana",
                    imageUrl = "",
                    isFavourite = false
                ),
                Fruit(
                    id = FruitId("2"),
                    name = "Orange",
                    imageUrl = "",
                    isFavourite = true
                ),
                Fruit(
                    id = FruitId("3"),
                    name = "Mango",
                    imageUrl = "",
                    isFavourite = false
                )
            )
        )
    )

    override val removingInProgress: StateFlow<Set<FruitId>> = MutableStateFlow(setOf())

    override fun onRemoveFruitClick(fruitId: FruitId): Unit = Unit

    override fun onRefresh(): Unit = Unit

    override fun onRetryClick(): Unit = Unit
}
