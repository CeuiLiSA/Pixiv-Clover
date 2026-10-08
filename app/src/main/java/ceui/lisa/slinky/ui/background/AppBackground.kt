package ceui.lisa.slinky.ui.background

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation

interface AppBackground {

    fun backgroundType(): Int

    fun render(imageView: ImageView)

    companion object {
        const val DEFAULT_COLOR = "#111111"
    }
}

class IllustBackground(val illustId: Long, private val fileUri: String) : AppBackground {

    override fun backgroundType(): Int {
        return BackgroundType.CHOOSE_ILLUST
    }

    override fun render(imageView: ImageView) {
        Glide.with(imageView)
            .load(Uri.parse(fileUri))
            .into(imageView)
    }

    override fun toString(): String {
        return "IllustBackground(illustId=$illustId)"
    }
}

class PureColorBackground(private val colorString: String) : AppBackground {
    override fun backgroundType(): Int {
        return BackgroundType.PURE_COLOR
    }

    override fun render(imageView: ImageView) {
        val color = try {
            Color.parseColor(colorString)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Color.parseColor(AppBackground.DEFAULT_COLOR)
        }
        imageView.setImageDrawable(ColorDrawable(color))
    }

    override fun toString(): String {
        return "PureColorBackground(colorString='$colorString')"
    }
}

class FileFromGalleryBackground(val fileUri: String) : AppBackground {

    override fun backgroundType(): Int {
        return BackgroundType.FILE_FROM_GALLERY
    }

    override fun render(imageView: ImageView) {
        Glide.with(imageView)
            .load(Uri.parse(fileUri))
            .into(imageView)
    }
}