package me.aartikov.replica.common

import me.aartikov.replica.client.cancelByTags
import me.aartikov.replica.client.clearByTags
import me.aartikov.replica.client.invalidateByTags

/**
 * Can be used to perform bulk operations on a subset of replicas. See: [cancelByTags], [clearByTags], [invalidateByTags].
 */
interface ReplicaTag