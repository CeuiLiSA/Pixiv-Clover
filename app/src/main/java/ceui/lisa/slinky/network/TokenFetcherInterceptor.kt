package ceui.lisa.slinky.network

import android.text.TextUtils
import okhttp3.Interceptor
import okhttp3.Response

class TokenFetcherInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        return if (response.code() == 400) {
            val errorJson = response.peekBody(Long.MAX_VALUE).string()
            if (errorJson.contains(Client.TOKEN_ERROR_1) || errorJson.contains(Client.TOKEN_ERROR_2)) {
                response.close()
                val oldToken = request.header(Client.HEADER_AUTH)
                    ?.substring(Client.TOKEN_HEAD.length) ?: ""
                val accessToken = refreshToken(oldToken)
                if (accessToken != null) {
                    val newRequest = chain.request()
                        .newBuilder()
                        .header(Client.HEADER_AUTH, Client.TOKEN_HEAD + accessToken)
                        .build()
                    chain.proceed(newRequest)
                } else {
                    response
                }
            } else {
                response
            }
        } else {
            response
        }
    }

    @Synchronized
    private fun refreshToken(oldToken: String): String? {
        if (!TextUtils.equals(Settings.loggedInAccount.access_token, oldToken)) {
            return Settings.loggedInAccount.access_token
        }

        val refreshToken = Settings.loggedInAccount.refresh_token ?: ""
        try {
            val accountResponse = Client.authApi.refreshToken(refreshToken).execute().body()
            if (accountResponse != null) {
                Settings.updateLoggedInUser(accountResponse)
                return accountResponse.access_token
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}