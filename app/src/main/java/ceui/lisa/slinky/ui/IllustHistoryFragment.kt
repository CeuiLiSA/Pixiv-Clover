package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.core.slinkyListVM
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.db.ViewHistory
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.network.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IllustHistoryRepository : CustomRepository<IllustHistoryFragment>() {

    override suspend fun suspendRefresh(
        fragment: IllustHistoryFragment
    ) {
        val all = withContext(Dispatchers.IO) {
            RoomDB.db().historyDao().getAll()
        }
        withContext(Dispatchers.Main) {
            val displayList = mutableListOf<IllustItem>()
            all.filter { it.objectType == HistoryType.ILLUST }.onEach { history ->
                val illust = Util.gson.fromJson(history.objectJson, Illust::class.java)
                ObjectPool.updateIllust(illust)
                displayList.add(IllustItem(illust))
            }
            holderList.value = displayList
            refreshState.value =
                LoadState.LOADED(hasContent = displayList.isNotEmpty(), hasNext = false)
        }
    }
}

class IllustHistoryFragment : SlinkyListFragment(), HistoryAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by slinkyListVM(
        ownerProducer = { requireParentFragment() },
        keyProducer = { "ViewHistoryViewModel-Illust" }) {
        IllustHistoryRepository()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listView.setUpStaggerLayoutManager(requireContext())
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    override fun showFakeStatusBar(): Boolean {
        return false
    }

    override fun showHistory(history: ViewHistory) {
        if (history.objectType == HistoryType.ILLUST) {
            val illust = ObjectPool.get<Illust>(history.objectId).value ?: return
            onClickIllustImpl(illust)
        }
    }
}
