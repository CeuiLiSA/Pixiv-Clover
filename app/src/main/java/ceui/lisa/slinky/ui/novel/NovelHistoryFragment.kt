package ceui.lisa.slinky.ui.novel

import android.os.Bundle
import android.view.View
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.core.slinkyListVM
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.db.ViewHistory
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.ui.HistoryAction
import ceui.lisa.slinky.ui.HistoryHolder
import ceui.lisa.slinky.ui.HistoryType
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.UserHistoryFragment
import ceui.lisa.slinky.ui.UserHistoryRepository
import ceui.lisa.slinky.ui.onClickUserImpl
import ceui.lisa.slinky.ui.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NovelHistoryRepository : CustomRepository<NovelHistoryFragment>() {

    override suspend fun suspendRefresh(
        fragment: NovelHistoryFragment
    ) {
        val all = withContext(Dispatchers.IO) {
            RoomDB.db().historyDao().getAll()
        }
        val holders = mutableListOf<SlinkyItem>()
        withContext(Dispatchers.Main) {
            val displayList = all.filter { it.objectType == HistoryType.NOVEL }.onEach { history ->
                val novel = Util.gson.fromJson(history.objectJson, Novel::class.java)
                ObjectPool.update(novel)
                holders.add(NovelHolder(novel))
            }
            holderList.value = holders
            refreshState.value =
                LoadState.LOADED(hasContent = displayList.isNotEmpty(), hasNext = false)
        }
    }
}

class NovelHistoryFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by slinkyListVM(
        ownerProducer = { requireParentFragment() },
        keyProducer = { "ViewHistoryViewModel-Novel" }) {
        NovelHistoryRepository()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun showFakeStatusBar(): Boolean {
        return false
    }
}
