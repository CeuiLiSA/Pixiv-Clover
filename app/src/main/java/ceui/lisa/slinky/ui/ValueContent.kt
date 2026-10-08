package ceui.lisa.slinky.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.ResponseCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


open class ValueContent<ValueT>(
    private val coroutineScope: CoroutineScope,
    private val loader: suspend () -> ValueT,
) {

    private val _result = MutableLiveData<ValueT>()
    val result: LiveData<ValueT> = _result

    private val _loadState = MutableLiveData<LoadState>()
    val loadState: LiveData<LoadState> = _loadState

    private var _isLoading = false

    private var _responseCache: ResponseCache<ValueT>? = null

    fun setUpCache(impl: ResponseCache<ValueT>) {
        _responseCache = impl
    }

    fun refresh() {
        if (_isLoading) {
            return
        }

        _isLoading = true
        coroutineScope.launch {
            try {
                _loadState.value = LoadState.LOADING()
                val resp = withContext(Dispatchers.IO) {
                    val responseCache = _responseCache
                    val result = if (responseCache != null) {
                        val cached = responseCache.get()
                        if (cached != null) {
                            cached
                        } else {
                            val ret = loader()
                            responseCache.put(ret)
                            ret
                        }
                    } else {
                        loader()
                    }
                    _result.postValue(result!!)
                    result
                }
                onResponseReady(resp)
                _loadState.value = LoadState.LOADED(hasContent = hasContent(resp), hasNext = false)
            } catch (ex: Exception) {
                _loadState.value = LoadState.ERROR(ex)
                Timber.e(ex)
            } finally {
                _isLoading = false
            }
        }
    }

    open fun hasContent(t: ValueT): Boolean {
        return true
    }

    open fun onResponseReady(resp: ValueT) {

    }
}
