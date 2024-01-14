package me.aartikov.replica.advanced_sample.features.dudes.data

import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesContent
import me.aartikov.replica.paged.PagedReplica

interface DudeRepository {

    val dudesReplica: PagedReplica<DudesContent>
}