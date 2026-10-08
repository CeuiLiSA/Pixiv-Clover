package ceui.lisa.slinky.glide

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import ceui.lisa.slinky.R
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.ui.launchSuspend
import ceui.lisa.slinky.utils.SoundPlay
import ceui.lisa.slinky.utils.toGlideUrl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


suspend fun Fragment.selectOne(iterator: Iterable<String?>?): String {
    if (iterator == null) {
        return "Unknown"
    }

    val task = CompletableDeferred<String>()
    val builder = AlertDialog.Builder(requireContext())
    val items = mutableListOf<String>()
    iterator.forEach {
        items.add(it ?: "Unknown")
    }
    builder.setItems(
        items.toTypedArray()
    ) { dialog, which ->
        task.complete(items[which])
        dialog.dismiss()
    }
    builder.show()
    return task.await()
}


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
            withContext(Dispatchers.IO) {
                try {
                    val file =
                        GlideApp.with(requireContext()).asFile().load(url.toGlideUrl()).submit()
                            .get()
                    saveImage(ctx, file, illust, index)
                    isDownloadSuccessfully = true
                } catch (ex: Exception) {
                    isDownloadSuccessfully = false
                    handleError(ex)
                }
            }
            if (isDownloadSuccessfully) {
                withContext(Dispatchers.Main) {
                    showPush(title = getString(R.string.sava_illust_image_success_hint))
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
    illust: Illust,
    index: Int
) {

    val values = ContentValues().apply {
        val title = if (illust.title?.isNotEmpty() == true) {
            illust.title
        } else {
            "No title"
        }
        val displayName = if (illust.page_count == 1) {
            "${title}_${illust.id}.png"
        } else {
            "${title}_${illust.id}_${index}.png"
        }
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