package ceui.lisa.slinky.ui.novel

import androidx.core.view.isVisible
import androidx.fragment.app.findFragment
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.databinding.ItemNovelBinding
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.paging.ListShow
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.NovelAction
import ceui.lisa.slinky.ui.NovelSeriesAction
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.ui.didClickBookmarkNovel
import ceui.lisa.slinky.ui.didLongClickBookmarkNovel
import ceui.lisa.slinky.ui.findActionReceiverOrNull
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.Glide

class NovelListRepository<FragmentT : NavFragment>(
    loader: suspend () -> ListShow<Novel>,
) : PixivListRepository<Novel, FragmentT>(
    loader = loader,
    dataMapper = { NovelHolder(it) }
)

class NovelHolder(val novel: Novel) : SlinkyItem() {


    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return novel.id == (other as? NovelHolder)?.novel?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return novel == (other as? NovelHolder)?.novel
    }
}

@ItemHolder(NovelHolder::class)
class NovelViewHolder(aa: ItemNovelBinding) : SlinkyViewHolder<ItemNovelBinding, NovelHolder>(aa) {

    override fun onBindViewHolder(item: NovelHolder) {
        super.onBindViewHolder(item)

        val liveDataNovel = ObjectPool.get<Novel>(item.novel.id)
        binding.item = liveDataNovel
        binding.bookmark.setOnClick {
            val fragment = it.findFragment<NavFragment>()
            fragment.didClickBookmarkNovel(liveDataNovel, binding.bookmark)
        }
        binding.bookmark.setOnLongClickListener {
            val fragment = it.findFragment<NavFragment>()
            fragment.didLongClickBookmarkNovel(liveDataNovel, binding.bookmark)
            true
        }

        binding.novelTitle.text = item.novel.title
        Glide.with(binding.root.context).load(item.novel.image_urls?.findMaxSizeUrl()?.toGlideUrl())
            .into(binding.cover)
        binding.root.setOnClick {
            it.findActionReceiverOrNull<NovelAction>()?.onClickNovel(item.novel)
        }
        val series = item.novel.series?.title
        if (series?.isNotEmpty() == true) {
            binding.novelSeriesArea.isVisible = true
            binding.novelSeriesArea.setOnClick {
                it.findActionReceiverOrNull<NovelSeriesAction>()?.onClickNovelSeries(item.novel.series.id ?: 0L)
            }
            binding.novelSeries.text = series
            binding.tagSpacing.isVisible = false
        } else {
            binding.novelSeriesArea.isVisible = false
            binding.tagSpacing.isVisible = true
        }

        val srcs = listOf(
            R.drawable.ic_novel_cover_border_1,
            R.drawable.ic_novel_cover_border_2,
            R.drawable.ic_novel_cover_border_3,
        )

        binding.coverBorder.setImageResource(srcs[(item.novel.id % 3).toInt()])

        val tags = item.novel.tags
        if (tags?.isNotEmpty() == true) {
            binding.tagsFlowView.isVisible = true
            binding.tagsFlowView.setTags(tags)

        } else {
            binding.tagsFlowView.isVisible = false
        }
    }
}

