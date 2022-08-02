package me.aartikov.replica.sample.features.fruits.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.network.connected
import me.aartikov.replica.sample.core.error_handling.NoInternetException
import me.aartikov.replica.sample.core.error_handling.ServerException

class FakeFruitApi(
    private val networkConnectivityProvider: NetworkConnectivityProvider
) : FruitApi {

    private val names = listOf(
        "Apple", "Banana", "Cherry", "Dragon fruit", "Durian", "Grape", "Kiwi", "Lemon", "Lime",
        "Lychee", "Mandarin", "Mango", "Mangosteen", "Melon", "Orange", "Papaya", "Passion fruit",
        "Peach", "Pear", "Persimmon", "Pineapple", "Pomegranate", "Strawberry", "Watermelon"
    )

    private var fruits = createFruits()

    private val mutex = Mutex()

    override suspend fun getFruits(): List<FruitResponse> {
        emulateNetworkError()
        delay(800)
        return fruits
    }

    override suspend fun likeFruit(fruitId: String) {
        emulateNetworkError()
        emulateLikeError(fruitId)
        delay(500)
        setLiked(fruitId, true)
    }

    override suspend fun dislikeFruit(fruitId: String) {
        emulateNetworkError()
        delay(500)
        setLiked(fruitId, false)
    }

    private fun createFruits(): List<FruitResponse> {
        return names.mapIndexed { index, name ->
            FruitResponse(
                id = index.toString(),
                name = name,
                imageUrl = "file:///android_asset/fruits/$name.png",
                liked = false
            )
        }
    }

    private suspend fun emulateNetworkError() {
        if (!networkConnectivityProvider.connected) {
            delay(100)
            throw NoInternetException(cause = null)
        }
    }

    private suspend fun emulateLikeError(fruitId: String) {
        val fruit = fruits.find { it.id == fruitId } ?: return

        if (fruit.name == "Durian") {
            delay(300)
            throw ServerException(
                message = "Are you kidding? Nobody likes durian."
            )
        }

        val popularFruits = listOf("Banana", "Kiwi", "Mandarin", "Pear")
        if (fruit.name in popularFruits) {
            delay(300)
            throw ServerException(
                message = "The server is overloaded. Too many users like ${fruit.name.lowercase()}."
            )
        }
    }

    private suspend fun setLiked(fruitId: String, liked: Boolean) = mutex.withLock {
        fruits = fruits.map {
            if (it.id == fruitId) it.copy(liked = liked) else it
        }
    }
}