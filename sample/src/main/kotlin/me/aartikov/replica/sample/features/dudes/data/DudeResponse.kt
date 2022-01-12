package me.aartikov.replica.sample.features.dudes.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.aartikov.replica.sample.features.dudes.domain.Dude
import me.aartikov.replica.sample.features.dudes.domain.DudeId
import java.net.URLEncoder

@Serializable
data class DudeResponse(
    @SerialName("uid") val uid: String,
    @SerialName("name") val name: String
)

fun DudeResponse.toDomain(): Dude {
    return Dude(
        id = DudeId(uid),
        name = name,
        photoUrl = getPhotoUrl(size = 240, name = name)
    )
}

@Suppress("SameParameterValue")
private fun getPhotoUrl(size: Int, name: String): String {
    val urlEncodedName = URLEncoder.encode(name, "UTF-8")
    return "https://adorable-avatars.broken.services/$size/$urlEncodedName"
}