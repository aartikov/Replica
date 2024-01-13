package me.aartikov.replica.advanced_sample.features.dudes.data

import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesContent
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesPage
import me.aartikov.replica.algebra.paged.map
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.keyed_paged.KeyedPagedFetcher
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedReplicaSettings

class DudeRepositoryImpl(
    replicaClient: ReplicaClient,
    private val api: DudeApi
) : DudeRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override val dudesReplica = replicaClient.createKeyedPagedReplica(
            name = "dudes",
            childName = { "dudes $it" },
            childSettings = { PagedReplicaSettings(staleTime = null) },
            idExtractor = { it.id },
            fetcher = object : KeyedPagedFetcher<String, Dude, DudesPage> {

            override suspend fun fetchFirstPage(key: String): DudesPage {
                val dudes = api.getRandomDudes(count = PAGE_SIZE).map { it.toDomain(key) }
                return DudesPage(
                    items = dudes,
                    nextPageCursor = if (dudes.size >= PAGE_SIZE) "a" else null,
                    )
                }

                override suspend fun fetchNextPage(key: String, currentData: PagedData<Dude, DudesPage>): DudesPage {
                    val nextPageCursor = currentData.pages.last().nextPageCursor
                        ?: throw IllegalArgumentException("nextPageCursor can't be null here")

                    val lastPage = nextPageCursor.length == 10
                    val dudes = api.getRandomDudes(count = PAGE_SIZE).map { it.toDomain(key) }

                    return DudesPage(
                        items = dudes,
                        nextPageCursor = if (lastPage) null else nextPageCursor + "a",
                    )
                }
        }
    ).map { _, data ->
        DudesContent(data.items, data.hasNextPage)
    }
}