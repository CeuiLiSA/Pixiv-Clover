package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.observeEvent
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.utils.visibleOrGone

class PopularIllustFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val searchViewModel by viewModels<SearchViewModel>(ownerProducer = { requireParentFragment() })
    private val viewModel by listViewModel({ searchViewModel }) { vm ->
        IllustListRepository(
            loader = {
                val keyword = vm.word.value ?: ""
                Client.appApi.popularIllust(keyword)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchViewModel.popularRefreshEvent.observeEvent(viewLifecycleOwner) {
            slinkyLaunchWhenResumed {
                viewModel.refresh(RefreshHint.pullToRefresh(), this@PopularIllustFragment)
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