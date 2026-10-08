package ceui.lisa.slinky.models


data class WebNovel(
    val aiType: Int? = null,
    val caption: String? = null,
    val coverUrl: String? = null,
    val glossaryItems: List<Any?>? = null,
    val id: String? = null,
    val text: String? = null,
    val isOriginal: Boolean? = null,
    val marker: NovelMarkerBean? = null,
    val illusts: Map<String, WebIllustHolder>? = null,
    val images: Map<String, NovelImages>? = null,
    val replaceableItemIds: List<Any?>? = null,
    val seriesId: String? = null,
    val seriesIsWatched: Boolean? = null,
    val seriesNavigation: SeriesNavigation? = null,
    val seriesTitle: String? = null,
    val tags: List<String?>? = null,
    val title: String? = null,
    val userId: String? = null
)

data class NovelMarkerBean(
    val page: Int? = null,
)

data class SeriesNavigation(
    val nextNovel: Novel? = null,
    val prevNovel: Novel? = null
)

data class NovelImages(
    val novelImageId: Long? = null,
    val sl: Int? = null,
    val urls: Map<String, String>? = null,
) {
    companion object {

    }

    object Size {
        const val Size240mw = "240mw"
        const val Size480mw = "480mw"
        const val Size1200x1200 = "1200x1200"
        const val Size128x128 = "128x128"
        const val SizeOriginal = "original"
    }
}