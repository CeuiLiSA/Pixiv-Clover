package ceui.lisa.slinky.ui

import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.models.UserResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.paging.ListShow
import ceui.lisa.slinky.ui.novel.UserCreatedNovelsHolder
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class UserProfileRepository(
    private val userId: Long
) : CustomRepository<UserFragment>() {

    private val userBookmarkedIllustViewModel by lazy { UserBookmarkedIllustValueContent(userId, coroutineScope) }
    private val userCreatedWorksViewModel by lazy { UserCreatedWorksValueContent(userId, ObjectType.MANGA, coroutineScope = coroutineScope) }
    private val userCreatedNovelsViewModel by lazy { UserCreatedNovelsValueContent(userId, coroutineScope = coroutineScope) }

    private val rootRepository = this
    private val userCreatedIllustFetcher = object : PixivListRepository<Illust, UserFragment>(
        loader = {
            delay(1600L)
            Client.appApi.userCreatedIllust(userId, ObjectType.ILLUST)
        },
        dataMapper = { PostItem(it, showUserLayout = false) }
    ) {
        override suspend fun applyRefreshData(
            fragment: UserFragment,
            displayList: List<Illust>
        ) {
            val items = rootRepository.toMutableList()
            items.removeLast()
            items.addAll(displayList.map {
                PostItem(it, showUserLayout = false)
            })
            rootRepository.holderList.value = items
        }

        override suspend fun applyLoadMoreData(fragment: UserFragment, displayList: List<Illust>) {
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

    override fun attachFragment(fragment: UserFragment) {
        super.attachFragment(fragment)
        userCreatedIllustFetcher.coroutineScope = rootRepository.coroutineScope
    }

    override suspend fun suspendRefresh(
        fragment: UserFragment
    ) {
        coroutineScope {
            with(fragment) {
                val task0: Deferred<UserResponse> = async {
                    Client.appApi.user(userId)
                }
                val userResponse = task0.await()
                userResponse.user?.let {
                    ObjectPool.update(it)
                }
                val headerHolders = mutableListOf<SlinkyItem>()
                val holder0 =
                    UserHeaderHolder(ObjectPool.get(userId), userResponse.profile)
                headerHolders.add(holder0)

                if ((userResponse.profile?.total_illust_bookmarks_public ?: 0) > 0) {
                    val seeMoreTitle = getString(
                        R.string.item_count,
                        userResponse.profile?.total_illust_bookmarks_public
                    )
                    headerHolders.add(
                        SectionHeaderHolder(
                            getString(R.string.bookmarked_by_him),
                            SeeMoreType.BOOKMARKED_ILLUST,
                            seeMoreTitle
                        )
                    )
                    headerHolders.add(
                        UserIllustHolder(
                            userBookmarkedIllustViewModel.result,
                            userBookmarkedIllustViewModel.loadState
                        ) {
                            userBookmarkedIllustViewModel.refresh()
                        })
                    userBookmarkedIllustViewModel.refresh()
                }

                if ((userResponse.profile?.total_manga ?: 0) > 0) {
                    headerHolders.add(SpaceHolder(10.pxValue))
                    val seeMoreTitle =
                        getString(R.string.item_count, userResponse.profile?.total_manga)
                    headerHolders.add(
                        SectionHeaderHolder(
                            getString(R.string.manga_item),
                            SeeMoreType.CREATED_MANGA,
                            seeMoreTitle
                        )
                    )
                    headerHolders.add(
                        UserIllustHolder(
                            userCreatedWorksViewModel.result,
                            userCreatedWorksViewModel.loadState
                        ) {
                            userCreatedWorksViewModel.refresh()
                        })
                    userCreatedWorksViewModel.refresh()
                }

                if ((userResponse.profile?.total_novels ?: 0) > 0) {
                    headerHolders.add(SpaceHolder(10.pxValue))
                    val seeMoreTitle =
                        getString(R.string.item_count, userResponse.profile?.total_novels)
                    headerHolders.add(
                        SectionHeaderHolder(
                            getString(R.string.novel_works),
                            SeeMoreType.CREATED_NOVEL,
                            seeMoreTitle
                        )
                    )
                    headerHolders.add(
                        UserCreatedNovelsHolder(
                            userCreatedNovelsViewModel.result,
                            userCreatedNovelsViewModel.loadState
                        ) {
                            userCreatedNovelsViewModel.refresh()
                        })
                    userCreatedNovelsViewModel.refresh()
                }

                run {
                    headerHolders.add(SpaceHolder(10.pxValue))
                    val seeMoreTitle = if ((userResponse.profile?.total_illusts ?: 0) > 0) {
                        getString(R.string.item_count, userResponse.profile?.total_illusts)
                    } else {
                        ""
                    }
                    headerHolders.add(
                        SectionHeaderHolder(
                            getString(R.string.illust_item),
                            SeeMoreType.CREATED_ILLUST,
                            seeMoreTitle
                        )
                    )
                }
                val loadCreatedIllustsBlock: (RefreshHint) -> Unit = { hint ->
                    userCreatedIllustFetcher.dispatchRefresh(
                        fragment,
                        hint
                    )
                }
                headerHolders.add(
                    LoadingHolder(
                        userCreatedIllustFetcher.refreshState,
                        refreshBlock = { loadCreatedIllustsBlock.invoke(RefreshHint.retry()) })
                )
                loadCreatedIllustsBlock.invoke(RefreshHint.initialLoad())
                holderList.value = headerHolders
                refreshState.value = LoadState.LOADED(
                    hasContent = true,
                    hasNext = userCreatedIllustFetcher.nextUrl.value != null
                )
            }
        }
    }

    override suspend fun suspendLoadMore(
        fragment: UserFragment
    ) {
        userCreatedIllustFetcher.suspendLoadMore(fragment)
    }
}