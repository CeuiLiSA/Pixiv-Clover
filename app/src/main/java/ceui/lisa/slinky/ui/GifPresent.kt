package ceui.lisa.slinky.ui

import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import ceui.lisa.slinky.glide.ProgressListener
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.GifInfoResponse
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.styles.ProgressImageButton
import ceui.lisa.slinky.ui.task.downloadFileFromUrl
import ceui.lisa.slinky.utils.visibleOrInvisible
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ZipUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


fun NavFragment.startGifPresentWorkflowIfNeeded(
    illst: Illust,
    sender: ProgressImageButton,
    progressBar: ProgressBar,
    imageView: ImageView
) {
    slinkyLaunchWhenResumed {
        sender.showProgress(true)
        val gifInfoResponse =
            ObjectPool.get<GifInfoResponse>(illst.id).value ?: getGifZipInfo(illst)
        if (gifInfoResponse != null) {
            val cachedZipFolder =
                File(PathUtils.getInternalAppCachePath() + "/gifUnzipFiles/gif_unzip_folder_${illst.id}")
            if (cachedZipFolder.exists()) {
                notifyPlayGif(gifInfoResponse, cachedZipFolder, imageView)
                GifStateManager.update(illst.id, true)
            } else {
                progressBar.isVisible = true
                val gifZipFile = downloadGifZip(illst.id, gifInfoResponse, progressBar)
                if (gifZipFile != null) {
                    val unzipFolder = unzipGif(illst, gifZipFile)
                    if (unzipFolder != null) {
                        notifyPlayGif(gifInfoResponse, unzipFolder, imageView)
                        GifStateManager.update(illst.id, true)
                    } else {
                        GifStateManager.update(illst.id, false)
                    }
                } else {
                    GifStateManager.update(illst.id, false)
                }
                progressBar.isVisible = false
            }
            sender.showProgress(false)
            sender.visibleOrInvisible = false
        } else {
            sender.showProgress(false)
            GifStateManager.update(illst.id, false)
        }
    }
}


// 获得gif作品的详细信息，gif zip下载链接
suspend fun NavFragment.getGifZipInfo(illst: Illust): GifInfoResponse? {
    return try {
        val gifInfo = Client.appApi.getGifZipInfo(illst.id)
        val gifInfoWithId = gifInfo.copy(illustId = illst.id)
        ObjectPool.update(gifInfoWithId)
        gifInfoWithId
    } catch (ex: Exception) {
        handleError(ex)
        null
    }
}

// 下载包含gif 所有帧图片的 zip包
suspend fun NavFragment.downloadGifZip(
    illstId: Long,
    gifInfoResponse: GifInfoResponse,
    progressBar: ProgressBar
): File? {
    val url = gifInfoResponse.ugoira_metadata?.zip_urls?.medium
    if (url?.isNotEmpty() == true) {
        val progressListener = object : ProgressListener {
            override fun onProgress(progress: Int) {
                progressBar.progress = progress
            }

            override fun onFailed(ex: Exception) {
            }
        }
        val parentFile = File(PathUtils.getInternalAppCachePath() + "/gifZipFiles/")
        if (!parentFile.exists()) {
            parentFile.mkdir()
        }

        val file = File(parentFile, "gif_zip_${illstId}.zip")
        if (!file.exists()) {
            file.createNewFile()
        }
        return downloadFileFromUrl(url, progressListener, file)
    } else {
        return null
    }
}

// 将zip 包中的每一张解压到对应作品的文件夹
suspend fun unzipGif(illst: Illust, zipFile: File): File? {
    return suspendCoroutine { continuation ->
        thread {
            try {
                val parentFile = File(PathUtils.getInternalAppCachePath() + "/gifUnzipFiles/")
                if (!parentFile.exists()) {
                    parentFile.mkdir()
                }

                val unzipFolder = File(parentFile, "gif_unzip_folder_${illst.id}")
                if (!unzipFolder.exists()) {
                    unzipFolder.mkdir()
                }

                ZipUtils.unzipFile(zipFile, unzipFolder)
                zipFile.delete()
                continuation.resume(unzipFolder)
            } catch (ex: Exception) {
                continuation.resumeWithException(ex)
            }
        }
    }
}

suspend fun notifyPlayGif(gifInfo: GifInfoResponse, unzipFolder: File, imageView: ImageView) {
    withContext(Dispatchers.IO) {
        val animationDrawable = AnimationDrawable().apply {
            isOneShot = false
        }
        val delay = gifInfo.ugoira_metadata?.frames?.getOrNull(0)?.delay ?: 80
        unzipFolder.listFiles()?.forEach {
            val frame = Drawable.createFromPath(it.path)
            if (frame != null) {
                animationDrawable.addFrame(frame, delay)
            }
        }
        withContext(Dispatchers.Main) {
            imageView.setImageDrawable(animationDrawable)
            animationDrawable.start()
        }
    }
}

object GifStateManager {

    val isGifPrepared = mutableMapOf<Long, Boolean?>()

    fun update(illustId: Long, isPrepared: Boolean) {
        isGifPrepared[illustId] = isPrepared
    }
}
