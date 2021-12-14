package me.aartikov.replica.sample.features.project.data

import kotlinx.coroutines.delay
import me.aartikov.replica.client.ReplicaClient

class ProjectRepositoryImpl(
    private val api: ProjectApi,
    replicaClient: ReplicaClient
) : ProjectRepository {

    override val projectReplica = replicaClient.createReplica {
        delay(1000) // Delay, because Github api is too fast
        api.getProject("aartikov", "Sesame").toDomain()
    }
}