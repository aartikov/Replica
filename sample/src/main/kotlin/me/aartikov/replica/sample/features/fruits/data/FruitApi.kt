package me.aartikov.replica.sample.features.fruits.data

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FruitApi {

    @GET("/api/fruits")
    suspend fun getFruits(): List<FruitResponse>

    @POST("/api/fruits/like/{id}")
    suspend fun likeFruit(@Path("id") fruitId: String)

    @POST("/api/fruits/dislike/{id}")
    suspend fun dislikeFruit(@Path("id") fruitId: String)
}