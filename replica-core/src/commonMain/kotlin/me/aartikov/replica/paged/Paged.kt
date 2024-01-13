package me.aartikov.replica.paged

import me.aartikov.replica.common.AbstractLoadable
import me.aartikov.replica.common.CombinedLoadingError

data class Paged<out T : Any, out P : Page<T>>(
    val loadingStatus: PagedLoadingStatus = PagedLoadingStatus.None,
    override val data: PagedData<T, P>? = null,
    override val error: CombinedLoadingError? = null
) : AbstractLoadable<PagedData<T, P>> {

    override val loading: Boolean
        get() = loadingStatus == PagedLoadingStatus.LoadingFirstPage
}

fun <T : Any, P : Page<T>, TR : Any, PR : Page<TR>> Paged<T, P>.mapData(
    transform: (PagedData<T, P>) -> PagedData<TR, PR>
): Paged<TR, PR> {
    return Paged(
        loadingStatus = loadingStatus, data = data?.let { transform(it) }, error = error
    )
}