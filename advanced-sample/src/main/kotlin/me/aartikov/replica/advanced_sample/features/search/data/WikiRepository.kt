package me.aartikov.replica.advanced_sample.features.search.data

import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchResult
import me.aartikov.replica.keyed.KeyedReplica

interface WikiRepository {

    val searchReplica: KeyedReplica<String, WikiSearchResult>
}
