package me.aartikov.replica.advanced_sample.features.search.ui

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.error_handling.safeRun
import me.aartikov.replica.advanced_sample.core.external_app_service.ExternalAppService
import me.aartikov.replica.advanced_sample.core.utils.componentCoroutineScope
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

    companion object {
        private const val DEBOUNCE_TIMEOUT_MS = 500L
        private const val SUBSCRIPTION_TIMEOUT_MS = 5000L
    }

    override val queryInputControl = InputControl(
        coroutineScope = componentCoroutineScope,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )

    @OptIn(FlowPreview::class)
    override val debouncedQuery: StateFlow<String> = queryInputControl.value
        .debounce(DEBOUNCE_TIMEOUT_MS)
        .map(String::trimAndTrimMiddle)
        .stateIn(
            scope = componentCoroutineScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = queryInputControl.value.value
        )

    private val searchReplica =
        wikiRepository.searchReplica.keepPreviousData().withKey(debouncedQuery)

    override val wikiSearchItems = searchReplica.observe(lifecycle, errorHandler)

    override fun onItemClick(item: WikiSearchItem) {
        safeRun(errorHandler) {
            externalAppService.openBrowser(item.url)
        }
    }

    override fun onRetryClick() = searchReplica.refresh()
}

private fun String.trimAndTrimMiddle(): String = trim().replace(Regex("\\s+"), " ")
