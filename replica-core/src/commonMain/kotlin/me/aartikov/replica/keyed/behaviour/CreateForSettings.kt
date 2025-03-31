package me.aartikov.replica.keyed.behaviour

import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.behaviour.standard.limitChildCount


internal fun <K : Any, T : Any> KeyedReplicaBehaviour.Companion.createForSettings(
    settings: KeyedReplicaSettings<K, T>
) = buildList {
    if (settings.maxCount != Int.MAX_VALUE) {
        add(
            KeyedReplicaBehaviour.limitChildCount(settings.maxCount, settings.clearPolicy)
        )
    }
}