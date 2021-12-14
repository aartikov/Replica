package me.aartikov.replica.sample.features.profile.data

import me.aartikov.replica.sample.features.profile.domain.Profile
import me.aartikov.replica.simple.Replica

interface ProfileRepository {

    val profileReplica: Replica<Profile>
}