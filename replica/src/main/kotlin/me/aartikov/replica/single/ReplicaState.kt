package me.aartikov.replica.single

data class ReplicaState<out T : Any>(
    val data: ReplicaData<T>?,
    val loading: Boolean,
    val error: Exception?,
    val observerUuids: Set<String>,
    val activeObserverUuids: Set<String>,
    val dataRequested: Boolean,
    val storageChecked: Boolean
) {

    val hasFreshData get() = data != null && data.fresh

    val observerCount get() = observerUuids.size

    val activeObserverCount get() = activeObserverUuids.size

    companion object {
        fun <T : Any> createEmpty(hasStorage: Boolean): ReplicaState<T> = ReplicaState(
            data = null,
            loading = false,
            error = null,
            observerUuids = emptySet(),
            activeObserverUuids = emptySet(),
            dataRequested = false,
            storageChecked = !hasStorage
        )
    }
}

internal fun <T : Any> ReplicaState<T>.toLoadable() = Loadable(
    data = data?.value,
    loading = loading,
    error = error
)