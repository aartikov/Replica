package me.aartikov.replica.utils

import me.aartikov.replica.common.ReplicaTag

@JvmInline
value class TestReplicaTag(val value: String = "test_tag") : ReplicaTag