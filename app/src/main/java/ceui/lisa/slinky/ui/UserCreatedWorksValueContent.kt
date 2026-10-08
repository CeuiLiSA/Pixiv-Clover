package ceui.lisa.slinky.ui

import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class UserCreatedWorksValueContent(
    private val userId: Long,
    private val type: String,
    private val fromIllustId: Long = 0L,
    coroutineScope: CoroutineScope
) : ValueContent<IllustResponse>(coroutineScope, {
    val ret = if (userId == 0L) {
        IllustResponse()
    } else {
        delay(800L)

        val resp = Client.appApi.userCreatedIllust(userId, type)
        if (type == ObjectType.ILLUST && resp.illusts.size > 6) {
            val shuffled = resp.illusts.filter { it.id != fromIllustId }.shuffled()
            resp.copy(illusts = shuffled)
        } else {
            resp
        }
    }
    ret
}) {

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