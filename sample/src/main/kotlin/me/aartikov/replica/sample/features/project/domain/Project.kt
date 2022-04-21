package me.aartikov.replica.sample.features.project.domain

data class Project(
    val name: String,
    val url: String,
    val starsCount: Int,
    val forksCount: Int,
    val subscribersCount: Int
)