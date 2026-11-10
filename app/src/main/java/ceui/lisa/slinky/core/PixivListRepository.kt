package ceui.lisa.slinky.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.models.Comment
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.ModelObject
import ceui.lisa.slinky.models.TrendingTag
import ceui.lisa.slinky.models.UserPreview
import ceui.lisa.slinky.mute.MuteRule
import ceui.lisa.slinky.mute.MuteStore
import ceui.lisa.slinky.mute.MuteSubjectItem
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.network.paging.ListShow
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.SlinkyItem
import com.airbnb.lottie.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

open class PixivListRepository<ModelT, FragmentT : NavFragment>(
    val loader: suspend () -> ListShow<ModelT>,
    val dataMapper: ((ModelT) -> SlinkyItem),
    private val muteRule: MuteRule = MuteRule.Default,
) : Repository<FragmentT>() {

    private val _nextUrl = MutableLiveData<String?>()
    val nextUrl: LiveData<String?> = _nextUrl

    private var _responseCache: ResponseCache<ListShow<ModelT>>? = null

    fun <ResponseT : ListShow<ModelT>> setUpCache(impl: ResponseCache<ResponseT>) {
        try {
            _responseCache = impl as ResponseCache<ListShow<ModelT>>
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    override suspend fun suspendRefresh(
        fragment: FragmentT
    ) {
        val resp = requestImpl()
        val displayList = visibleItems(resp.displayList)
        val hasNext = resp.nextPageUrl != null
        if (displayList.isNotEmpty()) {
            applyRefreshData(fragment, displayList)
            refreshState.value = LoadState.LOADED(hasContent = true, hasNext = hasNext)
        } else {
            refreshState.value = LoadState.LOADED(hasContent = false, hasNext = false)
        }
    }

    override suspend fun suspendLoadMore(
        fragment: FragmentT
    ) {
        val nextUrl = _nextUrl.value
        if (nextUrl?.isNotEmpty() == true) {
            val resp = requestImpl(nextUrl)
            val displayList = visibleItems(resp.displayList)
            val hasNext = resp.nextPageUrl != null
            if (displayList.isNotEmpty()) {
                applyLoadMoreData(fragment, displayList)
            }
            // 整页都被屏蔽时还有下一页，不能当成到底了
            val canLoadMore = resp.displayList.isNotEmpty() && hasNext
            loadMoreState.value = LoadState.LOADED(hasContent = true, hasNext = canLoadMore)
        } else {
            loadMoreState.value = LoadState.LOADED(hasContent = true, hasNext = false)
        }
    }

    private var isObservingMute = false

    override fun attachFragment(fragment: FragmentT) {
        super.attachFragment(fragment)
        if (muteRule != MuteRule.Disabled && !isObservingMute) {
            isObservingMute = true
            coroutineScope.launch {
                MuteStore.changes.collect { hideNewlyMutedItems() }
            }
        }
    }

    private suspend fun visibleItems(displayList: List<ModelT>): List<ModelT> {
        if (muteRule == MuteRule.Disabled) {
            return displayList
        }
        MuteStore.awaitLoaded()
        return displayList.filterNot { MuteStore.shouldHide(it, muteRule) }
    }

    /** 名单变了只拿掉新命中的条目；取消屏蔽的条目要等下次刷新才回来。 */
    private fun hideNewlyMutedItems() {
        val current = holderList.value ?: return
        val visible = current.filterNot { item ->
            item is MuteSubjectItem && MuteStore.shouldHide(item.muteSubject, muteRule)
        }
        if (visible.size != current.size) {
            holderList.value = visible
        }
    }

    open suspend fun applyRefreshData(
        fragment: FragmentT,
        displayList: List<ModelT>,
    ) {
        holderList.value = displayList.map(dataMapper)
    }

    open suspend fun applyLoadMoreData(
        fragment: FragmentT,
        displayList: List<ModelT>,
    ) {
        val pages = toMutableList()
        pages.addAll(displayList.map(dataMapper))
        holderList.value = pages
    }

    private suspend fun loaderImpl(nextUrl: String? = null): ListShow<ModelT> {
        return if (nextUrl == null) {
            val responseCache = _responseCache
            if (responseCache != null) {
                val cached = responseCache.get()
                if (cached != null) {
                    cached
                } else {
                    val ret = loader.invoke()
                    responseCache.put(ret)
                    ret
                }
            } else {
                loader.invoke()
            }
        } else {
            val responseBody = Client.appApi.generalGet(nextUrl)
            val str = responseBody.string()
            val obj = Util.gson.fromJson(str, classSpec)
            obj as ListShow<ModelT>
        }
    }

    open suspend fun onResponseReady(resp: ListShow<ModelT>) {

    }

    private var classSpec: Class<*>? = null

    private suspend fun requestImpl(nextUrl: String? = null): ListShow<ModelT> {
        val resp = loaderImpl(nextUrl)
        onResponseReady(resp)
        classSpec = resp::class.java
        Timber.d("PixivListRepository requestImpl ${classSpec?.simpleName}")
        _nextUrl.value = resp.nextPageUrl
        val displayList = resp.displayList
        if (displayList.isNotEmpty()) {
            displayList.forEach {
                if (it is ModelObject) {
                    if (it is Illust) {
                        ObjectPool.updateIllust(it)
                    } else {
                        ObjectPool.update(it)
                    }
                } else if (it is Comment) {
                    ObjectPool.update(it.user)
                } else if (it is UserPreview) {
                    ObjectPool.putUserPreview(it)
                } else if (it is TrendingTag) {
                    it.illust?.let { illust -> ObjectPool.updateIllust(illust) }
                }
            }
        }
        return resp
    }
}