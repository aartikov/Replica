package me.aartikov.replica.advanced_sample.features.project.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.aartikov.replica.advanced_sample.features.project.domain.Project

@Serializable
class ProjectResponse(
    @SerialName("name") val name: String,
    @SerialName("html_url") val url: String,
    @SerialName("stargazers_count") val starsCount: Int,
    @SerialName("forks_count") val forksCount: Int,
    @SerialName("subscribers_count") val subscribersCount: Int
)

fun ProjectResponse.toDomain(): Project {
    return Project(
        name = name,
        url = url,
        starsCount = starsCount,
        forksCount = forksCount,
        subscribersCount = subscribersCount
    )
}