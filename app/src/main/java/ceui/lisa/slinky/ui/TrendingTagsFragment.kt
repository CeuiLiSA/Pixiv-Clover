package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemTrendingTagBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.models.TrendingTag
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.utils.visibleOrGone
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrendingTagsFragment : SlinkyListFragment(), TrendingTagAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: TrendingTagsFragmentArgs by navArgs()
    private val viewModel by listViewModel({ safeArgs.type }) { type ->
        PixivListRepository(
            loader = { Client.appApi.trendingTags(type) },
            dataMapper = { TrendingTagHolder(it) }
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

    override fun onLongClickTrendingTag(trendingTag: TrendingTag) {
        onLongClickTrendingTagImpl(trendingTag)
    }

    override fun searchTag(name: String) {
        searchTagImpl(name, false, safeArgs.type)
    }
}

class TrendingTagHolder(val trendingTag: TrendingTag) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return trendingTag.tag == (other as? TrendingTagHolder)?.trendingTag?.tag &&
                trendingTag.illust?.id == (other as? TrendingTagHolder)?.trendingTag?.illust?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return trendingTag.illust == (other as? TrendingTagHolder)?.trendingTag?.illust &&
                trendingTag.tag == (other as? TrendingTagHolder)?.trendingTag?.tag
    }
}


@ItemHolder(TrendingTagHolder::class)
class TrendingTagViewHolder(aa: ItemTrendingTagBinding) :
    SlinkyViewHolder<ItemTrendingTagBinding, TrendingTagHolder>(aa) {

    override fun onBindViewHolder(item: TrendingTagHolder) {
        super.onBindViewHolder(item)

        val illust = item.trendingTag.illust ?: return

        val params = binding.imageView.layoutParams
        val itemWidth = (screenWidth - 12.pxValue) / 2
        params.width = itemWidth
        params.height = (itemWidth * illust.height) / illust.width
        binding.imageView.layoutParams = params

        binding.imageView.setOnClick {
            item.trendingTag.tag?.let { name ->
                binding.imageView.findActionReceiverOrNull<TrendingTagAction>()?.searchTag(name)
            }
        }
        binding.imageView.setOnLongClickListener {
            binding.imageView.findActionReceiverOrNull<TrendingTagAction>()
                ?.onLongClickTrendingTag(item.trendingTag)
            true
        }
        val translatedName = item.trendingTag.translated_name
        if (translatedName?.isNotEmpty() == true) {
            binding.translatedName.isVisible = true
            binding.translatedName.text = "[${translatedName}]"
        } else {
            binding.translatedName.isVisible = false
        }

        val radius = context.dipToPx(6F)
        GlideApp.with(context)
            .load(illust.image_urls?.large)
            .transform(RoundedCorners(radius))
            .into(binding.imageView)
        binding.tagName.text = item.trendingTag.tag
    }
}