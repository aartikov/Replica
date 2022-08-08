package me.aartikov.replica.advanced_sample.features.project.ui

import me.aartikov.replica.advanced_sample.features.project.domain.Project
import me.aartikov.replica.single.Loadable

interface ProjectComponent {

    val projectState: Loadable<Project>

    fun onRefresh()

    fun onRetryClick()

    fun onUrlClick(url: String)
}