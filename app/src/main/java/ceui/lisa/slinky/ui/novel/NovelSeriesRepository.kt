package ceui.lisa.slinky.ui.novel

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.databinding.CellSeriesItemBinding
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.models.NovelSeries
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.ui.IllustSeriesFragment
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.SeriesDescHolder
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.ui.findFragmentOrNull
import ceui.lisa.slinky.ui.pxValue
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.Glide
import kotlin.math.roundToInt

class NovelSeriesRepository(
    private val seriesId: Long,
    private val seriesDetail: MutableLiveData<NovelSeries> = MutableLiveData()
) : PixivListRepository<Novel, IllustSeriesFragment>(
    loader = {
        val resp = Client.appApi.getNovelSeriesDetail(seriesId)
        seriesDetail.value = resp
        resp
    },
    dataMapper = { illust -> NovelSeriesHolder(illust, 0, 0) }
) {
    override suspend fun applyRefreshData(
        fragment: IllustSeriesFragment,
        displayList: List<Novel>
    ) {
        val holders = mutableListOf<SlinkyItem>()
        seriesDetail.value?.novel_series_detail?.let {
            holders.add(SeriesDescHolder(it))
        }
        val count = seriesDetail.value?.novel_series_detail?.content_count ?: 0
        holders.addAll(
            displayList.mapIndexed { index, illust ->
                NovelSeriesHolder(illust, count - index, count)
            }
        )
        holderList.value = holders
    }

    override suspend fun applyLoadMoreData(
        fragment: IllustSeriesFragment,
        displayList: List<Novel>
    ) {
        val pages = toMutableList()
        val count = seriesDetail.value?.novel_series_detail?.content_count ?: 0
        pages.addAll(
            displayList.mapIndexed { index, illust ->
                NovelSeriesHolder(illust, count - (index + pages.size - 1), count)
            }
        )
        holderList.value = pages
    }
}


class NovelSeriesHolder(val novel: Novel, val index: Int, val count: Int) : SlinkyItem() {

    init {
        ObjectPool.update(novel)
    }

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return novel.id == (other as? NovelSeriesHolder)?.novel?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return novel == (other as? NovelSeriesHolder)?.novel
    }
}

@ItemHolder(NovelSeriesHolder::class)
class NovelSeriesViewHolder(val aa: CellSeriesItemBinding) : SlinkyViewHolder<CellSeriesItemBinding, NovelSeriesHolder>(aa) {

    override fun onBindViewHolder(item: NovelSeriesHolder) {
        super.onBindViewHolder(item)
        (binding.imageView.layoutParams as ViewGroup.MarginLayoutParams).let {
            it.width = 120.pxValue
            it.height = (120.pxValue * 4F / 3F).roundToInt()
            binding.imageView.layoutParams = it
        }
        binding.pSize.isVisible = false
        Glide.with(context).load(item.novel.image_urls?.large?.toGlideUrl()).into(binding.imageView)
        binding.title.text = item.novel.title
        binding.index.text = "#${item.index} / #${item.count}"
        binding.dateTime.text = context.getString(R.string.publish_time, item.novel.displayCreateDate())
        binding.root.setOnClick {
            it.findFragmentOrNull<NavFragment>()?.onClickNovelImpl(item.novel)
        }
    }
}