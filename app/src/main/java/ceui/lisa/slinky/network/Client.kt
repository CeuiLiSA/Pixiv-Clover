package ceui.lisa.slinky.network

import ceui.lisa.slinky.network.api.API
import ceui.lisa.slinky.network.api.OAuthAPI
import ceui.lisa.slinky.network.api.PixivWebApi
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Client {

    const val APP_API_HOST = "https://app-api.pixiv.net"
    const val OAUTH_HOST = "https://oauth.secure.pixiv.net"
    const val WEB_API_HOST = "https://www.pixiv.net"

    const val CLIENT_ID = "KzEZED7aC0vird8jWyHM38mXjNTY"
    const val CLIENT_SECRET = "W9JZoJe00qPvJsiyCGT3CCtC6ZUtdpKpzMbNlUGP"
    const val GRANT_REFRESH_TOKEN = "refresh_token"
    const val GRANT_AUTH_CODE = "authorization_code"

    const val CALLBACK_LINK = "https://app-api.pixiv.net/web/v1/users/auth/pixiv/callback"

    const val TOKEN_HEAD = "Bearer "

    const val HEADER_AUTH = "authorization"

    const val REQUIEST_TIME = 15L

    const val TOKEN_ERROR_1 = "Error occurred at the OAuth process"
    const val TOKEN_ERROR_2 = "Invalid refresh token"


    val appApi: API by lazy {
        createAPPAPI(API::class.java, APP_API_HOST, true)
    }

    val authApi: OAuthAPI by lazy {
        createAPPAPI(OAuthAPI::class.java, OAUTH_HOST, false)
    }

    val webApi: PixivWebApi by lazy {
        createWebAPIService(PixivWebApi::class.java, WEB_API_HOST)
    }

    private fun <T> createAPPAPI(service: Class<T>, hostUrl: String, autoRefreshToken: Boolean): T {
        val httpBuilder = OkHttpClient.Builder()
            .connectTimeout(REQUIEST_TIME, TimeUnit.SECONDS)
            .writeTimeout(REQUIEST_TIME, TimeUnit.SECONDS)
            .readTimeout(REQUIEST_TIME, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_1_1))

        httpBuilder.addInterceptor(HeaderInterceptor())

        if (autoRefreshToken) {
            httpBuilder.addInterceptor(TokenFetcherInterceptor())
        }

        return Retrofit.Builder()
            .baseUrl(hostUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpBuilder.build())
            .build()
            .create(service)
    }

    private fun <T> createWebAPIService(service: Class<T>, url: String): T {
        val httpBuilder = OkHttpClient.Builder()
            .connectTimeout(REQUIEST_TIME, TimeUnit.SECONDS)
            .writeTimeout(REQUIEST_TIME, TimeUnit.SECONDS)
            .readTimeout(REQUIEST_TIME, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_1_1))

        httpBuilder.addInterceptor(WebHeaderInterceptor())

        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpBuilder.build())
            .build()
            .create(service)
    }
}