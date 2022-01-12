package me.aartikov.replica.sample.features.dudes.data

import retrofit2.http.GET
import retrofit2.http.Query

interface DudeApi {

    @GET("/api/name/random_name")
    suspend fun getRandomDudes(@Query("size") count: Int): List<DudeResponse>
}