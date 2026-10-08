package ceui.lisa.slinky.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.LandingActivity
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.AddNewCirclePlaceholderBinding
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemSwitchAccountBinding
import ceui.lisa.slinky.models.AccountResponse
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import com.blankj.utilcode.util.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SwitchAccountRepository(private val senderId: Long) :
    CustomRepository<SwitchAccountFragment>() {

    override suspend fun suspendRefresh(
        fragment: SwitchAccountFragment
    ) {
        val users = withContext(Dispatchers.IO) {
            RoomDB.db().historyDao().getHistoryByType(HistoryType.RECENT_USER)
        }
        val holders = mutableListOf<SlinkyItem>()
        holders.addAll(users.map {
            val account = Util.gson.fromJson(it.objectJson, AccountResponse::class.java)
            requireNotNull(account.user)
            ObjectPool.update(account.user)
            SwitchAccountHolder(ObjectPool.get(account.user.id), account)
        })
        holders.add(AddAccountHolder(fragment.getString(R.string.add_account)))
        holderList.value = holders
        refreshState.value = LoadState.LOADED(hasContent = users.isNotEmpty(), hasNext = false)
    }
}

class SwitchAccountFragment : SlinkyListFragment() {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel({ senderId }) { myselfId ->
        SwitchAccountRepository(myselfId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.switch_account)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    fun switchAccount(accountResponse: AccountResponse) {
        if (accountResponse.user?.id == senderId) {
            showPush(title = getString(R.string.its_current_account))
            return
        }

        launchSuspend {
            val message = getString(R.string.switch_account_hint, accountResponse.user?.name)
            if (alertYesOrCancel(message = message)) {
                Settings.updateLoggedInUser(accountResponse)
                AppUtils.relaunchApp()
            }
        }
    }

    fun addAccount() {
        startActivity(Intent(requireContext(), LandingActivity::class.java))
    }
}

class SwitchAccountHolder(val user: LiveData<User>, val accountResponse: AccountResponse) :
    SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return accountResponse.user?.id == (other as? SwitchAccountHolder)?.accountResponse?.user?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return accountResponse == (other as? SwitchAccountHolder)?.accountResponse &&
                user.value == (other as? SwitchAccountHolder)?.user?.value
    }
}


@ItemHolder(SwitchAccountHolder::class)
class SwitchAccountViewHolder(aa: ItemSwitchAccountBinding) :
    SlinkyViewHolder<ItemSwitchAccountBinding, SwitchAccountHolder>(aa) {

    override fun onBindViewHolder(item: SwitchAccountHolder) {
        super.onBindViewHolder(item)
        binding.rootLayout.setOnClick {
            it.findFragmentOrNull<SwitchAccountFragment>()?.switchAccount(item.accountResponse)
        }
        binding.userSnapLayout.followLayout.isVisible = false
        binding.userSnapLayout.user = item.user
        binding.userSnapLayout.lifecycleOwner = lifecycleOwner
    }
}


class AddAccountHolder(val title: String) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? AddAccountHolder)?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? AddAccountHolder)?.title
    }
}

@ItemHolder(AddAccountHolder::class)
class AddAccountViewHolder(aa: AddNewCirclePlaceholderBinding) :
    SlinkyViewHolder<AddNewCirclePlaceholderBinding, AddAccountHolder>(aa) {

    override fun onBindViewHolder(item: AddAccountHolder) {
        super.onBindViewHolder(item)
        binding.textPresent = item.title
        binding.cellAdd.setOnClick {
            it.findFragmentOrNull<SwitchAccountFragment>()?.addAccount()
        }
    }
}