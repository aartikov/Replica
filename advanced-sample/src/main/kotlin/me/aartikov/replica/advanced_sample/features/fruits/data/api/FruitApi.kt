package me.aartikov.replica.advanced_sample.features.fruits.data.api

import me.aartikov.replica.advanced_sample.features.fruits.data.dto.FruitResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FruitApi {

    @GET("/api/fruits")
    suspend fun getFruits(): List<FruitResponse>

    @GET("/api/fruits/favourite")
    suspend fun getFavouriteFruits(): List<FruitResponse>

    @POST("/api/fruits/like/{id}")
    suspend fun likeFruit(@Path("id") fruitId: String)

    @POST("/api/fruits/dislike/{id}")
    suspend fun dislikeFruit(@Path("id") fruitId: String)
}