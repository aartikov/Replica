package me.aartikov.replica.keyed_paged.behaviour

import me.aartikov.replica.keyed_paged.KeyedPagedReplicaSettings
import me.aartikov.replica.keyed_paged.behaviour.standard.LimitChildCount
import me.aartikov.replica.paged.Page


internal fun <K : Any, I : Any, P : Page<I>> createBehavioursForKeyedPagedReplicaSettings(
    settings: KeyedPagedReplicaSettings<K, I, P>
) = buildList<KeyedPagedReplicaBehaviour<K, I, P>> {

    if (settings.maxCount != Int.MAX_VALUE) {
        add(
            LimitChildCount(settings.maxCount, settings.clearPolicy)
        )
    }
}