package ceui.lisa.slinky.glide

import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import okhttp3.OkHttpClient
import java.io.InputStream

class OkhttpModelLoader(private val okHttpClient: OkHttpClient) : ModelLoader<String, InputStream> {

    override fun buildLoadData(
        model: String,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(
            ObjectKey(model),
            OkHttpStreamFetcher(okHttpClient, model.toGlideUrl())
        )
    }

    override fun handles(model: String): Boolean {
        return true
    }
}

class OkhttpModelLoaderFactory(private val okHttpClient: OkHttpClient) :
    ModelLoaderFactory<String, InputStream> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
        return OkhttpModelLoader(okHttpClient)
    }

    override fun teardown() {
    }
}
