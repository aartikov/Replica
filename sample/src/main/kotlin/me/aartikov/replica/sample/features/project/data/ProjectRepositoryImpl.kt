package me.aartikov.replica.sample.features.project.data

import kotlinx.coroutines.delay
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
        settings = ReplicaSettings(staleTime = 5.seconds),
    ) {
        delay(1000) // Delay, because Github api is too fast
        api.getProject("aartikov", "Replica").toDomain()
    }
}