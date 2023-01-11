package me.aartikov.replica.keyed.behaviour

import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.behaviour.standard.LimitChildCount


internal fun <K : Any, T : Any> createBehavioursForKeyedReplicaSettings(
    settings: KeyedReplicaSettings<K, T>
) = buildList<KeyedReplicaBehaviour<K, T>> {

    if (settings.maxCount != Int.MAX_VALUE) {
        add(
            LimitChildCount(settings.maxCount, settings.clearPolicy)
        )
    }
}