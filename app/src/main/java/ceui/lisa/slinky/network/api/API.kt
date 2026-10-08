package ceui.lisa.slinky.network.api

import ceui.lisa.slinky.models.AISettings
import ceui.lisa.slinky.models.AddCommentResponse
import ceui.lisa.slinky.models.ArticlesResponse
import ceui.lisa.slinky.models.CommentResponse
import ceui.lisa.slinky.models.GifInfoResponse
import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.models.IllustSeries
import ceui.lisa.slinky.models.NotificationResponse
import ceui.lisa.slinky.models.NovelResponse
import ceui.lisa.slinky.models.NovelSeries
import ceui.lisa.slinky.models.SingleIllustResponse
import ceui.lisa.slinky.models.TrendingTagsResponse
import ceui.lisa.slinky.models.UserPreviewResponse
import ceui.lisa.slinky.models.UserResponse
import ceui.lisa.slinky.models.WatchlistResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface API {

    @GET("/v1/notification/list")
    suspend fun getNotifications(): NotificationResponse

    @GET("/v1/illust/ranking?filter=for_ios")
    suspend fun rankListIllust(
        @Query("mode") mode: String
    ): IllustResponse

    @GET("/v1/novel/ranking")
    suspend fun rankListNovel(
        @Query("mode") mode: String
    ): NovelResponse


    @GET("/v1/illust/new?filter=for_ios")
    suspend fun latestContent(
        @Query("content_type") content_type: String
    ): IllustResponse

    @GET("/v1/novel/new?filter=for_ios")
    suspend fun latestNovel(): NovelResponse

    @GET("/v2/illust/follow")
    suspend fun followUserIllust(
        @Query("restrict") restrict: String
    ): IllustResponse


    @GET("/v1/novel/follow")
    suspend fun followUserNovel(
        @Query("restrict") restrict: String
    ): NovelResponse

    @GET("/v1/manga/recommended?include_privacy_policy=true&filter=for_ios")
    suspend fun getRecommendManga(@Query("include_ranking_illusts") include_ranking_illusts: Boolean): IllustResponse

    @GET("/v1/illust/recommended?include_privacy_policy=true&filter=for_ios")
    suspend fun getRecommendIllusts(@Query("include_ranking_illusts") include_ranking_illusts: Boolean): IllustResponse

    @GET("/v1/novel/recommended?include_privacy_policy=true&filter=for_ios")
    suspend fun getRecommendNovels(@Query("include_ranking_novels") include_ranking_novels: Boolean): NovelResponse

    @GET("/webview/v2/novel")
    fun getNovelText(@Query("id") id: Long): Call<ResponseBody>

    @GET("/v1/user/illusts?filter=for_ios")
    suspend fun userCreatedIllust(
        @Query("user_id") user_id: Long,
        @Query("type") type: String
    ): IllustResponse

    @GET("/v1/user/novels")
    suspend fun userCreatedNovel(
        @Query("user_id") user_id: Long
    ): NovelResponse


    @GET("/v1/user/bookmarks/illust?filter=for_ios")
    suspend fun userBookmarkedIllust(
        @Query("user_id") user_id: Long,
        @Query("restrict") restrict: String
    ): IllustResponse

    @GET("/v1/user/bookmarks/novel")
    suspend fun userBookmarkedNovel(
        @Query("user_id") user_id: Long,
        @Query("restrict") restrict: String
    ): NovelResponse

    @GET("/v2/illust/related?filter=for_ios")
    suspend fun relatedIllust(
        @Query("illust_id") illust_id: Long
    ): IllustResponse

    @GET("/v1/user/detail?filter=for_ios")
    suspend fun user(@Query("user_id") user_id: Long): UserResponse

    @GET("/v1/search/illust?search_target=exact_match_for_tags&filter=for_ios&include_translated_tag_results=true&merge_plain_keyword_results=true")
    suspend fun searchIllust(
        @Query("word") word: String,
        @Query("sort") sort: String
    ): IllustResponse


    // novel?merge_plain_keyword_results=true&sort=&include_translated_tag_results=true&search_target=exact_match_for_tags&word=%E6%80%96%E3%81%84%E8%A9%B1
    //novel?sort=popular_desc&include_translated_tag_results=true&merge_plain_keyword_results=true&word=%E6%80%96%E3%81%84%E8%A9%B1&search_target=exact_match_for_tags
    @GET("/v1/search/novel?include_translated_tag_results=true&merge_plain_keyword_results=true&search_target=partial_match_for_tags")
    suspend fun searchNovel(
        @Query("word") word: String,
        @Query("sort") sort: String,
    ): NovelResponse

    @GET("/v1/search/user?filter=for_ios")
    suspend fun searchUser(
        @Query("word") word: String
    ): UserPreviewResponse

    @GET("v1/search/popular-preview/illust?filter=for_ios&include_translated_tag_results=true&merge_plain_keyword_results=true&search_target=exact_match_for_tags")
    suspend fun popularIllust(
        @Query("word") word: String?
    ): IllustResponse

    @FormUrlEncoded
    @POST("/v1/user/follow/add")
    suspend fun postFollow(
        @Field("user_id") user_id: Long,
        @Field("restrict") followType: String
    )

    @FormUrlEncoded
    @POST("/v1/user/follow/delete")
    suspend fun postUnFollow(
        @Field("user_id") user_id: Long
    )

    @FormUrlEncoded
    @POST("/v1/watchlist/manga/add")
    suspend fun postAddBookmarkMangaSeries(
        @Field("series_id") series_id: Long,
    )

    @FormUrlEncoded
    @POST("/v1/watchlist/manga/delete")
    suspend fun postRemoveBookmarkMangaSeries(
        @Field("series_id") series_id: Long
    )

    @FormUrlEncoded
    @POST("/v1/illust/comment/add")
    suspend fun postComment(
        @Field("illust_id") illust_id: Long,
        @Field("comment") comment: String,
        @Field("parent_comment_id") parent_comment_id: Long? = null,
    ): AddCommentResponse

    @FormUrlEncoded
    @POST("/v2/illust/bookmark/add")
    suspend fun addBookmark(
        @Field("illust_id") illust_id: Long,
        @Field("restrict") followType: String
    )

    @FormUrlEncoded
    @POST("/v2/novel/bookmark/add")
    suspend fun addNovelBookmark(
        @Field("novel_id") novel_id: Long,
        @Field("restrict") followType: String
    )

    @FormUrlEncoded
    @POST("/v1/illust/bookmark/delete")
    suspend fun removeBookmark(
        @Field("illust_id") illust_id: Long
    )

    @FormUrlEncoded
    @POST("/v1/novel/bookmark/delete")
    suspend fun removeNovelBookmark(
        @Field("novel_id") novel_id: Long
    )


    @GET("/v1/user/recommended?filter=for_ios")
    suspend fun recommendUser(): UserPreviewResponse


    @GET("/v1/illust-series/illust?filter=for_ios")
    suspend fun getIllustSeriesSnapshot(
        @Query("illust_id") illust_id: Long
    ): IllustSeries

    @GET("/v1/illust/series?filter=for_ios")
    suspend fun getIllustSeriesDetail(
        @Query("illust_series_id") illust_series_id: Long
    ): IllustSeries

    @GET("/v2/novel/series")
    suspend fun getNovelSeriesDetail(
        @Query("series_id") series_id: Long
    ): NovelSeries

    @GET("/v1/trending-tags/{type}?filter=for_ios")
    suspend fun trendingTags(
        @Path("type") type: String,
    ): TrendingTagsResponse

    @GET("/v1/spotlight/articles?filter=for_ios")
    suspend fun articles(
        @Query("category") category: String
    ): ArticlesResponse

    @GET("/v1/watchlist/{type}")
    suspend fun watchlist(
        @Path("type") type: String,
    ): WatchlistResponse


    @GET("/v1/ugoira/metadata")
    suspend fun getGifZipInfo(
        @Query("illust_id") illust_id: Long
    ): GifInfoResponse

    @GET("/v1/illust/detail")
    suspend fun getIllustById(
        @Query("illust_id") illust_id: Long
    ): SingleIllustResponse

    @GET("/v1/user/following")
    suspend fun getUserFollowingList(
        @Query("user_id") user_id: Long,
        @Query("restrict") restrict: String,
    ): UserPreviewResponse

    @GET("/v1/user/follower?filter=for_ios")
    suspend fun getUserFansList(
        @Query("user_id") user_id: Long,
    ): UserPreviewResponse

    @GET("/v1/user/mypixiv")
    suspend fun getUserPixivFriendsList(
        @Query("user_id") user_id: Long,
    ): UserPreviewResponse

    @GET("/v3/illust/comments")
    suspend fun commentList(
        @Query("illust_id") illust_id: Long,
    ): CommentResponse

    @GET("/v2/illust/comment/replies")
    suspend fun replyList(
        @Query("comment_id") comment_id: Long,
    ): CommentResponse

    // 推荐用户
    @GET("/v1/user/related?filter=for_android")
    suspend fun relatedUsers(
        @Query("seed_user_id") seed_user_id: Long
    ): UserPreviewResponse


    @GET("/v1/user/ai-show-settings")
    suspend fun getAISettings(): AISettings

    @FormUrlEncoded
    @POST("/v1/user/ai-show-settings/edit")
    suspend fun postAISettings(
        @Field("show_ai") show_ai: Boolean
    ): AISettings

    @GET
    suspend fun getNextArticle(@Url next_url: String): ArticlesResponse

    @GET
    suspend fun generalGet(@Url url: String): ResponseBody
}