package me.aartikov.replica.advanced_sample.features.dudes.data

import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings

class DudeRepositoryImpl(
    replicaClient: ReplicaClient,
    private val api: DudeApi
) : DudeRepository {

    override val dudesReplica: PhysicalReplica<List<Dude>> = replicaClient.createReplica(
        name = "dudes",
        settings = ReplicaSettings(staleTime = null),
        fetcher = {
            api.getRandomDudes(count = 20).map { it.toDomain() }
        }
    )
}