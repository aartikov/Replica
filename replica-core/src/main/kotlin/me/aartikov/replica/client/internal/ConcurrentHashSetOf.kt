package me.aartikov.replica.client.internal

import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal fun <T> concurrentHashSetOf() = Collections.newSetFromMap(ConcurrentHashMap<T, Boolean>())