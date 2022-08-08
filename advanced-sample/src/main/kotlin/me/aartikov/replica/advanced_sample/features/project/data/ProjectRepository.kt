package me.aartikov.replica.advanced_sample.features.project.data

import me.aartikov.replica.advanced_sample.features.project.domain.Project
import me.aartikov.replica.single.Replica

interface ProjectRepository {

    val projectReplica: Replica<Project>
}