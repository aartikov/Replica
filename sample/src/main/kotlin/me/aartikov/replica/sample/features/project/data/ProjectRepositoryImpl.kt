package me.aartikov.replica.sample.features.project.data

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.sample.features.project.domain.Project
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import kotlin.time.Duration.Companion.seconds

class ProjectRepositoryImpl(
    replicaClient: ReplicaClient,
    api: ProjectApi
) : ProjectRepository {

    override val projectReplica: PhysicalReplica<Project> = replicaClient.createReplica(
        name = "project",
        settings = ReplicaSettings(
            staleTime = 5.seconds,
            clearTime = 10.seconds
        ),
        fetcher = {
            api.getProject("aartikov", "Replica").toDomain()
        }
    )
}