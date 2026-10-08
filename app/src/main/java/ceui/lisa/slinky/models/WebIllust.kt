package ceui.lisa.slinky.models

import java.io.Serializable

data class WebIllust(
    val alt: String? = null,
    val bookmarkData: Any? = null,
    val createDate: String? = null,
    val description: String? = null,
    val height: Int,
    val id: Long = 0L,
    val illustType: Int? = null,
    val isBookmarkable: Boolean? = null,
    val images: Urls? = null,
    val isMasked: Boolean? = null,
    val isUnlisted: Boolean? = null,
    val pageCount: Int = 0,
    val aiType: Int = 0,
    val profileImageUrl: String? = null,
    val restrict: Int? = null,
    val sl: Int? = null,
    val title: String? = null,
    val updateDate: String? = null,
    val url: String? = null,
    val urls: Map<String, String?>? = null,
    val userId: Long = 0L,
    val tags: List<String>? = null,
    val userName: String? = null,
    val width: Int,
    val xRestrict: Int? = null,
) : Serializable {

    fun toIllust(): Illust {
        return Illust(
            id = id,
            caption = alt,
            create_date = createDate,
            height = height,
            illust_ai_type = aiType,
            image_urls = ImageUrls(
                original = urls?.get("1200x1200"),
                large = urls?.get("540x540"),
                medium = urls?.get("360x360"),
                square_medium = urls?.get("250x250"),
            ),
            is_bookmarked = isBookmarkable != true,
            is_muted = isUnlisted,
            meta_pages = null,
            meta_single_page = null,
            page_count = pageCount,
            restrict = restrict,
            sanity_level = sl,
            series = null,
            tags = tags?.map { Tag(name = it) },
            title = title,
            tools = null,
            total_bookmarks = null,
            total_view = null,
            type = null,
            user = User(
                account = "@${userId}",
                id = userId
                ),
            visible = isMasked != true,
            width = width,
            x_restrict = xRestrict
        )
    }
}

data class Urls(
    val small: String? = null,
    val medium: String? = null,
    val original: String? = null,
) : Serializable

data class WebIllustHolder(
    val illust: WebIllust? = null,
    val id: Long? = null,
    val user: WebUser? = null
) : Serializable

data class MiniTag(
    val tag: String? = null,
    val userId: Long? = null,
) : Serializable
