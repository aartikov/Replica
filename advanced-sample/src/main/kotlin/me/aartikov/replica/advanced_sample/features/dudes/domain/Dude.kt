package me.aartikov.replica.advanced_sample.features.dudes.domain

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class DudeId(val value: String)

data class Dude(
    val id: DudeId,
    val name: String,
    val photoUrl: String
) {

    companion object {
        val FAKE_LIST = listOf(
            Dude(
                id = DudeId("1"),
                name = "Leanne Graham",
                photoUrl = ""
            ),
            Dude(
                id = DudeId("2"),
                name = "Ervin Howell",
                photoUrl = ""
            ),
            Dude(
                id = DudeId("3"),
                name = "Clementine Bauch",
                photoUrl = ""
            )
        )
    }
}