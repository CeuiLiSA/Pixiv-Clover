package ceui.lisa.slinky.network.api

import ceui.lisa.slinky.models.SquareResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface PixivWebApi {

    //

    @GET("/rpc/index.php?mode=latest_message_threads2&num=10&offset=0")
    suspend fun getMessageList()

    @GET("/ajax/illust/{illust_id}")
    suspend fun getWebIllust(@Path("illust_id") illust_id: Long)

    @GET("/ajax/top/illust?mode=all&lang=zh")
    suspend fun getSquareContents(): SquareResponse


    @GET("/touch/ajax/recommender/top_items?mode=safe&lang=zh")
    suspend fun getMessageListBBBB()




}