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
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.network.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserHistoryRepository : CustomRepository<UserHistoryFragment>() {

    override suspend fun suspendRefresh(
        fragment: UserHistoryFragment
    ) {
        val all = withContext(Dispatchers.IO) {
            RoomDB.db().historyDao().getAll()
        }
        withContext(Dispatchers.Main) {
            val displayList = all.filter { it.objectType == HistoryType.USER }.onEach { history ->
                val user = Util.gson.fromJson(history.objectJson, User::class.java)
                ObjectPool.update(user)
            }
            holderList.value = displayList.map { HistoryHolder(it) }
            refreshState.value =
                LoadState.LOADED(hasContent = displayList.isNotEmpty(), hasNext = false)
        }
    }
}

class UserHistoryFragment : SlinkyListFragment(), HistoryAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by slinkyListVM(
        ownerProducer = { requireParentFragment() },
        keyProducer = { "ViewHistoryViewModel-User" }) {
        UserHistoryRepository()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun showFakeStatusBar(): Boolean {
        return false
    }

    override fun showHistory(history: ViewHistory) {
        if (history.objectType == HistoryType.USER) {
            val user = Util.gson.fromJson(history.objectJson, User::class.java)
            ObjectPool.update(user)
            onClickUserImpl(user)
        }
    }
}
