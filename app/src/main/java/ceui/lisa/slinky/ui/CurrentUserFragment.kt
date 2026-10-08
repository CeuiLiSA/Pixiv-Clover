package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.ActionItem
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.PreferencePool
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemAppVersionBinding
import ceui.lisa.slinky.databinding.ItemBigUserSnapBinding
import ceui.lisa.slinky.databinding.ItemTabBinding
import ceui.lisa.slinky.databinding.ItemTabToggleBinding
import ceui.lisa.slinky.databinding.ShoulderBinding
import ceui.lisa.slinky.databinding.TableSectionHeaderBinding
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.models.UserResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.network.requireUserPrefImpl
import ceui.lisa.slinky.safeCall
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import kotlinx.coroutines.CoroutineScope

class CurrentUserRepository(
    private val senderId: Long
) : CustomRepository<CurrentUserFragment>() {

    override suspend fun suspendRefresh(
        fragment: CurrentUserFragment
    ) {
        with(fragment) {
            val pref = requireUserPrefImpl()
            val liveUser = MutableLiveData<UserResponse>()
            val lastRefreshTime = pref.getLong(CurrentUserFragment.LAST_REFRESH_TIME, 0L)
            val lastResponse = pref.getString(CurrentUserFragment.LAST_USER_RESPONSE, "")
            val hasExistValue = if (lastResponse?.isNotEmpty() == true) {
                try {
                    val storedResp = Util.gson.fromJson(lastResponse, UserResponse::class.java)
                    liveUser.value = storedResp
                    true
                } catch (ex: Exception) {
                    handleError(ex)
                    false
                }
            } else {
                false
            }

            val apiFetch = suspend {
                val userResp = Client.appApi.user(senderId)
                userResp.user?.let {
                    ObjectPool.update(it)
                }
                pref.putLong(CurrentUserFragment.LAST_REFRESH_TIME, System.currentTimeMillis())
                pref.putString(CurrentUserFragment.LAST_USER_RESPONSE, Util.gson.toJson(userResp))
                liveUser.value = userResp
            }

            if (hasExistValue) {
                val r = refreshState.value
                val isForceRefresh =
                    r is LoadState.LOADING && r.refreshHint == RefreshHint.pullToRefresh()
                val isLongTimeNoFetch =
                    ((System.currentTimeMillis() - lastRefreshTime) > CurrentUserFragment.ONE_HOUR)

                if (isForceRefresh || isLongTimeNoFetch) {
                    apiFetch.invoke()
                }
            } else {
                apiFetch.invoke()
            }

            val tagItems = mutableListOf<SlinkyItem>()
            tagItems.add(BigUserSnapHolder(liveUser))
            tagItems.add(TabHolder(getString(R.string.my_bookmark_illust)) {
                pushFragment(
                    R.id.viewPagerFragment,
                    ViewPagerFragmentArgs(ViewPagerContentType.TYPE_MY_BOOKMARK_ILLUST).toBundle()
                )
            })
            tagItems.add(TabHolder(getString(R.string.my_bookmark_novel)) {
                pushFragment(
                    R.id.viewPagerFragment,
                    ViewPagerFragmentArgs(ViewPagerContentType.TYPE_MY_BOOKMARK_NOVEL).toBundle()
                )
            })
            tagItems.add(TabHolder(getString(R.string.ranking_novel)) {
                pushFragment(
                    R.id.viewPagerFragment,
                    ViewPagerFragmentArgs(ViewPagerContentType.TYPE_RANKING_NOVEL).toBundle()
                )
            })
            tagItems.add(TabHolder(getString(R.string.view_history)) {
                pushFragment(
                    R.id.viewPagerFragment,
                    ViewPagerFragmentArgs(ViewPagerContentType.TYPE_VIEW_HISTORY).toBundle()
                )
            })
            tagItems.add(TabHolder(getString(R.string.watchlist)) {
                pushFragment(
                    R.id.viewPagerFragment,
                    ViewPagerFragmentArgs(ViewPagerContentType.TYPE_WATCHLIST).toBundle()
                )
            })
            tagItems.add(TabHolder(getString(R.string.my_works)) {
                pushFragment(
                    R.id.viewPagerFragment,
                    ViewPagerFragmentArgs(ViewPagerContentType.TYPE_CREATED_BY_ME).toBundle()
                )
            })
            tagItems.add(TabHolder(getString(R.string.settings)) { pushFragment(R.id.settingsFragment) })
            tagItems.add(TabHolder(getString(R.string.switch_account)) { pushFragment(R.id.switchAccountFragment) })
            tagItems.add(TabHolder(getString(R.string.log_out)) { performLogout() })
            holderList.value = tagItems
            refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
        }
    }

}

class CurrentUserFragment : SlinkyListFragment(), UserFullAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel({ senderId }) { myselfId ->
        CurrentUserRepository(myselfId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.listView.layoutManager = LinearLayoutManager(requireContext())

        actionbarContent.endItems.value = listOf(
            ActionItem(R.drawable.ic_bell) {
                pushFragment(R.id.navigation_notifications_fragment)
            }
        )
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    companion object {
        const val LAST_REFRESH_TIME = "LAST_REFRESH_TIME"
        const val LAST_USER_RESPONSE = "LAST_USER_RESPONSE"
        const val ONE_HOUR = 3600 * 1000L
    }

    fun performLogout() {
        launchSuspend {
            if (alertYesOrCancel(message = getString(R.string.log_out))) {
                requireUserPrefImpl().clearAll()
                Settings.logOut()
            }
        }
    }

    override fun onClickFollowUserList() {
        pushFragment(
            R.id.viewPagerFragment,
            ViewPagerFragmentArgs(ViewPagerContentType.TYPE_MY_FOLLOWING).toBundle()
        )
    }

    override fun onClickFansList() {
        showUserList(UserListType.FANS, senderId)
    }

    override fun onClickPixivFriendsList() {
        showUserList(UserListType.PIXIV_FRIENDS, senderId)
    }

    override fun onClickUser(user: User) {
        onClickUserImpl(user)
    }
}

class AppVersionHolder(val versionText: String) : SlinkyItem()

@ItemHolder(AppVersionHolder::class)
class AppVersionViewHolder(aa: ItemAppVersionBinding) :
    SlinkyViewHolder<ItemAppVersionBinding, AppVersionHolder>(aa) {

    override fun onBindViewHolder(item: AppVersionHolder) {
        super.onBindViewHolder(item)
        binding.versionText.text = item.versionText
        binding.copyrightText.text = context.getString(R.string.copyright)
    }
}

class BigUserSnapHolder(val userResponse: LiveData<UserResponse>) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return userResponse.value?.user?.id == (other as? BigUserSnapHolder)?.userResponse?.value?.user?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return userResponse.value == (other as? BigUserSnapHolder)?.userResponse?.value
    }
}


@ItemHolder(BigUserSnapHolder::class)
class BigUserSnapViewHolder(aa: ItemBigUserSnapBinding) :
    SlinkyViewHolder<ItemBigUserSnapBinding, BigUserSnapHolder>(aa) {

    override fun onBindViewHolder(item: BigUserSnapHolder) {
        super.onBindViewHolder(item)
        item.userResponse.observe(lifecycleOwner) {
            binding.userResponse = it
        }
        binding.userRoot.setOnClick {
            item.userResponse.value?.user?.let { user ->
                it.findActionReceiverOrNull<UserFullAction>()?.onClickUser(user)
            }
        }
        binding.followUser.setOnClick {
            it.findActionReceiverOrNull<UserFullAction>()?.onClickFollowUserList()
        }
        binding.fansUser.setOnClick {
            it.findActionReceiverOrNull<UserFullAction>()?.onClickFansList()
        }
        binding.pixivFriends.setOnClick {
            it.findActionReceiverOrNull<UserFullAction>()?.onClickPixivFriendsList()
        }
    }
}

class TabHolder(
    val title: String,
    val rightText: LiveData<String>? = null,
    val action: (() -> Unit)? = null
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? TabHolder)?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? TabHolder)?.title &&
                rightText?.value == (other as? TabHolder)?.rightText?.value
    }
}


@ItemHolder(TabHolder::class)
class TabViewHolder(aa: ItemTabBinding) : SlinkyViewHolder<ItemTabBinding, TabHolder>(aa) {

    override fun onBindViewHolder(item: TabHolder) {
        super.onBindViewHolder(item)
        binding.title.text = item.title
        if (item.rightText != null) {
            item.rightText.observe(lifecycleOwner) {
                binding.rightTitle.text = it
            }
            binding.rightTitle.isVisible = true
        } else {
            binding.rightTitle.isVisible = false
        }
        binding.tabRoot.setOnClick {
            item.action?.invoke()
        }
    }
}

class TabToggleHolder(
    val title: String,
    val isInitialChecked: Boolean,
    val action: ((Boolean) -> Unit)? = null
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? TabToggleHolder)?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? TabToggleHolder)?.title &&
                isInitialChecked == (other as? TabToggleHolder)?.isInitialChecked
    }
}


@ItemHolder(TabToggleHolder::class)
class TabToggleViewHolder(aa: ItemTabToggleBinding) :
    SlinkyViewHolder<ItemTabToggleBinding, TabToggleHolder>(aa) {

    override fun onBindViewHolder(item: TabToggleHolder) {
        super.onBindViewHolder(item)
        binding.item = item
        binding.lifecycleOwner = lifecycleOwner
        binding.title.text = item.title
        binding.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            item.action?.invoke(isChecked)
        }
    }
}

class TabSectionHolder(val title: String) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? TabSectionHolder)?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? TabSectionHolder)?.title
    }
}

@ItemHolder(TabSectionHolder::class)
class TabSectionViewHolder(aa: TableSectionHeaderBinding) :
    SlinkyViewHolder<TableSectionHeaderBinding, TabSectionHolder>(aa) {

    override fun onBindViewHolder(item: TabSectionHolder) {
        super.onBindViewHolder(item)
        binding.headerTitle.text = item.title
    }
}