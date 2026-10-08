package ceui.lisa.slinky.ui

import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.fragment.app.findFragment
import androidx.lifecycle.MutableLiveData
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.databinding.CellSeriesDetailBinding
import ceui.lisa.slinky.databinding.CellSeriesItemBinding
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.IllustSeries
import ceui.lisa.slinky.models.IllustSeriesDetail
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.Glide
import kotlin.math.roundToInt

class IllustSeriesRepository(
    private val seriesId: Long,
    private val seriesDetail: MutableLiveData<IllustSeries> = MutableLiveData()
) : PixivListRepository<Illust, IllustSeriesFragment>(
    loader = {
        val resp = Client.appApi.getIllustSeriesDetail(seriesId)
        seriesDetail.value = resp
        resp
    },
    dataMapper = { illust -> SeriesHolder(illust, 0, 0) }
) {
    override suspend fun applyRefreshData(
        fragment: IllustSeriesFragment,
        displayList: List<Illust>
    ) {
        val holders = mutableListOf<SlinkyItem>()
        seriesDetail.value?.illust_series_detail?.let {
            holders.add(SeriesDescHolder(it))
        }
        val count = seriesDetail.value?.illust_series_detail?.series_work_count ?: 0
        holders.addAll(
            displayList.mapIndexed { index, illust ->
                SeriesHolder(illust, count - index, count)
            }
        )
        holderList.value = holders
    }

    override suspend fun applyLoadMoreData(
        fragment: IllustSeriesFragment,
        displayList: List<Illust>
    ) {
        val pages = toMutableList()
        val count = seriesDetail.value?.illust_series_detail?.series_work_count ?: 0
        pages.addAll(
            displayList.mapIndexed { index, illust ->
                SeriesHolder(illust, count - (index + pages.size - 1), count)
            }
        )
        holderList.value = pages
    }
}

class SeriesHolder(val illust: Illust, val index: Int, val count: Int) : SlinkyItem() {

    init {
        ObjectPool.updateIllust(illust)
    }

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illust.id == (other as? SeriesHolder)?.illust?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illust == (other as? SeriesHolder)?.illust
    }
}

@ItemHolder(SeriesHolder::class)
class SeriesViewHolder(val aa: CellSeriesItemBinding) : SlinkyViewHolder<CellSeriesItemBinding, SeriesHolder>(aa) {

    override fun onBindViewHolder(item: SeriesHolder) {
        super.onBindViewHolder(item)
        (binding.imageView.layoutParams as MarginLayoutParams).let {
            it.width = 120.pxValue
            it.height = (120.pxValue * item.illust.height / item.illust.width.toFloat()).roundToInt()
            binding.imageView.layoutParams = it
        }
        val pageCount = item.illust.page_count
        if (pageCount == 1) {
            binding.pSize.isVisible = false
        } else {
            binding.pSize.isVisible = true
            binding.pSize.text = context.getString(R.string.p_size, pageCount)
        }
        Glide.with(context).load(item.illust.image_urls?.large?.toGlideUrl()).into(binding.imageView)
        binding.title.text = item.illust.title
        binding.index.text = "#${item.index} / #${item.count}"
        binding.dateTime.text = context.getString(R.string.publish_time, item.illust.displayCreateDate())
        binding.root.setOnClick {
            it.findFragmentOrNull<NavFragment>()?.onClickIllustImpl(item.illust)
        }
    }
}

class SeriesDescHolder(val detail: IllustSeriesDetail) : SlinkyItem() {

    init {
        detail.user?.let {
            ObjectPool.update(it)
        }
        ObjectPool.update(detail)
    }

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return detail.id == (other as? SeriesDescHolder)?.detail?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return detail == (other as? SeriesDescHolder)?.detail
    }
}

@ItemHolder(SeriesDescHolder::class)
class SeriesDescViewHolder(val aa: CellSeriesDetailBinding) : SlinkyViewHolder<CellSeriesDetailBinding, SeriesDescHolder>(aa) {

    override fun onBindViewHolder(item: SeriesDescHolder) {
        super.onBindViewHolder(item)
        val liveSeriesDetail = ObjectPool.get<IllustSeriesDetail>(item.detail.id)
        binding.detail = liveSeriesDetail
        binding.userSnapLayout.lifecycleOwner = lifecycleOwner
        binding.bookmark.setOnClick {
            it.findFragment<NavFragment>().didClickBookmarkMangaSeries(liveSeriesDetail, binding.bookmark)
        }
        binding.userSnapLayout.userHead.setOnClick {
            item.detail.user?.let { user ->
                it.findFragmentOrNull<NavFragment>()?.onClickUserImpl(user)
            }
        }
        binding.userSnapLayout.userName.setOnClick {
            item.detail.user?.let { user ->
                it.findFragmentOrNull<NavFragment>()?.onClickUserImpl(user)
            }
        }
        val followButton = binding.userSnapLayout.follow
        binding.userSnapLayout.lifecycleOwner = lifecycleOwner
        item.detail.user?.let { user ->
            val liveUser = ObjectPool.get<User>(user.id)
            binding.userSnapLayout.user = liveUser
            followButton.setOnClick {
                it.findFragment<NavFragment>().didClickFollowUser(liveUser, it)
            }
            followButton.setOnLongClickListener {
                it.findFragment<NavFragment>().didLongClickFollowUser(liveUser, followButton)
                true
            }
            binding.userSnapLayout.unfollow.setOnClick {
                it.findFragment<NavFragment>()
                    .didClickFollowUser(liveUser, it)
            }
        }
    }
}