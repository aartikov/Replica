package me.aartikov.replica.advanced_sample.features.search.data

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchResult
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.single.ReplicaSettings
import kotlin.time.Duration.Companion.seconds

class WikiRepositoryImpl(
    replicaClient: ReplicaClient,
    api: WikiApi,
) : WikiRepository {

    override val searchReplica: KeyedPhysicalReplica<String, WikiSearchResult> =
        replicaClient.createKeyedReplica(
            name = "wikiSearch",
            settings = KeyedReplicaSettings(maxCount = 10),
            childName = { query -> "query = $query" },
            childSettings = {
                ReplicaSettings(
                    staleTime = 10.seconds,
                    clearTime = 60.seconds
                )
            },
            fetcher = { query ->
                val trimmedQuery = query.trim()

                if (trimmedQuery.isBlank()) {
                    WikiSearchResult(trimmedQuery, emptyList())
                } else {
                    val items = api.search(trimmedQuery).parseWikiSearchResponse().toDomain()
                    WikiSearchResult(trimmedQuery, items)
                }
            }
        )
}

private fun List<JsonElement>.parseWikiSearchResponse(): WikiSearchResponse {
    val query = getOrNull(0)?.jsonPrimitive?.contentOrNull ?: ""
    val titles = getOrNull(1)?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
    val descriptions = getOrNull(2)?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
    val urls = getOrNull(3)?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

    return WikiSearchResponse(
        query = query,
        titles = titles,
        descriptions = descriptions,
        urls = urls
    )
}
