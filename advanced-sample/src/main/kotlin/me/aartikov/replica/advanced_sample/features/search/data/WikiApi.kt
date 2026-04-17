package me.aartikov.replica.advanced_sample.features.search.data

import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface WikiApi {

    @Headers("User-Agent: ReplicaAdvancedSampleSearch/1.0 (https://github.com/aartikov/Replica)")
    @GET("w/api.php?action=opensearch&limit=30&namespace=0&format=json")
    suspend fun search(@Query("search") query: String): List<JsonElement>
}
