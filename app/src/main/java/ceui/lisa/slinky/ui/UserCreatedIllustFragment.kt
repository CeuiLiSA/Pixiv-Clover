package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.network.Client

class UserCreatedIllustFragment : SlinkyListFragment() {

    private val safeArgs: UserCreatedIllustFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel(
        { safeArgs.userId },
        { safeArgs.type }) { userId, type ->
        IllustListRepository(
            loader = { Client.appApi.userCreatedIllust(userId, type) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (safeArgs.type == ObjectType.ILLUST) {
            actionbarContent.title.value = getString(R.string.illust_item)
        } else if (safeArgs.type == ObjectType.MANGA) {
            actionbarContent.title.value = getString(R.string.manga_item)
        }
        binding.toolbarContainer.isVisible = parentFragment !is ViewPagerContainer
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.setUpStaggerLayoutManager(requireContext())
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }
}