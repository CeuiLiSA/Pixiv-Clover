package ceui.lisa.slinky.ui.background

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.ItemAppBackgroundBinding
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.ui.findFragmentOrNull
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.Glide

class BackgroundHolder(
    val illust: Illust? = null,
    val currentBackground: LiveData<AppBackground>,
    val backgroundType: Int,
    val backgroundColorString: String? = null,
) : SlinkyItem() {


    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illust == (other as? BackgroundHolder)?.illust &&
                currentBackground.value == (other as? BackgroundHolder)?.currentBackground?.value &&
                backgroundType == (other as? BackgroundHolder)?.backgroundType &&
                backgroundColorString == (other as? BackgroundHolder)?.backgroundColorString
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illust == (other as? BackgroundHolder)?.illust &&
                currentBackground.value == (other as? BackgroundHolder)?.currentBackground?.value &&
                backgroundType == (other as? BackgroundHolder)?.backgroundType &&
                backgroundColorString == (other as? BackgroundHolder)?.backgroundColorString
    }
}


@ItemHolder(BackgroundHolder::class)
class BackgroundViewHolder(aa: ItemAppBackgroundBinding) :
    SlinkyViewHolder<ItemAppBackgroundBinding, BackgroundHolder>(aa) {

    override fun onBindViewHolder(item: BackgroundHolder) {
        super.onBindViewHolder(item)
        binding.backgroundType = item.backgroundType
        binding.currentBackground = item.currentBackground
        binding.imageView.setOnClick {
            it.findFragmentOrNull<BackgroundFragment>()?.didClickBackgroundHolder(it, item)
        }

        if (item.backgroundType == BackgroundType.CHOOSE_ILLUST && item.illust != null) {
            GlideApp.with(context)
                .load(item.illust.image_urls?.large?.toGlideUrl())
                .into(binding.imageView)
        } else if (item.backgroundType == BackgroundType.PURE_COLOR && item.backgroundColorString != null) {
            val color = try {
                Color.parseColor(item.backgroundColorString)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Color.parseColor(AppBackground.DEFAULT_COLOR)
            }
            GlideApp.with(context)
                .load(ColorDrawable(color))
                .into(binding.imageView)
        } else if (item.backgroundType == BackgroundType.FILE_FROM_GALLERY) {
            GlideApp.with(context)
                .load(ColorDrawable(context.getColor(R.color.page_default_background)))
                .into(binding.imageView)
        }

        item.currentBackground.observe(lifecycleOwner) { currentBackground ->
            val type = currentBackground?.backgroundType()
            if (type == BackgroundType.CHOOSE_ILLUST) {
                binding.selectedIcon.isVisible =
                    currentBackground is IllustBackground && currentBackground.illustId == item.illust?.id
            } else if (type == BackgroundType.PURE_COLOR && item.backgroundType == BackgroundType.PURE_COLOR) {
                val color = try {
                    Color.parseColor(item.backgroundColorString)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Color.parseColor(AppBackground.DEFAULT_COLOR)
                }
                GlideApp.with(context)
                    .load(ColorDrawable(color))
                    .into(binding.imageView)
                binding.selectedIcon.isVisible = true
            } else if (type == BackgroundType.FILE_FROM_GALLERY && item.backgroundType == BackgroundType.FILE_FROM_GALLERY) {
                if (currentBackground is FileFromGalleryBackground) {
                    Glide.with(context)
                        .load(Uri.parse(currentBackground.fileUri))
                        .into(binding.imageView)
                }
                binding.selectedIcon.isVisible = true
            } else {
                binding.selectedIcon.isVisible = false
            }
        }
    }
}