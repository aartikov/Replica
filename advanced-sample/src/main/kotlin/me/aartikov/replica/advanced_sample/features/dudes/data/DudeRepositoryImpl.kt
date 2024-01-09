package me.aartikov.replica.advanced_sample.features.dudes.data

import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedFetcher
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaSettings

class DudeRepositoryImpl(
    replicaClient: ReplicaClient,
    private val api: DudeApi
) : DudeRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override val dudesReplica: PagedPhysicalReplica<Dude, Page<Dude>> =
        replicaClient.createPagedReplica(
            name = "dudes",
            settings = PagedReplicaSettings(staleTime = null),
            idExtractor = { it.id },
            fetcher = object : PagedFetcher<Dude, Page<Dude>> {

                override suspend fun fetchFirstPage(): Page<Dude> {
                    val dudes = api.getRandomDudes(count = PAGE_SIZE).map { it.toDomain() }
                    return Page(
                        items = dudes,
                        hasNextPage = dudes.size >= PAGE_SIZE,
                        hasPreviousPage = false
                    )
                }

                override suspend fun fetchNextPage(currentData: PagedData<Dude, Page<Dude>>): Page<Dude> {
                    val lastPage = currentData.items.size >= 300
                    val dudes = api.getRandomDudes(
                        count = if (lastPage) PAGE_SIZE / 2 else PAGE_SIZE
                    ).map { it.toDomain() }

                    return Page(
                        items = dudes,
                        hasNextPage = dudes.size >= PAGE_SIZE,
                        hasPreviousPage = true
                    )
                }
            }
        )
}