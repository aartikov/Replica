package me.aartikov.replica.advanced_sample.features.dudes.data

import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudeId
import java.net.URLEncoder
import java.util.UUID

typealias DudeResponse = String

fun DudeResponse.toDomain(type: String): Dude {
    return Dude(
        id = DudeId(UUID.randomUUID().toString()),
        name = this,
        photoUrl = getPhotoUrl(size = 240, type = type, name = this)
    )
}

@Suppress("SameParameterValue")
private fun getPhotoUrl(size: Int, type: String, name: String): String {
    val urlEncodedName = URLEncoder.encode(name, "UTF-8")
    return "https://api.dicebear.com/7.x/$type/png?seed=$urlEncodedName&size=$size&radius=20"
}