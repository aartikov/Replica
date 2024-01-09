package me.aartikov.replica.paged

import me.aartikov.replica.common.CombinedLoadingError

data class Paged<out T : Any, out P : Page<T>>(
    val loadingStatus: PagedLoadingStatus = PagedLoadingStatus.None,
    val data: PagedData<T, P>? = null,
    val error: CombinedLoadingError? = null
)

fun <T : Any, P : Page<T>, TR : Any, PR : Page<TR>> Paged<T, P>.mapData(
    transform: (PagedData<T, P>) -> PagedData<TR, PR>
): Paged<TR, PR> {
    return Paged(
        loadingStatus = loadingStatus, data = data?.let { transform(it) }, error = error
    )
}