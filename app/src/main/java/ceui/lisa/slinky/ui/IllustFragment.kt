package ceui.lisa.slinky.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.slinky.ActionItem
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CMFragmentArgs
import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.core.SLAdapter
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpDisablePage
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.core.valueViewModel
import ceui.lisa.slinky.databinding.FragmentIllustBinding
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.glide.saveImageImpl
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.ui.dialog.BottomDialog
import ceui.lisa.slinky.ui.dialog.BottomDialogArgs
import ceui.lisa.slinky.utils.toGlideUrl
import com.blankj.utilcode.util.ImageUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import timber.log.Timber


class IllustFragment : SlinkyListFragment(R.layout.fragment_illust),
    PostAction, TagAction, ShowImageViewPager, SeeMoreAction {

    private val safeArgs: IllustFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentIllustBinding::bind)
    private val viewModel by listViewModel({ safeArgs.illustId }) { illustId ->
        IllustFragmentRepository(illustId, illustAuthorId)
    }
    private val liveIllust by lazy { ObjectPool.get<Illust>(safeArgs.illustId) }

    private val illustAuthorId: Long
        get() {
            return liveIllust.value?.user?.id ?: 0L
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        setUpDisablePage(binding.deletedFrame)
        val context = requireContext()
        val layoutManager = LinearLayoutManager(context)
        binding.listView.layoutManager = layoutManager
        binding.scrollToBottom.setOnClick {
            binding.listView.smoothScrollToPosition((liveIllust.value?.page_count ?: 0) + 8)
        }
        binding.listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if ((binding.listView.adapter as? SLAdapter)?.getItemViewType(lastVisibleItem) == SingleIllustHolder::class.java.hashCode()) {
                    binding.scrollToBottom.isVisible = true
                } else {
                    binding.scrollToBottom.isVisible = false
                }
            }
        })

        liveIllust.observe(viewLifecycleOwner) { illust ->
            if (illust != null) {
                if (illust.user?.id != null && illust.user.id != 0L) {
                    if (illust.isGif()) {
                        actionbarContent.endItems.value = listOf(
                            ActionItem(R.drawable.icon_comment) {
                                pushFragment(
                                    R.id.commentListFragmentV2,
                                    CMFragmentArgs(illust.id, illust.user.id).toBundle()
                                )
                            }
                        )
                    } else {
                        if (illust.page_count > 1) {
                            illust.meta_pages?.forEach { metaPage ->
                                metaPage.image_urls?.large?.toGlideUrl()?.let {
                                    GlideApp.with(context).load(it).preload()
                                }
                            }
                        }
                        actionbarContent.endItems.value = listOf(
                            ActionItem(R.drawable.ic_action_download) {
                                if (illust.page_count == 1) {
                                    saveImageImpl(safeArgs.illustId)
                                } else {
                                    BottomDialog(this).apply {
                                        arguments = BottomDialogArgs(safeArgs.illustId).toBundle()
                                    }.show(childFragmentManager, BottomDialog.TAG)
                                }
                            },
                            ActionItem(R.drawable.icon_comment) {
                                pushFragment(
                                    R.id.commentListFragmentV2,
                                    CMFragmentArgs(illust.id, illust.user.id).toBundle()
                                )
                            }
                        )
                    }
                    binding.goToSeries.isVisible = illust.series?.id.exist()
                    binding.goToSeries.setOnClick {
                        pushFragment(
                            R.id.navigation_style_fragment,
                            StyleFragmentArgs(
                                illust.series?.id ?: 0L, ObjectType.ILLUST
                            ).toBundle()
                        )
                    }
                }
                visitIllust(illust)
            }
        }
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    override fun isAbandonedPage(): Boolean {
        val illust = ObjectPool.get<Illust>(safeArgs.illustId).value ?: return true
        return illust.isDisabled()
    }

    override fun onClickUser(user: User) {
        onClickUserImpl(user)
    }

    override fun searchTag(name: String) {
        searchTagImpl(name, false)
    }

    override fun showImageViewPager(illustId: Long, index: Int) {
        pushFragment(
            R.id.imageViewPagerFragment,
            OriginalImageViewPagerFragmentArgs(illustId, index).toBundle()
        )
    }

    override fun seeMore(type: Int) {
        if (type == SeeMoreType.RELATED_ILLUST) {
            pushFragment(R.id.relatedIllustFragment, safeArgs.toBundle())
        } else if (type == SeeMoreType.CREATED_ILLUST) {
            pushFragment(
                R.id.userCreatedIllustFragment,
                UserCreatedIllustFragmentArgs(illustAuthorId, ObjectType.ILLUST).toBundle()
            )
        } else if (type == SeeMoreType.IllustSeries) {
            liveIllust.value?.series?.id?.let {
                pushFragment(
                    R.id.navigation_style_fragment,
                    StyleFragmentArgs(it, ObjectType.ILLUST).toBundle()
                )
            }
        }
    }
}

fun TextView.setCaption(caption: String?) {
    text = if (caption?.isNotEmpty() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(caption, 0)
        } else {
            caption
        }
    } else {
        context.getString(R.string.published_a_work)
    }
}