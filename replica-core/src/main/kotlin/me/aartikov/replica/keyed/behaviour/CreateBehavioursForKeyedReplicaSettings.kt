package me.aartikov.replica.keyed.behaviour

import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.behaviour.standard.ClearOnMaxCountExceed


internal fun <K : Any, T : Any> createBehavioursForKeyedReplicaSettings(
    settings: KeyedReplicaSettings<K, T>
) = buildList<KeyedReplicaBehaviour<K, T>> {

    if (settings.maxCount != Int.MAX_VALUE) {
        add(
            ClearOnMaxCountExceed(settings.maxCount, settings.clearPolicy)
        )
    }
}