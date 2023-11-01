package me.aartikov.replica.advanced_sample.features.project.ui

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.project.domain.Project
import me.aartikov.replica.single.Loadable

interface ProjectComponent {

    val projectState: StateFlow<Loadable<Project>>

    fun onRefresh()

    fun onRetryClick()

    fun onUrlClick(url: String)
}