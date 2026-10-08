package ceui.lisa.slinky.models

import java.io.Serializable


data class SquareResponse(
    val error: Boolean? = null,
    val message: String? = null,
    val body: Square? = null
) : Serializable

data class Square(
    val tagTranslation: Map<String, TranslatedTags>? = null,
    val page: Page? = null,
    val thumbnails: Thumbnails? = null,
) : Serializable

data class TranslatedTags(
    val en: String? = null,
    val ko: String? = null,
    val zh: String? = null,
    val zh_tw: String? = null,
    val romaji: String? = null,
) : Serializable

data class Page(
    val recommendByTag: List<Tag>? = null,
    val trendingTags: List<Tag>? = null,
) : Serializable

data class Thumbnails(
    val illust: List<WebIllust>? = null
) : Serializable

