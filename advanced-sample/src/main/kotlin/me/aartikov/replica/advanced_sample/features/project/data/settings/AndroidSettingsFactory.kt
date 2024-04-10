package me.aartikov.replica.advanced_sample.features.project.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineDispatcher

class AndroidSettingsFactory(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : SettingsFactory {

    override fun createSettings(name: String): Settings {
        return AndroidSettings(createSharedPreferences(name), dispatcher)
    }

    private fun createSharedPreferences(name: String): SharedPreferences {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}