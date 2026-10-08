package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Client

class RelatedIllustFragment : SlinkyListFragment() {

    private val safeArgs: RelatedIllustFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel({ safeArgs.illustId }) { illustId ->
        IllustListRepository(
            loader = {
                Client.appApi.relatedIllust(illustId)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.related_items)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.setUpStaggerLayoutManager(requireContext())
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }
}