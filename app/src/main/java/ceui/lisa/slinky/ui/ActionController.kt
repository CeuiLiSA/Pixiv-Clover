package ceui.lisa.slinky.ui

import android.text.TextUtils
import androidx.lifecycle.LiveData
import ceui.lisa.slinky.R
import ceui.lisa.slinky.db.ViewHistory
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.IllustSeriesDetail
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.models.TrendingTag
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.requireLoggedInUserId
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.styles.ProgressImageButton
import ceui.lisa.slinky.styles.ProgressTextButton
import ceui.lisa.slinky.ui.dialog.alertNotice
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import ceui.lisa.slinky.ui.novel.NovelTextFragmentArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object BookmarkType {
    const val PUBLIC = "public"
    const val PRIVATE = "private"
}

fun NavFragment.followUserImpl(button: ProgressTextButton, user: User, bookmarkType: String) {
    launchSuspend {
        if (user.id == requireLoggedInUserId()) {
            alertNotice(message = getString(R.string.cant_follow_myself))
        } else {
            try {
                button.showProgress()
                Client.appApi.postFollow(user.id, bookmarkType)
                val updated = user.copy(is_followed = true)
                ObjectPool.update(updated)
                visitUser(updated)
                if (bookmarkType == BookmarkType.PRIVATE) {
                    showPush(title = getString(R.string.follow_success_hint_private))
                } else {
                    showPush(title = getString(R.string.follow_success_hint))
                }
            } catch (ex: Exception) {
                handleError(ex)
            } finally {
                button.hideProgress()
            }
        }
    }
}

fun NavFragment.unfollowUserImpl(button: ProgressTextButton, user: User) {
    launchSuspend {
        try {
            button.showProgress()
            if (alertYesOrCancel(message = getString(R.string.unfollow_user_hint))) {
                Client.appApi.postUnFollow(user.id)
                val updated = user.copy(is_followed = false)
                ObjectPool.update(updated)
                visitUser(updated)
                showPush(title = getString(R.string.unfollow_success_hint))
            }
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            button.hideProgress()
        }
    }
}

fun NavFragment.addBookmarkMangaSeriesImpl(
    button: ProgressImageButton,
    illustSeriesDetail: IllustSeriesDetail,
) {
    launchSuspend {
        try {
            button.showProgress(true)
            Client.appApi.postAddBookmarkMangaSeries(illustSeriesDetail.id)
            ObjectPool.update(illustSeriesDetail.copy(watchlist_added = true))
            showPush(title = getString(R.string.add_bookmark_success_hint))
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            button.showProgress(false)
        }
    }
}

fun NavFragment.removeBookmarkMangaSeriesImpl(
    button: ProgressImageButton,
    illustSeriesDetail: IllustSeriesDetail,
) {
    launchSuspend {
        try {
            button.showProgress(true)
            if (alertYesOrCancel(message = getString(R.string.remove_bookmark_manga_series_hint))) {
                Client.appApi.postRemoveBookmarkMangaSeries(illustSeriesDetail.id)
                ObjectPool.update(illustSeriesDetail.copy(watchlist_added = false))
                showPush(title = getString(R.string.remove_bookmark_success_hint))
            }
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            button.showProgress(false)
        }
    }
}

fun NavFragment.addBookmarkIllustImpl(
    button: ProgressImageButton,
    illust: Illust,
    bookmarkType: String
) {
    launchSuspend {
        try {
            button.showProgress(true)
            Client.appApi.addBookmark(illust.id, bookmarkType)
            val updated = illust.copy(is_bookmarked = true)
            ObjectPool.update(updated)
            visitIllust(updated)
            if (bookmarkType == BookmarkType.PRIVATE) {
                showPush(title = getString(R.string.add_bookmark_success_hint_private))
            } else {
                showPush(title = getString(R.string.add_bookmark_success_hint))
            }
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            button.showProgress(false)
        }
    }
}

fun NavFragment.removeBookmarkIllustImpl(button: ProgressImageButton, illust: Illust) {
    launchSuspend {
        try {
            button.showProgress(true)
            if (alertYesOrCancel(message = getString(R.string.remove_bookmark_illust_hint))) {
                Client.appApi.removeBookmark(illust.id)
                val updated = illust.copy(is_bookmarked = false)
                ObjectPool.update(updated)
                visitIllust(updated)
                showPush(title = getString(R.string.remove_bookmark_success_hint))
            }
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            button.showProgress(false)
        }
    }
}

fun NavFragment.addBookmarkNovelImpl(
    button: ProgressImageButton,
    novel: Novel,
    bookmarkType: String
) {
    launchSuspend {
        try {
            button.showProgress(true)
            Client.appApi.addNovelBookmark(novel.id, bookmarkType)
            val updated = novel.copy(is_bookmarked = true)
            ObjectPool.update(updated)
            visitNovel(updated)
            if (bookmarkType == BookmarkType.PRIVATE) {
                showPush(title = getString(R.string.add_bookmark_success_hint_private))
            } else {
                showPush(title = getString(R.string.add_bookmark_success_hint))
            }
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            button.showProgress(false)
        }
    }
}

fun NavFragment.removeBookmarkNovelImpl(button: ProgressImageButton, novel: Novel) {
    launchSuspend {
        try {
            button.showProgress(true)
            if (alertYesOrCancel(message = getString(R.string.remove_bookmark_illust_hint))) {
                Client.appApi.removeNovelBookmark(novel.id)
                val updated = novel.copy(is_bookmarked = false)
                ObjectPool.update(updated)
                visitNovel(updated)
                showPush(title = getString(R.string.remove_bookmark_success_hint))
            }
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            button.showProgress(false)
        }
    }
}

fun NavFragment.onLongClickIllustImpl(illust: Illust) {
    pushFragment(
        R.id.imageViewPagerFragment,
        OriginalImageViewPagerFragmentArgs(illust.id, 0).toBundle(),
    )
}

fun NavFragment.onClickIllustImpl(illust: Illust) {
    pushFragment(R.id.illustFragment, IllustFragmentArgs(illust.id).toBundle())
}

fun NavFragment.onClickUserImpl(user: User) {
    pushFragment(R.id.userFragment, UserFragmentArgs(user.id).toBundle())
}

fun NavFragment.onClickNovelImpl(novel: Novel) {
    pushFragment(R.id.navigation_novel_text_fragment, NovelTextFragmentArgs(novelId = novel.id).toBundle())
}

fun NavFragment.launch(block: suspend () -> Unit) {
    launchSuspend {
        block.invoke()
    }
}

fun NavFragment.searchTagImpl(name: String, shouldSearchUser: Boolean, searchType: String? = null) {
    launch {
        withContext(Dispatchers.IO) {
            RoomDB.db().historyDao().insertViewHistory(
                ViewHistory(
                    name.hashCode().toLong(),
                    System.currentTimeMillis(),
                    name,
                    HistoryType.SEARCH_KEYWORD
                )
            )
        }
    }

    if (TextUtils.equals(searchType, ObjectType.NOVEL)) {
        searchNovelTagImpl(name)
    } else {
        val type =
            if (shouldSearchUser) ViewPagerContentType.TYPE_SEARCH_KEY_WORD else ViewPagerContentType.TYPE_SEARCH_ILLUST
        pushFragment(
            R.id.viewPagerFragment,
            ViewPagerFragmentArgs(type, name).toBundle(),
        )
    }
}

fun NavFragment.searchNovelTagImpl(name: String) {
    pushFragment(
        R.id.viewPagerFragment,
        ViewPagerFragmentArgs(ViewPagerContentType.TYPE_SEARCH_NOVEL, name).toBundle()
    )
}

fun NavFragment.onLongClickTrendingTagImpl(trendingTag: TrendingTag) {
    if (trendingTag.illust != null) {
        onClickIllustImpl(trendingTag.illust)
    }
}


fun NavFragment.showUserList(type: Int, userId: Long) {
    pushFragment(R.id.userListFragment, UserListFragmentArgs(type, userId).toBundle())
}

fun NavFragment.didClickBookmarkIllust(
    liveDataIllust: LiveData<Illust>,
    button: ProgressImageButton
) {
    liveDataIllust.value?.let { illust ->
        if (illust.is_bookmarked == true) {
            removeBookmarkIllustImpl(button, illust)
        } else {
            addBookmarkIllustImpl(button, illust, BookmarkType.PUBLIC)
        }
    }
}

fun NavFragment.didClickBookmarkNovel(
    liveDataNovel: LiveData<Novel>,
    button: ProgressImageButton
) {
    liveDataNovel.value?.let { novel ->
        if (novel.is_bookmarked == true) {
            removeBookmarkNovelImpl(button, novel)
        } else {
            addBookmarkNovelImpl(button, novel, BookmarkType.PUBLIC)
        }
    }
}

fun NavFragment.didClickBookmarkMangaSeries(
    liveDataSeries: LiveData<IllustSeriesDetail>,
    button: ProgressImageButton
) {
    liveDataSeries.value?.let { series ->
        if (series.watchlist_added == true) {
            removeBookmarkMangaSeriesImpl(button, series)
        } else {
            addBookmarkMangaSeriesImpl(button, series)
        }
    }
}

fun NavFragment.didLongClickBookmarkIllust(
    liveDataIllust: LiveData<Illust>,
    button: ProgressImageButton
) {
    liveDataIllust.value?.let { illust ->
        if (illust.is_bookmarked == true) {
            removeBookmarkIllustImpl(button, illust)
        } else {
            addBookmarkIllustImpl(button, illust, BookmarkType.PRIVATE)
        }
    }
}

fun NavFragment.didLongClickBookmarkNovel(
    liveDataNovel: LiveData<Novel>,
    button: ProgressImageButton
) {
    liveDataNovel.value?.let { novel ->
        if (novel.is_bookmarked == true) {
            removeBookmarkNovelImpl(button, novel)
        } else {
            addBookmarkNovelImpl(button, novel, BookmarkType.PRIVATE)
        }
    }
}

fun NavFragment.didClickFollowUser(liveDataUser: LiveData<User>, button: ProgressTextButton) {
    liveDataUser.value?.let { user ->
        if (user.is_followed == true) {
            unfollowUserImpl(button, user)
        } else {
            followUserImpl(button, user, BookmarkType.PUBLIC)
        }
    }
}

fun NavFragment.didLongClickFollowUser(liveDataUser: LiveData<User>, button: ProgressTextButton) {
    liveDataUser.value?.let { user ->
        if (user.is_followed == true) {
            unfollowUserImpl(button, user)
        } else {
            followUserImpl(button, user, BookmarkType.PRIVATE)
        }
    }
}
