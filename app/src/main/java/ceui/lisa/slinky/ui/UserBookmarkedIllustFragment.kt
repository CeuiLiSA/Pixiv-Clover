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
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.utils.visibleOrGone

class UserBookmarkedIllustFragment :
    SlinkyListFragment(R.layout.fragment_slinky_list) {

    private val safeArgs: UserBookmarkedIllustFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel(
        { safeArgs.userId },
        { safeArgs.type }) { userId, type ->
        IllustListRepository(
            loader = { Client.appApi.userBookmarkedIllust(userId, type) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarContainer.visibleOrGone = safeArgs.showToolbar

        ObjectPool.get<User>(safeArgs.userId).observe(viewLifecycleOwner) {
            actionbarContent.title.value = getString(R.string.user_bookmarked_items, it.name)
        }

        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.setUpStaggerLayoutManager(requireContext())
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }
}