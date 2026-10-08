package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.core.SLAdapter
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.core.waitForValue
import ceui.lisa.slinky.databinding.CellRecmdByTagsBinding
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.SquareResponse
import ceui.lisa.slinky.models.Tag
import ceui.lisa.slinky.models.WebIllust
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SquareRepository : CustomRepository<SquareFragment>() {

    private val dataset by lazy {
        ValueContent(coroutineScope) {
            Client.webApi.getSquareContents()
        }.apply {
            setUpCache(PrefResponseCache(SquareResponse::class.java, prefKeyProducer = { "app-square" }))
        }
    }

    override suspend fun suspendRefresh(fragment: SquareFragment) {
        super.suspendRefresh(fragment)
        dataset.refresh()
        val data = dataset.result.waitForValue(fragment.viewLifecycleOwner)
        val holders = mutableListOf<SlinkyItem>()
        withContext(Dispatchers.IO) {
            data.body?.page?.recommendByTag?.forEach { tag ->
                val webIllusts = mutableListOf<WebIllust>()
                tag.ids?.forEach { id ->
                    data.body.thumbnails?.illust?.firstOrNull { it.id == id }?.let { webIllust ->
                        webIllusts.add(webIllust)
                    }
                }
                holders.add(RedSectionHeaderHolder(tag.tag ?: ""))
                holders.addAll(webIllusts.map { IllustSquareHolder(it) })
            }

            data.body?.page?.trendingTags?.forEach { tag ->
                val webIllusts = mutableListOf<WebIllust>()
                tag.ids?.forEach { id ->
                    data.body.thumbnails?.illust?.firstOrNull { it.id == id }?.let { webIllust ->
                        webIllusts.add(webIllust)
                    }
                }
                holders.add(RedSectionHeaderHolder(tag.tag ?: ""))
                holders.addAll(webIllusts.map { IllustSquareHolder(it) })
            }
        }
        holderList.value = holders
        refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
    }

}

class SquareFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel {
        SquareRepository()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.layoutManager = GridLayoutManager(context, 3).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (binding.listView.adapter?.getItemViewType(position) == RedSectionHeaderHolder::class.java.hashCode()) {
                        3
                    } else {
                        1
                    }
                }
            }
        }
    }
}