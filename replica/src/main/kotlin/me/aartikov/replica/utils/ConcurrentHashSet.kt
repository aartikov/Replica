package me.aartikov.replica.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal fun <T> concurrentHashSetOf() = Collections.newSetFromMap(ConcurrentHashMap<T, Boolean>())