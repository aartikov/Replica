package me.aartikov.replica.advanced_sample.features.dudes.data

import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.single.Replica

interface DudeRepository {

    val dudesReplica: Replica<List<Dude>>
}