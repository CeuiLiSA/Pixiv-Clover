package ceui.lisa.slinky.ui

import android.animation.AnimatorInflater
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import androidx.fragment.app.findFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.setUpLoadingState
import ceui.lisa.slinky.databinding.ItemDescBinding
import ceui.lisa.slinky.databinding.ItemIllustInfoBinding
import ceui.lisa.slinky.databinding.ItemLoadingBinding
import ceui.lisa.slinky.databinding.ItemSingleIllustBinding
import ceui.lisa.slinky.databinding.ItemTitleBinding
import ceui.lisa.slinky.glide.LiveDataProgressListener
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import ceui.lisa.slinky.ui.settings.LocalSetting
import ceui.lisa.slinky.utils.openWebPageWithSystemBrowser
import ceui.lisa.slinky.utils.toGlideUrl
import ceui.lisa.slinky.utils.visibleOrGone
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.progressindicator.CircularProgressIndicator
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt


object GlideState {
    const val IDLE = 0
    const val LOADING = 1
    const val FINISHED = 2
    const val FAILED = 3
}

@BindingAdapter("liveProgress")
fun CircularProgressIndicator.binding_set_live_progress(p: Int?) {
    progress = p ?: 0
}

class SingleIllustHolder(
    val illust: Illust,
    val url: String,
    val width: Int = 0,
    val height: Int = 0,
    val index: Int,
    val shouldResize: Boolean,
    val loader: ImageLoaderTask,
    val progressListener: LiveDataProgressListener
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return url == (other as? SingleIllustHolder)?.url
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illust == (other as? SingleIllustHolder)?.illust &&
                url == (other as? SingleIllustHolder)?.url &&
                width == (other as? SingleIllustHolder)?.width &&
                height == (other as? SingleIllustHolder)?.height &&
                index == (other as? SingleIllustHolder)?.index &&
                shouldResize == (other as? SingleIllustHolder)?.shouldResize
    }
}

fun View.fixSize(width: Int, height: Int) {
    val itemWidth = screenWidth
    val itemHeight = (itemWidth * height) / width

    val params = layoutParams
    params.width = itemWidth
    params.height = itemHeight
    layoutParams = params
}


@ItemHolder(SingleIllustHolder::class)
class SingleIllustViewHolder(aa: ItemSingleIllustBinding) :
    SlinkyViewHolder<ItemSingleIllustBinding, SingleIllustHolder>(aa) {

    override fun onBindViewHolder(item: SingleIllustHolder) {
        super.onBindViewHolder(item)
        binding.item = item

        val itemWidth = screenWidth

        fun resetImageViewToDefaultWidthHeight() {
            if (item.width != 0 && item.height != 0) {
                binding.imageView.fixSize(item.width, item.height)
            } else {
                binding.imageView.updateLayoutParams {
                    width = itemWidth
                    height = 230.pxValue
                }
            }
        }

        if (item.width != 0 && item.height != 0) {
            binding.imageView.fixSize(item.width, item.height)

            if (item.shouldResize) {
                val halfScreen = (screenHeight / 8F * 3F).roundToInt()
                val itemHeight = (itemWidth * item.height) / item.width
                if (itemHeight < halfScreen) {
                    val preferred = (screenHeight / 5F * 3F).roundToInt()
                    binding.resizeFrameLayout.isVisible = true
                    binding.resizeFrameLayout.fixSize(itemWidth, preferred)

                    Glide.with(context)
                        .load(item.illust.image_urls?.large?.toGlideUrl())
                        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.blurImageView)
                } else {
                    binding.resizeFrameLayout.isVisible = false
                }
            } else {
                binding.resizeFrameLayout.isVisible = false
            }
        } else {
            resetImageViewToDefaultWidthHeight()
            binding.resizeFrameLayout.isVisible = false
        }

        val loadImageBlock = {
            lifecycleOwner.lifecycleScope.launch {
                item.progressListener.glideState.value = GlideState.LOADING
                val sizedImageFile = item.loader.action()
                if (sizedImageFile != null) {
                    if (item.index == 0) {
                        binding.imageView.fixSize(item.illust.width, item.illust.height)
                    } else {
                        binding.imageView.fixSize(
                            sizedImageFile.size.width,
                            sizedImageFile.size.height
                        )
                    }
                    Glide.with(context)
                        .load(sizedImageFile.file)
                        .priority(Priority.IMMEDIATE)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.imageView)
                    Timber.d("ImageLoaderTask finished index: ${item.index} width: ${sizedImageFile.size.width}, height: ${sizedImageFile.size.height}, ${sizedImageFile.file.path}")
                    item.progressListener.glideState.value = GlideState.FINISHED
                } else {
                    Timber.d("ImageLoaderTask failed index: ${item.index}")
                    item.progressListener.glideState.value = GlideState.FAILED
                    item.progressListener.progressLiveData.value = 0
                }
            }
        }

        binding.retryButton.setOnClick {
            loadImageBlock.invoke()
        }

        if (item.progressListener.glideState.value != GlideState.FAILED) {
            loadImageBlock.invoke()
        } else {
            resetImageViewToDefaultWidthHeight()
        }

        if (item.illust.isGif()) {
            if (GifStateManager.isGifPrepared[item.illust.id] == true) {
                binding.playGif.isVisible = false
                val playGifButton = binding.playGif
                playGifButton.post {
                    playGifButton.findFragment<NavFragment>().startGifPresentWorkflowIfNeeded(
                        item.illust,
                        binding.playGif,
                        binding.gifDownloadProgress,
                        binding.imageView
                    )
                }
            } else {
                binding.playGif.isVisible = true
                binding.playGif.setOnClick {
                    it.findFragment<NavFragment>().startGifPresentWorkflowIfNeeded(
                        item.illust,
                        binding.playGif,
                        binding.gifDownloadProgress,
                        binding.imageView
                    )
                }
            }
        } else {
            binding.root.setOnClick {
                it.findActionReceiverOrNull<ShowImageViewPager>()?.showImageViewPager(item.illust.id, item.index)
            }
            binding.playGif.isVisible = false
        }
    }
}

fun Int?.exist(): Boolean {
    val self = this
    return if (self == null) {
        false
    } else {
        self > 0
    }
}

class LoadingHolder(val loadState: LiveData<LoadState>, val refreshBlock: () -> Unit) :
    SlinkyItem() {


    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return loadState.value == (other as? LoadingHolder)?.loadState?.value
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return loadState.value == (other as? LoadingHolder)?.loadState?.value
    }
}


@ItemHolder(LoadingHolder::class)
class LoadingViewHolder(aa: ItemLoadingBinding) :
    SlinkyViewHolder<ItemLoadingBinding, LoadingHolder>(aa) {

    override fun onBindViewHolder(item: LoadingHolder) {
        super.onBindViewHolder(item)
        binding.setUpLoadingState(item.loadState, lifecycleOwner) {
            item.refreshBlock.invoke()
        }
    }
}

class IllustTitleHolder(val illust: LiveData<Illust>) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illust.value?.id == (other as? IllustTitleHolder)?.illust?.value?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illust.value == (other as? IllustTitleHolder)?.illust?.value
    }
}


@ItemHolder(IllustTitleHolder::class)
class IllustTitleViewHolder(aa: ItemTitleBinding) :
    SlinkyViewHolder<ItemTitleBinding, IllustTitleHolder>(aa) {

    override fun onBindViewHolder(item: IllustTitleHolder) {
        super.onBindViewHolder(item)
        binding.item = item.illust
        binding.content.text = item.illust.value?.title
        binding.bookmark.setOnClick {
            it.findFragment<NavFragment>().didClickBookmarkIllust(item.illust, binding.bookmark)
        }
        binding.bookmark.setOnLongClickListener {
            it.findFragment<NavFragment>().didLongClickBookmarkIllust(item.illust, binding.bookmark)
            true
        }
    }
}

class DescHolder(val content: String?) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return content == (other as? DescHolder)?.content
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return content == (other as? DescHolder)?.content
    }
}


@ItemHolder(DescHolder::class)
class DescViewHolder(aa: ItemDescBinding) : SlinkyViewHolder<ItemDescBinding, DescHolder>(aa) {

    override fun onBindViewHolder(item: DescHolder) {
        super.onBindViewHolder(item)
        binding.content.movementMethod = LinkMovementMethod.getInstance()
        binding.content.setCaption(item.content)
    }
}

class IllustInfo(val illust: Illust, val localSetting: LocalSetting) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illust.id == (other as? IllustInfo)?.illust?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illust == (other as? IllustInfo)?.illust &&
                localSetting == (other as? IllustInfo)?.localSetting
    }
}


@ItemHolder(IllustInfo::class)
class IllustInfoViewHolder(aa: ItemIllustInfoBinding) :
    SlinkyViewHolder<ItemIllustInfoBinding, IllustInfo>(aa) {

    override fun onBindViewHolder(item: IllustInfo) {
        super.onBindViewHolder(item)
        binding.illustId.text = item.illust.id.toString()
        binding.illustId.setOnClick {
            it.findFragment<IllustFragment>().apply {
                launchSuspend {
                    if (alertYesOrCancel(message = getString(R.string.copy_illust_id))) {
                        TextUtil.copyToPB(requireContext(), item.illust.id.toString())
                        showPush(title = getString(R.string.copied))
                    }
                }
            }
        }
        binding.userId.text = item.illust.user?.id?.toString()
        binding.userId.setOnClick {
            it.findFragment<IllustFragment>().apply {
                launchSuspend {
                    if (alertYesOrCancel(message = getString(R.string.copy_user_id))) {
                        TextUtil.copyToPB(requireContext(), item.illust.user?.id?.toString() ?: "")
                        showPush(title = getString(R.string.copied))
                    }
                }
            }
        }
        binding.visit.text = (item.illust.total_view ?: 0).toString()
        binding.resolution.text =
            context.getString(R.string.resolution_px, item.illust.width, item.illust.height)
        binding.like.text = (item.illust.total_bookmarks ?: 0).toString()
        val toolList = item.illust.tools
        if (toolList?.isNotEmpty() == true) {
            val tools = toolList.joinToString(", ")
            (binding.tools.parent as ViewGroup).visibleOrGone = true
            binding.tools.text = tools
        } else {
            (binding.tools.parent as ViewGroup).visibleOrGone = false
        }

        val illustWebPageUrl = ILLUST_URL_HEAD + item.illust.id
        binding.showIllustInBrowser.text = illustWebPageUrl
        binding.showIllustInBrowser.setOnClick {
            it.findFragment<IllustFragment>().openWebPageWithSystemBrowser(illustWebPageUrl)
        }
        binding.showIllustInBrowser.setOnLongClickListener {
            it.findFragment<IllustFragment>().apply {
                launchSuspend {
                    if (alertYesOrCancel(message = getString(R.string.copy_link))) {
                        TextUtil.copyToPB(requireContext(), illustWebPageUrl)
                        showPush(title = getString(R.string.copied))
                    }
                }
            }
            true
        }

        binding.userLinkLl.visibleOrGone = true
        val userWebPageUrl = USER_URL_HEAD + item.illust.user?.id
        binding.showUserInBrowser.text = userWebPageUrl
        binding.showUserInBrowser.setOnClick {
            it.findFragment<IllustFragment>().openWebPageWithSystemBrowser(userWebPageUrl)
        }
        binding.showUserInBrowser.setOnLongClickListener {
            it.findFragment<IllustFragment>().apply {
                launchSuspend {
                    if (alertYesOrCancel(message = getString(R.string.copy_link))) {
                        TextUtil.copyToPB(requireContext(), userWebPageUrl)
                        showPush(title = getString(R.string.copied))
                    }
                }
            }
            true
        }

    }
}

const val PIXIV_URL_HEAD = "https://www.pixiv.net/"
const val ILLUST_URL_HEAD = "https://www.pixiv.net/artworks/"
const val USER_URL_HEAD = "https://www.pixiv.net/users/"

const val url_artworks = "/artworks/"
const val url_users = "/users/"

object TextUtil {

    fun copyToPB(context: Context, content: String) {
        val clipboard: ClipboardManager? =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("message", content)
        clipboard?.setPrimaryClip(clip)
    }

}

fun <T : View> T.setOnClick(listener: (T) -> Unit) {
    stateListAnimator =
        AnimatorInflater.loadStateListAnimator(context, R.animator.button_press_alpha)
    setOnClickListener {
        listener(this)
    }
}