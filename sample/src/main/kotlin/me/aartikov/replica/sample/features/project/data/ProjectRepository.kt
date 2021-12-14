package me.aartikov.replica.sample.features.project.data

import me.aartikov.replica.sample.features.project.domain.Project
import me.aartikov.replica.simple.Replica

interface ProjectRepository {

    val projectReplica: Replica<Project>
}