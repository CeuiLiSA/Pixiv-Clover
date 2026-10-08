package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Client

class PostListFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel {
        IllustListRepository {
            Client.appApi.followUserIllust(
                "all"
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.follow_post)
        binding.listView.setUpStaggerLayoutManager(requireContext())
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }
}