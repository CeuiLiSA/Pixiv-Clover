package ceui.lisa.slinky.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.PrefResponseCache
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.observeEvent
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemUserPreviewBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.models.UserPreview
import ceui.lisa.slinky.models.UserPreviewResponse
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

object UserListType {
    const val FOLLOWING = 1
    const val RECOMMEND = 2
    const val FANS = 3
    const val PIXIV_FRIENDS = 4
    const val SEARCH_USER = 5
    const val FOLLOWING_PRIVATE = 6
    const val RELATED = 7
}

class UserListFragment : SlinkyListFragment(), PostAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: UserListFragmentArgs by navArgs()
    private val searchViewModel by viewModels<SearchViewModel>(ownerProducer = { requireParentFragment() })
    private val viewModel by listViewModel(
        { safeArgs.userId },
        { safeArgs.type },
        { senderId }) { userId, type, myselfId ->
        PixivListRepository(
            loader = {
                when (type) {
                    UserListType.RECOMMEND -> {
                        PrefResponseCache(UserPreviewResponse::class.java, prefKeyProducer = { "recmd-user" }).get() ?: Client.appApi.recommendUser()
                    }

                    UserListType.FOLLOWING -> {
                        Client.appApi.getUserFollowingList(userId, BookmarkType.PUBLIC)
                    }

                    UserListType.FOLLOWING_PRIVATE -> {
                        Client.appApi.getUserFollowingList(myselfId, BookmarkType.PRIVATE)
                    }

                    UserListType.FANS -> {
                        Client.appApi.getUserFansList(userId)
                    }

                    UserListType.SEARCH_USER -> {
                        val keyword = searchViewModel.word.value ?: ""
                        Client.appApi.searchUser(keyword)
                    }

                    UserListType.RELATED -> {
                        Client.appApi.relatedUsers(userId)
                    }

                    else -> {
                        Client.appApi.getUserPixivFriendsList(userId)
                    }
                }
            },
            dataMapper = { UserPreviewHolder(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (safeArgs.type) {
            UserListType.RECOMMEND -> {
                actionbarContent.title.value = getString(R.string.recommend_user)
            }

            UserListType.PIXIV_FRIENDS -> {
                actionbarContent.title.value = getString(R.string.pixiv_friend)
            }

            UserListType.FANS -> {
                actionbarContent.title.value = getString(R.string.my_fans)
            }

            UserListType.FOLLOWING -> {
                actionbarContent.title.value = getString(R.string.follow)
            }

            UserListType.RELATED -> {
                actionbarContent.title.value = getString(R.string.related_user)
            }

            UserListType.SEARCH_USER -> {
                searchViewModel.usersRefreshEvent.observeEvent(viewLifecycleOwner) {
                    slinkyLaunchWhenResumed {
                        viewModel.refresh(RefreshHint.pullToRefresh(), this@UserListFragment)
                    }
                }
            }

            else -> {
            }
        }
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun showFakeStatusBar(): Boolean {
        return parentFragment !is ViewPagerFragment
    }

    override fun onClickUser(user: User) {
        onClickUserImpl(user)
    }
}

class UserPreviewHolder(val preview: UserPreview) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return preview.user?.id == (other as? UserPreviewHolder)?.preview?.user?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return preview == (other as? UserPreviewHolder)?.preview
    }
}


@ItemHolder(UserPreviewHolder::class)
class UserPreviewViewHolder(aa: ItemUserPreviewBinding) :
    SlinkyViewHolder<ItemUserPreviewBinding, UserPreviewHolder>(aa) {

    override fun onBindViewHolder(item: UserPreviewHolder) {
        super.onBindViewHolder(item)
        val userId = item.preview.user?.id ?: 0L
        if (userId != 0L) {
            binding.userSnapLayout.lifecycleOwner = lifecycleOwner
            val liveDataUser = ObjectPool.get<User>(userId)
            binding.item = liveDataUser

            val followButton = binding.userSnapLayout.follow
            followButton.setOnClick {
                it.findFragment<NavFragment>()
                    .didClickFollowUser(liveDataUser, it)
            }
            followButton.setOnLongClickListener {
                it.findFragment<NavFragment>()
                    .didLongClickFollowUser(liveDataUser, followButton)
                true
            }
            binding.userSnapLayout.unfollow.setOnClick {
                it.findFragment<NavFragment>()
                    .didClickFollowUser(liveDataUser, it)
            }

            binding.userSnapLayout.root.updateMargins(Rect(6.pxValue, 8.pxValue, 6.pxValue, 0))

            val onClickUser = View.OnClickListener { v ->
                liveDataUser.value?.let {
                    v.findActionReceiverOrNull<UserAction>()?.onClickUser(it)
                }
            }
            binding.userSnapLayout.userName.setOnClickListener(onClickUser)
            binding.userSnapLayout.userHead.setOnClickListener(onClickUser)
        }

        if ((item.preview.illusts?.size ?: 0) == 3) {
            binding.userIllustFrame.isVisible = true
            binding.extraPlaceHolder.isVisible = false
            val illusts = requireNotNull(item.preview.illusts)
            val operation: (Illust, ImageView) -> Unit = { data, imageView ->
                Glide.with(context).load(data.image_urls?.square_medium?.toGlideUrl()).into(imageView)
                imageView.setOnClick {
                    it.findFragment<NavFragment>().onClickIllustImpl(data)
                }
            }

            operation.invoke(illusts[0], binding.imageView4)
            operation.invoke(illusts[1], binding.imageView5)
            operation.invoke(illusts[2], binding.imageView6)
        } else {
            binding.userIllustFrame.isVisible = false
            binding.extraPlaceHolder.isVisible = true
        }
    }
}
