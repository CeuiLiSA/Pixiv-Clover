package ceui.lisa.slinky.glide

import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.fragment.app.Fragment
import ceui.lisa.slinky.R
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.ui.dialog.alertTwoChoicesOrCancel
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import ceui.lisa.slinky.ui.exist
import ceui.lisa.slinky.ui.launchSuspend
import ceui.lisa.slinky.utils.SoundPlay
import ceui.lisa.slinky.utils.toGlideUrl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException


fun Fragment.saveImageImpl(
    illustId: Long,
    index: Int = 0
) {
    launchSuspend {
        val ctx = requireContext()
        val illust = ObjectPool.get<Illust>(illustId).value ?: return@launchSuspend

        val url = if (illust.page_count == 1) {
            illust.meta_single_page?.original_image_url
        } else {
            illust.meta_pages?.get(index)?.image_urls?.original
        }
        if (url != null) {
            var isDownloadSuccessfully: Boolean
            val title = if (illust.title?.isNotEmpty() == true) {
                illust.title
            } else {
                "No title"
            }
            var displayName = if (illust.page_count == 1) {
                "${title}_${illust.id}.png"
            } else {
                "${title}_${illust.id}_${index}.png"
            }
            val existingMediaId = withContext(Dispatchers.IO) {
                isImageAlreadySaved(ctx, displayName)
            }
            try {
                if (existingMediaId.exist()) {
                    require(existingMediaId != null)
                    val ret = alertTwoChoicesOrCancel(message = "同名图片文件已存在，继续下载吗", "下载并替换原文件", "下载并同时保留两者")
                    when (ret) {
                        1 -> {
                            withContext(Dispatchers.IO) {
                                if (deleteImageFromGallery(ctx, existingMediaId)) {
                                    Timber.d("adsadsw2 删除成功 旧的：existingMediaId ${existingMediaId}")
                                }
                                val file =
                                    GlideApp.with(requireContext()).asFile().load(url.toGlideUrl()).submit()
                                        .get()
                                saveImage(ctx, file, displayName)
                                isDownloadSuccessfully = true
                            }
                        }
                        2 -> {
                            withContext(Dispatchers.IO) {
                                val existingCount = getImageCountWithSameDisplayName(ctx, displayName)
                                Timber.d("adsadsw2 () $existingCount")
                                val extraInfo = "_(${existingCount + 1})"
                                val file =
                                    GlideApp.with(requireContext()).asFile().load(url.toGlideUrl()).submit()
                                        .get()
                                displayName = "${displayName.split(".png")[0]}${extraInfo}.png"
                                saveImage(ctx, file, displayName)
                                isDownloadSuccessfully = true
                            }
                            isDownloadSuccessfully = true
                        }
                        else -> {
                            isDownloadSuccessfully = false
                        }
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        val file =
                            GlideApp.with(requireContext()).asFile().load(url.toGlideUrl()).submit()
                                .get()
                        saveImage(ctx, file, displayName)
                        isDownloadSuccessfully = true
                    }
                }
            } catch (ex: Exception) {
                isDownloadSuccessfully = false
                handleError(ex)
            }
            if (isDownloadSuccessfully) {
                withContext(Dispatchers.Main) {
                    showPush(
                        title = getString(R.string.sava_illust_image_success_hint),
                        "/Pictures/Slinky/${displayName}"
                    )
                }

                if (Settings.settingsInstance.value?.downloadSuccessfullySound == true) {
                    SoundPlay.play(R.raw.download_fixed)
                }
            }
        }
    }
}

@Throws(IOException::class)
private fun saveImage(
    context: Context,
    file: File,
    displayName: String
) {

    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Slinky")
        }
    }

    val resolver = context.contentResolver
    var uri: Uri? = null

    try {
        uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Failed to create new MediaStore record.")

        resolver.openOutputStream(uri)?.use {
            it.write(file.readBytes())
        } ?: throw IOException("Failed to open output stream.")
    } catch (e: IOException) {
        uri?.let { orphanUri ->
            resolver.delete(orphanUri, null, null)
        }
        throw e
    }
}

fun isImageAlreadySaved(context: Context, displayName: String): Long? {
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME
    )
    val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
    val selectionArgs = arrayOf(displayName)

    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )

    var existingMediaId: Long? = null

    if (cursor != null && cursor.moveToFirst()) {
        existingMediaId = cursor.getLongOrNull(0)
    }

    cursor?.close()


    Timber.d("adsadsw2 existingMediaId ${existingMediaId}")

    return existingMediaId
}

fun deleteImageFromGallery(context: Context, imageId: Long): Boolean {
    val resolver: ContentResolver = context.contentResolver
    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val selection = "${MediaStore.Images.Media._ID} = ?"
    val selectionArgs = arrayOf(imageId.toString())
    val deletedRows = resolver.delete(uri, selection, selectionArgs)
    return deletedRows > 0
}

fun getImageCountWithSameDisplayName(context: Context, displayName: String): Int {
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME
    )
    val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
    val selectionArgs = arrayOf("$displayName%")

    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )

    val count = cursor?.count ?: 0
    cursor?.close()

    return count
}