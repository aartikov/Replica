package me.aartikov.replica.advanced_sample.features.search.data

import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

interface WikiApi {

    @GET("w/api.php?action=opensearch&limit=30&namespace=0&format=json")
    suspend fun search(@Query("search") query: String): List<JsonElement>
}
