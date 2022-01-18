package me.aartikov.replica.time

import kotlinx.datetime.Instant

interface TimeProvider {
    val currentTime: Instant
}