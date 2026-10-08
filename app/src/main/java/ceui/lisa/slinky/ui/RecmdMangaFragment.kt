package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.IllustListRepository
import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.network.Client


class RecmdMangaRepository : IllustListRepository<RecmdMangaFragment>(
    loader = { Client.appApi.getRecommendManga(false) }
)

class RecmdMangaFragment : SlinkyListFragment(R.layout.fragment_slinky_list) {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel {
        RecmdMangaRepository().apply {
            setUpCache(
                PrefResponseCache(IllustResponse::class.java, prefKeyProducer = { "recmd-manga" })
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.recommend_manga)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.setUpStaggerLayoutManager(requireContext())
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }
}