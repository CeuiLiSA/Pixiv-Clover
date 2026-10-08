package ceui.lisa.slinky.ui.novel

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.core.slinkyListVM
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.ui.viewBinding
import ceui.lisa.slinky.utils.visibleOrGone

class RankingNovelFragment : SlinkyListFragment() {


    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: RankingNovelFragmentArgs by navArgs()
    private val viewModel by slinkyListVM(
        keyProducer = { "NovelRankViewModel#" + safeArgs.type },
        ownerProducer = { requireParentFragment() },
    ) {
        NovelListRepository(loader = { Client.appApi.rankListNovel(safeArgs.type) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarContainer.visibleOrGone = false
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }
}