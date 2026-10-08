package ceui.lisa.slinky.ui.watchlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.navArgs
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.CellWatchlistBinding
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.IllustSeriesDetail
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.styles.LinearItemDecoration
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.ui.StyleFragmentArgs
import ceui.lisa.slinky.ui.didClickBookmarkMangaSeries
import ceui.lisa.slinky.ui.didClickFollowUser
import ceui.lisa.slinky.ui.didLongClickFollowUser
import ceui.lisa.slinky.ui.findActionReceiverOrNull
import ceui.lisa.slinky.ui.findFragmentOrNull
import ceui.lisa.slinky.ui.onClickUserImpl
import ceui.lisa.slinky.ui.pxValue
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.showIllust
import ceui.lisa.slinky.ui.viewBinding
import ceui.lisa.slinky.utils.toGlideUrl
import ceui.lisa.slinky.utils.visibleOrGone
import com.bumptech.glide.Glide

class WatchlistFragment : SlinkyListFragment(), SeriesActionReceiver {

    private val safeArgs by navArgs<WatchlistFragmentArgs>()
    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel({ safeArgs.type }) { type ->
        PixivListRepository(
            loader = { Client.appApi.watchlist(type) },
            dataMapper = { WatchlistHolder(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarContainer.visibleOrGone = false
        binding.listView.addItemDecoration(LinearItemDecoration(18.pxValue))
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun onClickSeries(seriesDetail: IllustSeriesDetail) {
        pushFragment(
            R.id.navigation_style_fragment,
            StyleFragmentArgs(
                seriesDetail.id,
                safeArgs.type
            ).toBundle()
        )
    }

    override fun onClickShowLatestContent(seriesDetail: IllustSeriesDetail) {
        if (safeArgs.type == ObjectType.MANGA) {
            seriesDetail.latest_content_id?.let {
                showIllust(it)
            }
        }
    }
}

class WatchlistHolder(val detail: IllustSeriesDetail) : SlinkyItem() {

    init {
        detail.user?.let {
            ObjectPool.update(it)
        }
        ObjectPool.update(detail)
    }

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return detail.id == (other as? WatchlistHolder)?.detail?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return detail == (other as? WatchlistHolder)?.detail
    }
}


@ItemHolder(WatchlistHolder::class)
class WatchlistViewHolder(aa: CellWatchlistBinding) :
    SlinkyViewHolder<CellWatchlistBinding, WatchlistHolder>(aa) {

    override fun onBindViewHolder(item: WatchlistHolder) {
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
        Glide.with(context).load(item.detail.url?.toGlideUrl()).into(binding.imageView)
        binding.count.text = "共 ${item.detail.published_content_count} 话"
        binding.root.setOnClick {
            it.findActionReceiverOrNull<SeriesActionReceiver>()?.onClickSeries(item.detail)
        }
        binding.openButton.setOnClick {
            it.findActionReceiverOrNull<SeriesActionReceiver>()?.onClickShowLatestContent(item.detail)
        }
    }
}

interface SeriesActionReceiver {
    fun onClickSeries(seriesDetail: IllustSeriesDetail)

    fun onClickShowLatestContent(seriesDetail: IllustSeriesDetail)

}