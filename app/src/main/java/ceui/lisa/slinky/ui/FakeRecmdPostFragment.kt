package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.UserPreview
import ceui.lisa.slinky.models.UserPreviewResponse
import ceui.lisa.slinky.models.UserResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.styles.LinearItemDecoration
import ceui.lisa.slinky.utils.visibleOrGone
import com.blankj.utilcode.util.BarUtils

class FakeRecmdPostRepository : PixivListRepository<UserPreview, FakeRecmdPostFragment>(
    loader = { Client.appApi.recommendUser() },
    dataMapper = { preview -> UserPreviewHolder(preview) }
) {

    private val flatMapper: (UserPreview) -> List<SlinkyItem> = { input ->
        input.illusts?.map { PostItem(it) } ?: listOf()
    }

    override suspend fun applyRefreshData(
        fragment: FakeRecmdPostFragment,
        displayList: List<UserPreview>
    ) {
        holderList.value = listOf(
            SpaceHolder(height = BarUtils.getActionBarHeight() + 16.pxValue),
            RedSectionHeaderHolder(
                fragment.getString(R.string.post),
                seeMoreString = fragment.getString(R.string.more),
                type = SeeMoreType.FOLLOWING_POST
            ),
        ) + displayList.flatMap(flatMapper)
    }

    override suspend fun applyLoadMoreData(
        fragment: FakeRecmdPostFragment,
        displayList: List<UserPreview>
    ) {
        val pages = toMutableList()
        pages.addAll(displayList.flatMap(flatMapper))
        holderList.value = pages
    }
}

class FakeRecmdPostFragment : SlinkyListFragment() {

    private val viewModel by listViewModel { FakeRecmdPostRepository().apply {
        setUpCache(PrefResponseCache(UserPreviewResponse::class.java, prefKeyProducer = { "recmd-user" }))
    } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSlinkyListBinding.bind(view)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.toolbarContainer.visibleOrGone = false
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
    }
}