package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.utils.visibleOrGone
import com.blankj.utilcode.util.BarUtils

class RecmdIllustRepository : IllustListRepository<RecmdIllustFragment>(
    loader = { Client.appApi.getRecommendIllusts(false) }
) {
    override suspend fun applyRefreshData(
        fragment: RecmdIllustFragment,
        displayList: List<Illust>
    ) {
        val headerHeight = BarUtils.getActionBarHeight() + fragment.dipToPx(12F)
        holderList.value = listOf(
            SpaceHolder(headerHeight),
            SpaceHolder(headerHeight),
        ) + displayList.map(dataMapper)
    }
}

class RecmdIllustFragment : SlinkyListFragment(), ReselectAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel {
        RecmdIllustRepository().apply {
            setUpCache(
                PrefResponseCache(IllustResponse::class.java, prefKeyProducer = { "recmd-illust" })
            )
        }
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

    override fun onReselected(index: Int) {
        binding.listView.smoothScrollToTopIfNeeded()
    }
}

fun RecyclerView.smoothScrollToTopIfNeeded() {
    val isTop = isTop()
    if (!isTop) {
        smoothScrollToPosition(0)
    }
}

fun RecyclerView.isTop(): Boolean {
    return !canScrollVertically(-1)
}