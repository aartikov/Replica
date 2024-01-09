package me.aartikov.replica.advanced_sample.features.dudes.data

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface DudeApi {

    @GET("/api/Name")
    @Headers("X-Api-Key: e22400a6907844cf866b8dcfd6bdd0ff")
    suspend fun getRandomDudes(
        @Query("quantity") count: Int,
        @Query("nameType") nameType: String = "fullname"
    ): List<DudeResponse>
}