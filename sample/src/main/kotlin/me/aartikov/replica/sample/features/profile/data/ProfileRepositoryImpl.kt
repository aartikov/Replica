package me.aartikov.replica.sample.features.profile.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.sample.features.profile.domain.Profile

class ProfileRepositoryImpl(replicaClient: ReplicaClient) : ProfileRepository {

    private var counter = 0

    override val profileReplica = replicaClient.createReplica {
        loadProfile()
    }

    private suspend fun loadProfile(): Profile = withContext(Dispatchers.IO) {
        delay(1000)
        val success = counter % 2 == 1
        counter++

        if (success) {
            Profile(
                name = "John Smith",
                avatarUrl = "https://www.biography.com/.image/ar_1:1%2Cc_fill%2Ccs_srgb%2Cg_face%2Cq_auto:good%2Cw_300/MTIwNjA4NjMzOTc0MTk1NzI0/john-smith-9486928-1-402.jpg"
            )
        } else {
            throw RuntimeException("Emulated failure. Please, try again.")
        }
    }
}