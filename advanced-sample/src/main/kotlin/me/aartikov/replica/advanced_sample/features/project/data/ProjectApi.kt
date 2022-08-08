package me.aartikov.replica.advanced_sample.features.project.data

import retrofit2.http.GET
import retrofit2.http.Path

interface ProjectApi {

    @GET("repos/{userName}/{projectName}")
    suspend fun getProject(
        @Path("userName") userName: String,
        @Path("projectName") projectName: String
    ): ProjectResponse
}