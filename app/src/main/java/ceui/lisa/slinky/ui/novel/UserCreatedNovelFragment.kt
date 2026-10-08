package ceui.lisa.slinky.ui.novel

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.ui.ViewPagerContainer
import ceui.lisa.slinky.ui.setUpStaggerLayoutManager
import ceui.lisa.slinky.ui.viewBinding

class UserCreatedNovelFragment : SlinkyListFragment() {

    private val safeArgs: UserCreatedNovelFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel({ safeArgs.userId }) { userId ->
        NovelListRepository(loader = { Client.appApi.userCreatedNovel(userId) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.novel_works)
        binding.toolbarContainer.isVisible = parentFragment !is ViewPagerContainer
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }
}