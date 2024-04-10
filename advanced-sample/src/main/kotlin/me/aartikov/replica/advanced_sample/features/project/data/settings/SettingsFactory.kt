package me.aartikov.replica.advanced_sample.features.project.data.settings

interface SettingsFactory {

    fun createSettings(name: String): Settings

}