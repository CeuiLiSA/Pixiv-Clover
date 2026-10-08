package ceui.lisa.slinky.ui

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.glide.GlideProgress
import ceui.lisa.slinky.glide.LiveDataProgressListener
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.paging.ListShow
import ceui.lisa.slinky.ui.dialog.alertNotice
import ceui.lisa.slinky.ui.settings.requireLocalSetting


class IllustFragmentRepository(
    private val illustId: Long,
    private val illustAuthorId: Long
) : CustomRepository<IllustFragment>() {


    private val userCreatedWorksValueContent by lazy {
        UserCreatedWorksValueContent(
            illustAuthorId,
            ObjectType.ILLUST,
            illustId,
            coroutineScope
        ).apply {
            setUpCache(
                PrefResponseCache(IllustResponse::class.java, prefKeyProducer = { "user-[${illustAuthorId}]-created-illusts" })
            )
        }
    }
    private val illustSeriesValueContent by lazy {
        ValueContent(coroutineScope, loader = { Client.appApi.getIllustSeriesSnapshot(illustId) })
    }

    private val rootRepository = this

    private val relatedIllustRepository = object : PixivListRepository<Illust, IllustFragment>(
        loader = {
            kotlinx.coroutines.delay(1600L)
            Client.appApi.relatedIllust(illustId)
        },
        dataMapper = { PostItem(it) }
    ) {
        override suspend fun applyRefreshData(
            fragment: IllustFragment,
            displayList: List<Illust>
        ) {
            val items = rootRepository.toMutableList()
            items.removeLast()
            items.addAll(displayList.map {
                PostItem(it)
            })
            rootRepository.holderList.value = items
        }

        override suspend fun applyLoadMoreData(
            fragment: IllustFragment,
            displayList: List<Illust>
        ) {
            val pages = rootRepository.toMutableList()
            pages.addAll(displayList.map(dataMapper))
            rootRepository.holderList.value = pages
        }

        override suspend fun onResponseReady(resp: ListShow<Illust>) {
            with(rootRepository) {
                val hasNext = resp.nextPageUrl != null
                refreshState.value = LoadState.LOADED(hasContent = true, hasNext = hasNext)
            }
        }
    }

    override fun attachFragment(fragment: IllustFragment) {
        super.attachFragment(fragment)
        relatedIllustRepository.coroutineScope = rootRepository.coroutineScope
    }

    private val progressStore = hashMapOf<String, MutableLiveData<Int>>()
    private fun getProgress(url: String): MutableLiveData<Int> {
        val exist = progressStore[url]
        return if (exist != null) {
            exist
        } else {
            val newly = MutableLiveData(0)
            progressStore[url] = newly
            newly
        }
    }

    private val stateStore = hashMapOf<String, MutableLiveData<Int>>()
    private fun getState(url: String): MutableLiveData<Int> {
        val exist = stateStore[url]
        return if (exist != null) {
            exist
        } else {
            val newly = MutableLiveData(GlideState.IDLE)
            stateStore[url] = newly
            newly
        }
    }

    override suspend fun suspendRefresh(
        fragment: IllustFragment
    ) {
        with(fragment) {
            val illust = ObjectPool.get<Illust>(illustId).value ?: return

            if (illust.user?.id == 0L) {
                alertNotice(message = getString(R.string.wrong_illust_hint))
                performBack()
                return
            }

            val mutableHolders = mutableListOf<SlinkyItem>()

            if (illust.meta_pages == null && illust.meta_single_page == null) {
                val thisUrl = illust.image_urls?.original ?: ""
                val listener = LiveDataProgressListener(getProgress(thisUrl), getState(thisUrl))
                GlideProgress.add(thisUrl, fragment.lifecycle, listener)
                mutableHolders.add(
                    SingleIllustHolder(
                        illust,
                        thisUrl,
                        illust.width,
                        illust.height,
                        0,
                        true,
                        loader = ImageLoaderTask(fragment, thisUrl),
                        listener
                    )
                )
            } else if (illust.page_count == 1) {
                val thisUrl = illust.meta_single_page?.original_image_url ?: ""
                val listener = LiveDataProgressListener(getProgress(thisUrl), getState(thisUrl))
                GlideProgress.add(thisUrl, fragment.lifecycle, listener)
                mutableHolders.add(
                    SingleIllustHolder(
                        illust,
                        thisUrl,
                        illust.width,
                        illust.height,
                        0,
                        true,
                        loader = ImageLoaderTask(fragment, thisUrl),
                        listener
                    )
                )
            } else {
                illust.meta_pages?.forEachIndexed { index, metaPage ->
                    val thisUrl = metaPage.image_urls?.original ?: ""
                    val listener = LiveDataProgressListener(getProgress(thisUrl), getState(thisUrl))
                    GlideProgress.add(thisUrl, fragment.lifecycle, listener)
                    val preferWidth =
                        if (index == 0) illust.width else screenWidth
                    val preferHeight = if (index == 0) illust.height else 230.pxValue
                    mutableHolders.add(
                        SingleIllustHolder(
                            illust,
                            thisUrl,
                            preferWidth,
                            preferHeight,
                            index = index,
                            shouldResize = false,
                            loader = ImageLoaderTask(fragment, thisUrl),
                            progressListener = listener
                        )
                    )
                }
            }

            mutableHolders.add(SectionHeaderHolder(getString(R.string.title)))
            mutableHolders.add(IllustTitleHolder(ObjectPool.get(illustId)))

            if (illust.series != null && illust.series.id.exist()) {
                mutableHolders.add(
                    SectionHeaderHolder(
                        getString(R.string.series),
                        SeeMoreType.IllustSeries,
                        seeMoreString = getString(R.string.see_all_series)
                    )
                )
                mutableHolders.add(IllustSeriesHolder(illustSeriesValueContent.result))
            }

            if (illust.user != null) {
                mutableHolders.add(SectionHeaderHolder(getString(R.string.author)))
                mutableHolders.add(
                    UserSnapHolder(
                        ObjectPool.get(illust.user.id),
                        illust.displayCreateDate()
                    )
                )
                mutableHolders.add(SpaceHolder(5.pxValue))
                mutableHolders.add(
                    SectionHeaderHolder(
                        getString(R.string.other_works),
                        SeeMoreType.CREATED_ILLUST,
                        getString(R.string.more)
                    )
                )
                mutableHolders.add(
                    UserIllustHolder(
                        userCreatedWorksValueContent.result,
                        userCreatedWorksValueContent.loadState
                    ) {
                        userCreatedWorksValueContent.refresh()
                    })
                userCreatedWorksValueContent.refresh()
                mutableHolders.add(SpaceHolder(5.pxValue))
            }

            if (illust.caption?.isNotEmpty() == true) {
                mutableHolders.add(SectionHeaderHolder(getString(R.string.item_desc)))
                mutableHolders.add(DescHolder(illust.caption))
            }

            if (illust.tags?.isNotEmpty() == true) {
                mutableHolders.add(SectionHeaderHolder(getString(R.string.tags)))
                mutableHolders.add(TagsHolder(illust.tags))
            }

            mutableHolders.add(
                SectionHeaderHolder(
                    getString(
                        if (TextUtils.equals(illust.type, ObjectType.ILLUST))
                            R.string.illust_item
                        else
                            R.string.manga_item
                    )
                )
            )
            mutableHolders.add(IllustInfo(illust, requireLocalSetting()))


            mutableHolders.add(
                SectionHeaderHolder(
                    getString(R.string.related_items),
                    type = SeeMoreType.RELATED_ILLUST,
                    seeMoreString = getString(R.string.more)
                )
            )
            val loadRelatedIllustsBlock: (RefreshHint) -> Unit = { hint ->
                if (view != null) {
                    relatedIllustRepository.dispatchRefresh(
                        fragment,
                        hint
                    )
                }
            }
            mutableHolders.add(
                LoadingHolder(
                    relatedIllustRepository.refreshState,
                    refreshBlock = { loadRelatedIllustsBlock.invoke(RefreshHint.errorRetry()) }
                )
            )
            loadRelatedIllustsBlock.invoke(RefreshHint.initialLoad())

            holderList.value = mutableHolders
            refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
        }
    }

    override suspend fun suspendLoadMore(fragment: IllustFragment) {
        relatedIllustRepository.suspendLoadMore(fragment)
    }
}