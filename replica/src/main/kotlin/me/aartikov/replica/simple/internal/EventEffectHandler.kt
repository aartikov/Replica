package me.aartikov.replica.simple.internal

import me.aartikov.replica.simple.ReplicaEvent
import me.aartikov.sesame.loop.EffectHandler

internal class EventEffectHandler<T : Any>(private val emitEvent: suspend (ReplicaEvent<T>) -> Unit) :
    EffectHandler<Effect<T>, Action<T>> {

    override suspend fun handleEffect(effect: Effect<T>, actionConsumer: (Action<T>) -> Unit) {
        if (effect is Effect.EmitEvent) {
            emitEvent(effect.event)
        }
    }
}