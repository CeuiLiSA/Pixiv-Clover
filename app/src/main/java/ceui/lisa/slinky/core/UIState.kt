package ceui.lisa.slinky.core

import java.io.Serializable

sealed class LoadState : Serializable {
    data class LOADING(val title: String = "", val refreshHint: RefreshHint? = null) : LoadState()
    data class LOADED(val hasContent: Boolean = true, val hasNext: Boolean = true) : LoadState()
    data class ERROR(val exception: Exception, val isInitialLoad: Boolean = false) : LoadState()
}

data class RefreshHint(
    val cause: Cause
) {
    enum class Cause {
        PULL_TO_REFRESH,
        INITIAL_LOAD,
        LOAD_MORE,
        ERROR_RETRY,
    }

    companion object {
        fun pullToRefresh(): RefreshHint {
            return RefreshHint(Cause.PULL_TO_REFRESH)
        }

        fun initialLoad(): RefreshHint {
            return RefreshHint(Cause.INITIAL_LOAD)
        }

        fun loadMore(): RefreshHint {
            return RefreshHint(Cause.LOAD_MORE)
        }

        fun errorRetry(): RefreshHint {
            return RefreshHint(Cause.ERROR_RETRY)
        }
    }
}