package ceui.lisa.slinky.ui

import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.models.UserPreviewResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import kotlinx.coroutines.CoroutineScope

class RecommendUsersValueContent(coroutineScope: CoroutineScope) :
    ValueContent<UserPreviewResponse>(coroutineScope, { Client.appApi.recommendUser() }) {

    init {
        setUpCache(
            PrefResponseCache(UserPreviewResponse::class.java, prefKeyProducer = { "recmd-user" })
        )
    }

    override fun onResponseReady(resp: UserPreviewResponse) {
        super.onResponseReady(resp)
        resp.user_previews.forEach {
            ObjectPool.putUserPreview(it)
        }
    }
}