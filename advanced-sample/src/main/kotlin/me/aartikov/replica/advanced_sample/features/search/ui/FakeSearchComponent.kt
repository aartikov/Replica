package me.aartikov.replica.advanced_sample.features.search.ui

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.advanced_sample.core.utils.fakeInputControl
import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchItem
import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchResult
import me.aartikov.replica.single.Loadable

class FakeSearchComponent : SearchComponent {

    override val wikiSearchResultState = MutableStateFlow(
        Loadable<WikiSearchResult>(data = WikiSearchResult.MOCK)
    )

    override val queryInputControl = fakeInputControl("Replica")

    override fun onItemClick(item: WikiSearchItem) = Unit

    override fun onRefresh() = Unit
}
