package me.aartikov.replica.algebra.paged

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.paged.PagedReplica

/**
 * Creates paged replica with a const data.
 */
fun <T : Any> constPagedReplica(data: T): PagedReplica<T> {
    return flowPagedReplica(MutableStateFlow(data))
}