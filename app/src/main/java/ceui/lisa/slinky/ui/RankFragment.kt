package ceui.lisa.slinky.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.core.slinkyListVM
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemIllustBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.styles.LinearItemDecoration
import ceui.lisa.slinky.styles.LinearItemHorizontalDecoration
import ceui.lisa.slinky.styles.SpacesItemDecoration
import ceui.lisa.slinky.utils.visibleOrGone
import timber.log.Timber
import kotlin.math.roundToInt

class RankFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: RankFragmentArgs by navArgs()
    private val viewModel by slinkyListVM(
        keyProducer = { "RankViewModel#" + safeArgs.rankMode },
        ownerProducer = { requireParentFragment() },
    ) {
        IllustListRepository(
            loader = { Client.appApi.rankListIllust(safeArgs.rankMode) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarContainer.visibleOrGone = false
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.setUpStaggerLayoutManager(requireContext())
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }
}

inline fun <reified F : Fragment> View.findFragmentOrNull(): F? {
    return try {
        val targetFragment = findFragment<Fragment>()
        if (targetFragment is F) {
            targetFragment as F
        } else null
    } catch (e: Exception) {
        null
    }
}


inline fun <reified T : Fragment> Fragment.findAncestorOrSelf(): T? {
    if (this is T) {
        return this
    } else {
        return findAncestor()
    }
}

inline fun <reified T : Fragment> Fragment.findAncestor(): T? {
    var itr = this.parentFragment
    while (itr != null) {
        if (itr is T) {
            return itr
        }
        itr = itr.parentFragment
    }
    return null
}

inline fun <reified ActionReceiverT> Fragment.findActionReceiverOrNull(): ActionReceiverT? {
    var itr: Fragment? = this
    while (itr != null) {
        val receiver = itr as? ActionReceiverT
        if (receiver != null) {
            return receiver
        } else {
            itr = itr.parentFragment
        }
    }

    return activity as? ActionReceiverT
}

inline fun <reified ActionReceiverT> View.findActionReceiverOrNull(): ActionReceiverT? {
    val fragment = this.findFragmentOrNull<Fragment>()
    return fragment?.findActionReceiverOrNull<ActionReceiverT>()
}

class IllustItem(val illust: Illust) : SlinkyItem() {

    init {
        ObjectPool.updateIllust(illust)
    }

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illust.id == (other as? IllustItem)?.illust?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illust == (other as? IllustItem)?.illust
    }
}


@ItemHolder(IllustItem::class)
class IllustItemViewHolder(aa: ItemIllustBinding) :
    SlinkyViewHolder<ItemIllustBinding, IllustItem>(aa) {

    override fun onBindViewHolder(item: IllustItem) {
        super.onBindViewHolder(item)

        val illust = item.illust
        val params = binding.imageView.layoutParams
        val itemWidth = (screenWidth - 12.pxValue) / 2
        val ratio = illust.height / illust.width.toFloat()
        Timber.d("宽高比：${ratio}")
        params.width = itemWidth
        if (ratio > 1.8F) {
            // 如果图片很窄，但是高度很高，则限制最大高度，是宽的 1.8 倍
            params.height = (itemWidth * 1.8F).roundToInt()
        } else if (ratio < 0.6F) {
            params.height = (itemWidth * 0.6F).roundToInt()
        } else {
            params.height =
                ((itemWidth * illust.height.toFloat()) / illust.width.toFloat()).roundToInt()
        }
        binding.imageView.layoutParams = params
        binding.root.setOnClick {
            it.findFragment<NavFragment>().onClickIllustImpl(illust)
        }

        binding.root.setOnLongClickListener {
            it.findFragment<NavFragment>().onLongClickIllustImpl(illust)
            true
        }

        val liveDataIllust = ObjectPool.get<Illust>(illust.id)
        binding.item = liveDataIllust
        binding.bookmark.setOnClick {
            val fragment = it.findFragment<NavFragment>()
            fragment.didClickBookmarkIllust(liveDataIllust, binding.bookmark)
        }
        binding.bookmark.setOnLongClickListener {
            val fragment = it.findFragment<NavFragment>()
            fragment.didLongClickBookmarkIllust(liveDataIllust, binding.bookmark)
            true
        }

        val pageCount = illust.page_count
        if (pageCount == 1) {
            binding.pSize.isVisible = false
        } else {
            binding.pSize.isVisible = true
            binding.pSize.text = context.getString(R.string.p_size, pageCount)
        }

        if (illust.isGif()) {
            binding.type.isVisible = true
            binding.type.text = context.getText(R.string.type_gif)
        } else {
            binding.type.isVisible = false
        }

        binding.seriesLabel.isVisible = illust.series?.id.exist()

        binding.createdByAi.isVisible = illust.createdByAI()

        GlideApp.with(context)
            .load(illust.image_urls?.large)
            .into(binding.imageView)

//        binding.disableFrame.isVisible = illust.isDisabled()
    }
}

fun RecyclerView.setUpLinearlayoutManager(context: Context) {
    setUpLinearlayoutManager(context, withTop = true, addDecoration = true)
}

fun RecyclerView.setUpLinearlayoutManagerHorizontal(context: Context) {
    addItemDecoration(
        LinearItemHorizontalDecoration(
            context.dipToPx(8.0f)
        )
    )
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
}

fun RecyclerView.setUpGridlayoutManager(context: Context, spanCount: Int = 4) {
    layoutManager = GridLayoutManager(context, spanCount)
}


fun RecyclerView.setUpLinearlayoutManager(
    context: Context,
    withTop: Boolean,
    addDecoration: Boolean
) {
    if (addDecoration) {
        addItemDecoration(
            LinearItemDecoration(
                context.dipToPx(8.0f),
                withLeftRight = false,
                withTop = withTop
            )
        )
    }
    layoutManager = LinearLayoutManager(context)
}

fun RecyclerView.setUpStaggerLayoutManager(context: Context) {
    addItemDecoration(SpacesItemDecoration(context.dipToPx(4.0f), 2))
    layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
}