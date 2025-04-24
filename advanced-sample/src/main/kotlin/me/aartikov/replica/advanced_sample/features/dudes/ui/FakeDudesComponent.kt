package me.aartikov.replica.advanced_sample.features.dudes.ui

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesContent
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedLoadingStatus

class FakeDudesComponent : DudesComponent {
    override val dudesState = MutableStateFlow(
        Paged(
            loadingStatus = PagedLoadingStatus.LoadingFirstPage,
            data = DudesContent(Dude.FAKE_LIST, hasNextPage = false)
        )
    )

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit

    override fun onLoadNext() = Unit
}
