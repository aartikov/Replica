package me.aartikov.replica.sample.features.project.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.aartikov.replica.sample.features.project.domain.Project
import me.aartikov.replica.single.Storage

class ProjectStorage(context: Context) : Storage<Project> {

    private val preferences = context.getSharedPreferences("project", Context.MODE_PRIVATE)


    override suspend fun write(data: Project) {
        preferences.edit {
            putString("project", Json.encodeToString(data))
        }
    }

    override suspend fun read(): Project? {
        val json = preferences.getString("project", null) ?: return null
        return Json.decodeFromString(json)
    }

    override suspend fun remove() {
        preferences.edit {
            remove("project")
        }
    }
}