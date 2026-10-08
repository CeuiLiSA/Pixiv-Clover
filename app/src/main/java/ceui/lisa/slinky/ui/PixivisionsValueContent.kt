package ceui.lisa.slinky.ui

import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.models.Article
import ceui.lisa.slinky.models.ArticlesResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import kotlinx.coroutines.CoroutineScope

class PixivisionsValueContent(coroutineScope: CoroutineScope) : ValueContent<ArticlesResponse>(coroutineScope, loader = {
    val response = Client.appApi.articles(ArticleType.ALL)
    val snapResult = response.spotlight_articles
    val size = snapResult.size
    val stored = if (size in 1..5 && response.next_url?.isNotEmpty() == true) {
        val appendResponse = Client.appApi.getNextArticle(response.next_url)
        val appendList = appendResponse.spotlight_articles
        if (appendList.isNotEmpty()) {
            val result = mutableListOf<Article>()
            result.addAll(snapResult)
            result.addAll(appendList)
            result
        } else {
            snapResult
        }
    } else {
        snapResult
    }
    ArticlesResponse(spotlight_articles = stored)
}) {
    init {
        setUpCache(
            PrefResponseCache(ArticlesResponse::class.java, prefKeyProducer = { "pixivision" })
        )
    }

    override fun onResponseReady(resp: ArticlesResponse) {
        super.onResponseReady(resp)
        resp.spotlight_articles.forEach {
            ObjectPool.update(it)
        }
    }
}