package me.aartikov.replica.devtools.internal

data class ReplicaInfo(
    val id: String,
    val name: String,
    var details: String = "",
    val childInfos: MutableMap<String, ReplicaInfo> = mutableMapOf()
)