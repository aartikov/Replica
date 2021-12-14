package me.aartikov.replica.sample.features.project.ui

import me.aartikov.replica.sample.features.project.domain.Project
import me.aartikov.replica.simple.Loadable

interface ProjectComponent {

    val projectState: Loadable<Project>

    fun onRefresh()

    fun onRetryClick()

    fun onUrlClick(url: String)
}