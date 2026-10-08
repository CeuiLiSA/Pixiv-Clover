package ceui.lisa.slinky.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class SlinkyGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            String::class.java,
            InputStream::class.java,
            OkhttpModelLoaderFactory(okHttpClient)
        )
    }

    companion object {
        val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder().addInterceptor(ProgressInterceptor()).build()
        }
    }
}