package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.utils.visibleOrGone

class LatestIllustFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: LatestIllustFragmentArgs by navArgs()
    private val viewModel by listViewModel({ safeArgs.objectType }) { objectType ->
        IllustListRepository(
            loader = { Client.appApi.latestContent(objectType) }
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
}