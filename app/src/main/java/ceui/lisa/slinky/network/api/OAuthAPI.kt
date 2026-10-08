package ceui.lisa.slinky.network.api

import ceui.lisa.slinky.models.AccountResponse
import ceui.lisa.slinky.network.Client
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OAuthAPI {

    @FormUrlEncoded
    @POST("/auth/token")
    fun refreshToken(
        @Field("refresh_token") refresh_token: String,
        @Field("include_policy") include_policy: Boolean = true,
        @Field("client_id") client_id: String = Client.CLIENT_ID,
        @Field("client_secret") client_secret: String = Client.CLIENT_SECRET,
        @Field("grant_type") grant_type: String = Client.GRANT_REFRESH_TOKEN,
    ): Call<AccountResponse>

    @FormUrlEncoded
    @POST("/auth/token")
    suspend fun logIn(
        @Field("client_id") client_id: String = Client.CLIENT_ID,
        @Field("code_verifier") code_verifier: String,
        @Field("client_secret") client_secret: String = Client.CLIENT_SECRET,
        @Field("grant_type") grant_type: String = Client.GRANT_AUTH_CODE,
        @Field("include_policy") include_policy: Boolean = true,
        @Field("code") code: String,
        @Field("redirect_uri") redirect_uri: String = Client.CALLBACK_LINK
    ): AccountResponse
}
