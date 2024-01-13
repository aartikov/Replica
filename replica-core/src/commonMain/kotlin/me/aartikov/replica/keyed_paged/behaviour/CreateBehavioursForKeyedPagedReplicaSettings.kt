package me.aartikov.replica.keyed_paged.behaviour

import me.aartikov.replica.keyed_paged.KeyedPagedReplicaSettings
import me.aartikov.replica.keyed_paged.behaviour.standard.LimitChildCount
import me.aartikov.replica.paged.Page


internal fun <K : Any, T : Any, P : Page<T>> createBehavioursForKeyedPagedReplicaSettings(
    settings: KeyedPagedReplicaSettings<K, T, P>
) = buildList<KeyedPagedReplicaBehaviour<K, T, P>> {

    if (settings.maxCount != Int.MAX_VALUE) {
        add(
            LimitChildCount(settings.maxCount, settings.clearPolicy)
        )
    }
}