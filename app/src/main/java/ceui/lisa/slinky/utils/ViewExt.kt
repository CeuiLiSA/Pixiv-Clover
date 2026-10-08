package ceui.lisa.slinky.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ceui.lisa.slinky.R
import ceui.lisa.slinky.models.User
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import timber.log.Timber

@BindingAdapter("srcValue")
fun ImageView.binding_srcValue(srcRes: Int?) {
    if (srcRes != null) {
        setImageResource(srcRes)
    }
}

@BindingAdapter("loadUserHead")
fun ImageView.binding_loadUserHead(user: User?) {
    val self = this
    if (user != null) {
        val maxSizeUrl = user.profile_image_urls?.findMaxSizeUrl()?.toGlideUrl()

        val existing = self.getTag(R.id.user_head_icon_tag) as? GlideUrl
        if (existing?.toStringUrl() == maxSizeUrl?.toStringUrl()) {
            Timber.d("old: ${existing}, new: ${maxSizeUrl}")
            return
        }

        Glide.with(context)
            .load(maxSizeUrl)
            .placeholder(R.drawable.mask_src_circle)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    self.setTag(R.id.user_head_icon_tag, null)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    self.setTag(R.id.user_head_icon_tag, maxSizeUrl)
                    return false
                }
            })
            .into(this)
    }
}


@BindingAdapter("setTextWithAt")
fun TextView.binding_setTextWithAt(content: String?) {
    if (content != null) {
        text = context.getString(R.string.account_at, content)
    }
}

@set:BindingAdapter("visibleOrGone")
var View.visibleOrGone
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

@set:BindingAdapter("visibleOrInvisible")
var View.visibleOrInvisible
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }