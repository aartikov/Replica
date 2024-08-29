package me.aartikov.replica.algebra.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun ObserverScope() = CoroutineScope(Dispatchers.Main.immediate)