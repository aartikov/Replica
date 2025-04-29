package me.aartikov.replica.advanced_sample.features.search.ui

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.error_handling.safeRun
import me.aartikov.replica.advanced_sample.core.external_app_service.ExternalAppService
import me.aartikov.replica.advanced_sample.core.utils.componentCoroutineScope
import me.aartikov.replica.advanced_sample.core.utils.debouncedValue
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.search.data.WikiRepository
import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchItem
import me.aartikov.replica.algebra.normal.withKey
import me.aartikov.replica.keyed.keepPreviousData
import ru.mobileup.kmm_form_validation.control.InputControl
import ru.mobileup.kmm_form_validation.options.ImeAction
import ru.mobileup.kmm_form_validation.options.KeyboardOptions

class RealSearchComponent(
    componentContext: ComponentContext,
    wikiRepository: WikiRepository,
    private val errorHandler: ErrorHandler,
    private val externalAppService: ExternalAppService,
) : ComponentContext by componentContext, SearchComponent {

    override val queryInputControl = InputControl(
        coroutineScope = componentCoroutineScope,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )

    private val debouncedQuery = queryInputControl.debouncedValue(
        scope = componentCoroutineScope,
        transform = String::trimAndTrimMiddle
    )

    private val searchReplica =
        wikiRepository.searchReplica.keepPreviousData().withKey(debouncedQuery)

    override val wikiSearchResultState = searchReplica.observe(lifecycle, errorHandler)

    override fun onItemClick(item: WikiSearchItem) {
        safeRun(errorHandler) {
            externalAppService.openBrowser(item.url)
        }
    }

    override fun onRefresh() = searchReplica.refresh()
}

private fun String.trimAndTrimMiddle(): String = trim().replace(Regex("\\s+"), " ")
