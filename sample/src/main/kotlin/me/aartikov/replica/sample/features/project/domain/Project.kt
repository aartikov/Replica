package me.aartikov.replica.sample.features.project.domain

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val name: String,
    val url: String,
    val starsCount: Int,
    val forksCount: Int,
    val subscribersCount: Int
)