package me.aartikov.replica.advanced_sample.features.project.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AndroidSettings(
    private val sharedPreferences: SharedPreferences,
    private val dispatcher: CoroutineDispatcher
) : Settings {

    override suspend fun getString(key: String): String? = withContext(dispatcher) {
        sharedPreferences.getString(key, null)
    }

    override suspend fun putString(key: String, value: String) = withContext(dispatcher) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    override suspend fun remove(key: String) = withContext(dispatcher) {
        sharedPreferences.edit {
            remove(key)
        }
    }
}