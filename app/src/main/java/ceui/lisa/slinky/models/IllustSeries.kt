package ceui.lisa.slinky.models

import ceui.lisa.slinky.network.paging.ListShow

data class IllustSeries(
    val illust_series_context: IllustSeriesContext? = null,
    val illust_series_detail: IllustSeriesDetail? = null,
    val illust_series_first_illust: Illust? = null,
    val illust_series_latest_illust: Illust? = null,
    val illusts: List<Illust>? = null,
    val next_url: String? = null
) : ListShow<Illust> {
    override val displayList: List<Illust>
        get() = illusts ?: listOf()
    override val nextPageUrl: String?
        get() = next_url

}

data class NovelSeries(
    val novel_series_context: NovelSeriesContext? = null,
    val novel_series_detail: IllustSeriesDetail? = null,
    val novel_series_first_illust: Novel? = null,
    val novel_series_latest_illust: Novel? = null,
    val novels: List<Novel>? = null,
    val next_url: String? = null
) : ListShow<Novel> {
    override val displayList: List<Novel>
        get() = novels ?: listOf()
    override val nextPageUrl: String?
        get() = next_url

}

data class IllustSeriesContext(
    val content_order: Int? = 0,
    val next: Illust? = null,
    val prev: Illust? = null,
)

data class NovelSeriesContext(
    val content_order: Int? = 0,
    val next: Novel? = null,
    val prev: Novel? = null,
)

// published_content_count	Integer	56
// latest_content_id	Integer	117056465
data class IllustSeriesDetail(
    val caption: String? = null,
    val cover_image_urls: ImageUrls? = null,
    val create_date: String? = null,
    val url: String? = null,
    val latest_content_id: Long? = null,
    val height: Int = 1,
    val published_content_count: Int = 0,
    val id: Long = 0L,
    val series_work_count: Int? = null,
    val content_count: Int? = null,
    val title: String? = null,
    val user: User? = null,
    val watchlist_added: Boolean? = null,
    val width: Int = 1
): ModelObject {
    override val objectUniqueId: Long
        get() = id

}