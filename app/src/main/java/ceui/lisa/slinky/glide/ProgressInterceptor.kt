package ceui.lisa.slinky.glide

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ProgressInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request()
            val response = chain.proceed(request)
            val url = request.url().toString()
            val responseBody = response.body() ?: return response
            return response.newBuilder().body(ProgressResponseBody(responseBody, url)).build()
        } catch (ex: Exception) {
            throw ex
        }
    }
}