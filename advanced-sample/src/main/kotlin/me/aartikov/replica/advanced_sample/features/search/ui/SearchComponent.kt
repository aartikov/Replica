package me.aartikov.replica.advanced_sample.features.search.ui

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchItem
import me.aartikov.replica.single.Loadable
import ru.mobileup.kmm_form_validation.control.InputControl

interface SearchComponent {

    val wikiItemsState: StateFlow<Loadable<List<WikiSearchItem>>>

    val queryInputControl: InputControl

    val debouncedQuery: StateFlow<String>

    fun onItemClick(item: WikiSearchItem)

    fun onRefresh()
}
