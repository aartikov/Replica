package me.aartikov.replica.advanced_sample.features.search.domain

data class WikiSearchResult(
    val query: String,
    val items: List<WikiSearchItem>
) {
    companion object {
        val MOCK = WikiSearchResult(
            query = "Replica",
            items = listOf(
                WikiSearchItem(
                    title = "Replica",
                    description = "Kotlin Multiplatform library for organizing of network communication in a declarative way",
                    url = "https://github.com/aartikov/Replica"
                )
            )
        )
    }
}
