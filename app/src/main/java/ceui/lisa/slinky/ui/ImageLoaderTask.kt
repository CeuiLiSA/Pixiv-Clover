package ceui.lisa.slinky.ui

import android.graphics.BitmapFactory
import android.util.Size
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.ui.task.SlinkyTask
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class ImageLoaderTask(private val fragment: NavFragment, val url: String) :
    SlinkyTask<SizedImageFile?>() {


    override suspend fun action(): SizedImageFile? {
        val exist = cachedResult
        if (exist != null) {
            return exist
        }

        val task = CompletableDeferred<SizedImageFile?>()
        withContext(Dispatchers.IO) {
            try {
                val requestListener = object : RequestListener<File> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<File>,
                        isFirstResource: Boolean
                    ): Boolean {
                        task.complete(null)
                        return false
                    }

                    override fun onResourceReady(
                        file: File,
                        model: Any,
                        target: Target<File>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        val imageSize = decodeImageFileWidthHeight(file)
                        if (imageSize != null) {
                            val result = SizedImageFile(file, imageSize)
                            cachedResult = result
                            task.complete(result)
                        } else {
                            task.complete(null)
                        }
                        return false
                    }
                }
                GlideApp.with(fragment)
                    .asFile()
                    .load(url)
                    .addListener(requestListener)
                    .submit()
                    .get()
            } catch (outException: Exception) {
                Timber.e(outException)
                task.complete(null)
            }
        }
        return task.await()
    }
}


fun decodeImageFileWidthHeight(file: File): Size? {
    return try {
        val startTime = System.currentTimeMillis()
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.path, options)
        val width = options.outWidth
        val height = options.outHeight
        val endTime = System.currentTimeMillis()
        Timber.d("decodeImageFileWidthHeight took ${endTime - startTime}ms")
        Size(width, height)
    } catch (ex: Exception) {
        Timber.e(ex)
        null
    }
}