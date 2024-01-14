package me.aartikov.replica.paged

import me.aartikov.replica.common.AbstractLoadable
import me.aartikov.replica.common.CombinedLoadingError

data class Paged<out T : Any>(
    val loadingStatus: PagedLoadingStatus = PagedLoadingStatus.None,
    override val data: T? = null,
    override val error: CombinedLoadingError? = null
) : AbstractLoadable<T> {

    override val loading: Boolean
        get() = loadingStatus == PagedLoadingStatus.LoadingFirstPage
}

fun <T : Any, R : Any> Paged<T>.mapData(
    transform: (T) -> R
): Paged<R> {
    return Paged(
        loadingStatus = loadingStatus, data = data?.let { transform(it) }, error = error
    )
}