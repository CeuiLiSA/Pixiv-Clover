package ceui.lisa.slinky.ui.task

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ceui.lisa.slinky.R
import ceui.lisa.slinky.glide.GlideProgress
import ceui.lisa.slinky.glide.ProgressListener
import ceui.lisa.slinky.glide.SlinkyGlideModule
import ceui.lisa.slinky.utils.IMAGE_REFERER
import ceui.lisa.slinky.utils.MAP_KEY_SMALL
import com.blankj.utilcode.util.PathUtils
import okhttp3.Request
import okio.Okio
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class DownloadTask(val url: String) : HumanReadableTask<File>() {

    override val taskId: String
        get() = url
    val liveProgress = MutableLiveData(0)

    override fun executingTitle(context: Context): LiveData<String> {
        return liveProgress.map { progress ->
            "${context.getString(R.string.downloading)}(${progress}%)"
        }
    }

    override fun finishedTitle(context: Context): String {
        return context.getString(R.string.download_finished)
    }

    override suspend fun action(): File {
        val progressListener = object : ProgressListener {
            override fun onProgress(progress: Int) {
                liveProgress.postValue(progress)
            }

            override fun onFailed(ex: Exception) {
            }
        }
        val parentFile = File(PathUtils.getInternalAppCachePath() + "/SlinkyDownload/")
        if (!parentFile.exists()) {
            parentFile.mkdir()
        }

        val splits = url.split("/")
        val fileName = splits.lastOrNull() ?: "unknown_title.png"

        val file = File(parentFile, "image_download_${fileName}")
        if (!file.exists()) {
            file.createNewFile()
        }
        return downloadFileFromUrl(url, progressListener, file)
    }
}

suspend fun downloadFileFromUrl(url: String, listener: ProgressListener, destFile: File): File {
    return suspendCoroutine { continuation ->
        thread {
            try {
                GlideProgress.add(url, null, listener)
                val client = SlinkyGlideModule.okHttpClient
                val request =
                    Request.Builder().url(url).addHeader(MAP_KEY_SMALL, IMAGE_REFERER).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val sink = Okio.buffer(Okio.sink(destFile))
                        sink.writeAll(body.source())
                        sink.flush()
                        continuation.resume(destFile)
                    } else {
                        continuation.resumeWithException(FileDownloadFailedException("response body is null"))
                    }
                } else {
                    continuation.resumeWithException(FileDownloadFailedException("response is not isSuccessful"))
                }
            } catch (ex: Exception) {
                Timber.e(ex)
                listener.onFailed(ex)
                continuation.resumeWithException(ex)
            }
        }
    }
}

class FileDownloadFailedException(reason: String) : RuntimeException(reason)

fun copyFile(srcPath: String, destPath: String) {
    copyFile(File(srcPath), File(destPath))
}

fun copyFile(srcFile: File, destFile: File) {
    FileInputStream(srcFile).use { fis ->
        FileOutputStream(destFile).use { fos ->
            val buffer = ByteArray(1024)
            var len: Int
            while (fis.read(buffer).also { len = it } != -1) {
                fos.write(buffer, 0, len)
            }
        }
    }
}