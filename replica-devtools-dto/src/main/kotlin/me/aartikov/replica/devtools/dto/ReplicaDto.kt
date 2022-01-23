package me.aartikov.replica.devtools.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReplicaDto(
    val id: String,
    val name: String,
    var state: ReplicaStateDto
)

@Serializable
data class ReplicaStateDto(
    val loading: Boolean,
    val hasData: Boolean,
    val hasError: Boolean,
    val dataIsFresh: Boolean,
    val observerCount: Int,
    val activeObserverCount: Int
)