package ceui.lisa.slinky.ui

import ceui.lisa.slinky.db.ViewHistory
import ceui.lisa.slinky.models.Article
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.models.TrendingTag
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.models.WebIllust

interface UserAction {

    fun onClickUser(user: User)
}

interface PostAction : UserAction

interface NovelAction {

    fun onClickNovel(novel: Novel)
}

interface NovelSeriesAction {

    fun onClickNovelSeries(seriesId: Long)
}

interface WebIllustAction {

    fun onClickWebIllust(webIllust: WebIllust)
}

interface SeeMoreAction {

    fun seeMore(type: Int)
}

interface TagAction {

    fun searchTag(name: String)
}

interface DownloadIllustAction {

    fun download(index: Int)
}

interface TrendingTagAction : TagAction {

    fun onLongClickTrendingTag(trendingTag: TrendingTag)
}

interface ArticleAction {

    fun openArticle(article: Article)
}

interface ShowImageFullScreenAction {

    fun showImageFullScreen(url: String)
}

interface ShowImageViewPager {

    fun showImageViewPager(illustId: Long, index: Int)
}

interface HistoryAction {
    fun showHistory(history: ViewHistory)
}

interface UserFullAction : UserAction {

    fun onClickFollowUserList()

    fun onClickFansList()

    fun onClickPixivFriendsList()
}

interface RefreshAction {

    fun refresh()
}

interface ReselectAction {

    fun onReselected(index: Int)
}