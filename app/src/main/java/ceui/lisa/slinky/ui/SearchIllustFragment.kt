package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.observeEvent
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.ui.novel.SortType
import ceui.lisa.slinky.utils.visibleOrGone

class SearchIllustFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: SearchIllustFragmentArgs by navArgs()
    private val searchViewModel by viewModels<SearchViewModel>(ownerProducer = { requireParentFragment() })
    private val viewModel by listViewModel(
        { safeArgs.index },
        { searchViewModel }) { index, vm ->
        IllustListRepository(
            loader = {
                val keyword = vm.tagList.value?.map { it.name }?.joinToString(separator = " ") ?: ""
                val sortType = if (index == 0) {
                    vm.regularSortType.value ?: SortType.DATE_DESC
                } else {
                    vm.popularSortType.value ?: SortType.POPULAR
                }
                Client.appApi.searchIllust(keyword, sortType)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel.normalRefreshEvent.observeEvent(viewLifecycleOwner) {
            slinkyLaunchWhenResumed {
                viewModel.refresh(RefreshHint.pullToRefresh(), this@SearchIllustFragment)
            }
        }
        binding.toolbarContainer.visibleOrGone = false
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.setUpStaggerLayoutManager(requireContext())
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }
}
