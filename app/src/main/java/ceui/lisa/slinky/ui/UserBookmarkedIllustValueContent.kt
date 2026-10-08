package ceui.lisa.slinky.ui

import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import kotlinx.coroutines.CoroutineScope

class UserBookmarkedIllustValueContent(
    private val userId: Long,
    coroutineScope: CoroutineScope
) : ValueContent<IllustResponse>(
    coroutineScope, loader = { Client.appApi.userBookmarkedIllust(userId, BookmarkType.PUBLIC) }
) {
    override fun hasContent(t: IllustResponse): Boolean {
        return t.illusts.isNotEmpty()
    }

    override fun onResponseReady(resp: IllustResponse) {
        super.onResponseReady(resp)
        resp.illusts.forEach { illust ->
            ObjectPool.updateIllust(illust)
        }
    }
}