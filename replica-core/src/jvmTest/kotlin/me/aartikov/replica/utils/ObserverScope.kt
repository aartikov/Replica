package me.aartikov.replica.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun ObserverScope() = CoroutineScope(Dispatchers.Main.immediate)