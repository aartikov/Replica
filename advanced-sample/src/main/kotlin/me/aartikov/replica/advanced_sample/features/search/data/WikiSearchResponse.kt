package me.aartikov.replica.advanced_sample.features.search.data

import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchItem

data class WikiSearchResponse(
    val query: String,
    val titles: List<String>,
    val descriptions: List<String>,
    val urls: List<String>,
)

fun WikiSearchResponse.toDomain(): List<WikiSearchItem> {
    val result = mutableListOf<WikiSearchItem>()

    for (i in titles.indices) {
        result += WikiSearchItem(
            title = titles.getOrNull(i) ?: "",
            description = descriptions.getOrNull(i) ?: "",
            url = urls.getOrNull(i) ?: ""
        )
    }

    return result
}
