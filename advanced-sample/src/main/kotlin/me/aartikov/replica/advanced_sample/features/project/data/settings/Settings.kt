package me.aartikov.replica.advanced_sample.features.project.data.settings

interface Settings {

    suspend fun getString(key: String): String?

    suspend fun putString(key: String, value: String)

    suspend fun remove(key: String)
}