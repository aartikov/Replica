package me.aartikov.replica.advanced_sample.features.dudes.data

import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesContent
import me.aartikov.replica.keyed_paged.KeyedPagedReplica

interface DudeRepository {

    val dudesReplica: KeyedPagedReplica<String, DudesContent>
}