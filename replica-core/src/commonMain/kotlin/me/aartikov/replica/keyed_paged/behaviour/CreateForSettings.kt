package me.aartikov.replica.keyed_paged.behaviour

import me.aartikov.replica.keyed_paged.KeyedPagedReplicaSettings
import me.aartikov.replica.keyed_paged.behaviour.standard.limitChildCount
import me.aartikov.replica.paged.Page


internal fun <K : Any, I : Any, P : Page<I>> KeyedPagedReplicaBehaviour.Companion.createForSettings(
    settings: KeyedPagedReplicaSettings<K, I, P>
) = buildList {

    if (settings.maxCount != Int.MAX_VALUE) {
        add(
            KeyedPagedReplicaBehaviour.limitChildCount(settings.maxCount, settings.clearPolicy)
        )
    }
}