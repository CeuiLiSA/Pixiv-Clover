package ceui.lisa.slinky.core

import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.ui.SlinkyItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class Repository<FragmentT> {

    lateinit var coroutineScope: CoroutineScope

    val refreshState = MutableLiveData<LoadState>()
    val loadMoreState = MutableLiveData<LoadState>()
    val holderList = MutableLiveData<List<SlinkyItem>>()

    fun dispatchRefresh(
        frag: FragmentT,
        hint: RefreshHint
    ) {
        coroutineScope.launch {
            try {
                refreshState.value = LoadState.LOADING(refreshHint = hint)
                suspendRefresh(frag)
            } catch (ex: Exception) {
                Timber.e(ex)
                refreshState.value = LoadState.ERROR(ex)
            }
        }
    }

    fun dispatchLoadMore(
        frag: FragmentT
    ) {
        coroutineScope.launch {
            try {
                loadMoreState.value = LoadState.LOADING(refreshHint = RefreshHint.loadMore())
                suspendLoadMore(frag)
            } catch (ex: Exception) {
                Timber.e(ex)
                loadMoreState.value = LoadState.ERROR(ex)
            }
        }
    }

    abstract suspend fun suspendRefresh(
        fragment: FragmentT
    )

    abstract suspend fun suspendLoadMore(
        fragment: FragmentT
    )

    fun toMutableList(): MutableList<SlinkyItem> {
        return (holderList.value ?: listOf()).toMutableList()
    }

    open fun attachFragment(fragment: FragmentT) {

    }
}