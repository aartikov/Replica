package me.aartikov.replica.advanced_sample.features.dudes.ui

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesContent
import me.aartikov.replica.algebra.paged.withKey
import me.aartikov.replica.keyed_paged.KeyedPagedReplica
import me.aartikov.replica.keyed_paged.keepPreviousData

class RealDudesComponent(
    componentContext: ComponentContext,
    dudesByTypeReplica: KeyedPagedReplica<String, DudesContent>,
    errorHandler: ErrorHandler
) : ComponentContext by componentContext, DudesComponent {

    private val typeFlow = MutableStateFlow("bottts-neutral")

    private val dudesReplica = dudesByTypeReplica.keepPreviousData().withKey(typeFlow)

    override val dudesState = dudesReplica.observe(lifecycle, errorHandler)

    override fun onSwitch() {
        typeFlow.value = when (typeFlow.value) {
            "bottts-neutral" -> "adventurer"
            "adventurer" -> "croodles-neutral"
            else -> "bottts-neutral"
        }
    }

    override fun onRefresh() {
        dudesReplica.refresh()
    }

    override fun onRetryClick() {
        dudesReplica.refresh()
    }

    override fun onLoadNext() {
        dudesReplica.loadNext()
    }
}