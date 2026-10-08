package ceui.lisa.slinky.ui.novel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.observeEvent
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.ui.SearchViewModel
import ceui.lisa.slinky.ui.slinkyLaunchWhenResumed
import ceui.lisa.slinky.ui.viewBinding
import ceui.lisa.slinky.utils.visibleOrGone

class PopularNovelFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: PopularNovelFragmentArgs by navArgs()
    private val searchViewModel by viewModels<SearchViewModel>(ownerProducer = { requireParentFragment() })
    private val viewModel by listViewModel({ safeArgs.word }, { searchViewModel }) { word, vm ->
        NovelListRepository(
            loader = {
                val keyword = vm.tagList.value?.map { it.name }?.joinToString(separator = " ") ?: ""
                Client.appApi.searchNovel(keyword, SortType.POPULAR)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchViewModel.popularRefreshEvent.observeEvent(viewLifecycleOwner) {
            slinkyLaunchWhenResumed {
                viewModel.refresh(RefreshHint.pullToRefresh(), this@PopularNovelFragment)
            }
        }
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.toolbarContainer.visibleOrGone = false
    }
}