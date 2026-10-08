package ceui.lisa.slinky.ui

import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.models.NovelResponse
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class UserCreatedNovelsValueContent(
    private val userId: Long,
    coroutineScope: CoroutineScope
) : ValueContent<NovelResponse>(coroutineScope, {
    Client.appApi.userCreatedNovel(userId)
}) {

    override fun hasContent(t: NovelResponse): Boolean {
        return t.novels.isNotEmpty()
    }

    override fun onResponseReady(resp: NovelResponse) {
        super.onResponseReady(resp)
        resp.novels.forEach { novel ->
            ObjectPool.update(novel)
        }
    }
}