package ceui.lisa.slinky.ui.novel

import android.os.Bundle
import android.view.View
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.NovelResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.ui.viewBinding
import ceui.lisa.slinky.utils.visibleOrGone

class RecmdNovelFragment : SlinkyListFragment(R.layout.fragment_slinky_list) {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel {
        NovelListRepository<RecmdNovelFragment> { Client.appApi.getRecommendNovels(false) }.apply {
            setUpCache(PrefResponseCache(NovelResponse::class.java, prefKeyProducer = { "recmd-novel" }))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarContainer.visibleOrGone = false
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }
}