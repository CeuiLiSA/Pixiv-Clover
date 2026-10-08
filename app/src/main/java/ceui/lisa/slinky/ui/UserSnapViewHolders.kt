package ceui.lisa.slinky.ui

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.findFragment
import androidx.lifecycle.LiveData
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.setUpLoadingState
import ceui.lisa.slinky.databinding.ItemEmptyContentBinding
import ceui.lisa.slinky.databinding.ItemUserHeaderBinding
import ceui.lisa.slinky.databinding.ItemUserIllustBinding
import ceui.lisa.slinky.databinding.ItemUserSnapBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.models.Profile
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.utils.toGlideUrl
import ceui.lisa.slinky.utils.visibleOrInvisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class UserSnapHolder(val user: LiveData<User>, val secondTitle: String) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return user.value?.id == (other as? UserSnapHolder)?.user?.value?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return user.value == (other as? UserSnapHolder)?.user?.value &&
                secondTitle == (other as? UserSnapHolder)?.secondTitle
    }
}


@ItemHolder(UserSnapHolder::class)
class UserSnapViewHolder(aa: ItemUserSnapBinding) :
    SlinkyViewHolder<ItemUserSnapBinding, UserSnapHolder>(aa) {

    override fun onBindViewHolder(item: UserSnapHolder) {
        super.onBindViewHolder(item)
        binding.user = item.user
        binding.secondTitle = item.secondTitle
        val followButton = binding.follow
        followButton.setOnClick {
            it.findFragment<NavFragment>().didClickFollowUser(item.user, it)
        }
        followButton.setOnLongClickListener {
            it.findFragment<NavFragment>()
                .didLongClickFollowUser(item.user, followButton)
            true
        }
        binding.unfollow.setOnClick {
            it.findFragment<NavFragment>().didClickFollowUser(item.user, it)
        }

        val onClickUser =
            View.OnClickListener { v ->
                item.user.value?.let {
                    v.findActionReceiverOrNull<UserAction>()?.onClickUser(it)
                }
            }
        binding.userName.setOnClickListener(onClickUser)
        binding.userHead.setOnClickListener(onClickUser)
    }
}

class UserHeaderHolder(val user: LiveData<User>, val userProfile: Profile?) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return user.value?.id == (other as? UserHeaderHolder)?.user?.value?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        val oldUserValue = user.value
        val newUserValue = (other as? UserHeaderHolder)?.user?.value
        val oldProfile = userProfile
        val newProfile = (other as? UserHeaderHolder)?.userProfile
        return oldUserValue == newUserValue && oldProfile == newProfile
    }
}


@ItemHolder(UserHeaderHolder::class)
class UserHeaderViewHolder(aa: ItemUserHeaderBinding) :
    SlinkyViewHolder<ItemUserHeaderBinding, UserHeaderHolder>(aa) {

    override fun onBindViewHolder(item: UserHeaderHolder) {
        super.onBindViewHolder(item)
        binding.user = item.user
        binding.userHead.setOnClick {
            item.user.value?.profile_image_urls?.medium?.let { avatar ->
                it.findActionReceiverOrNull<ShowImageFullScreenAction>()?.showImageFullScreen(avatar)
            }
        }
        binding.followUserCount.text = (item.userProfile?.total_follow_users ?: 0).toString()
        binding.followUser.setOnClick {
            item.user.value?.id?.let { userId ->
                it.findFragment<NavFragment>().showUserList(UserListType.FOLLOWING, userId)
            }
        }
        val pixivFriendsCount = item.userProfile?.total_mypixiv_users ?: 0
//        binding.pixivFriends.isVisible = pixivFriendsCount != 0
        binding.pixivFriendsCount.text = pixivFriendsCount.toString()
        binding.pixivFriends.setOnClick {
            item.user.value?.id?.let { userId ->
                it.findFragment<NavFragment>().showUserList(UserListType.PIXIV_FRIENDS, userId)
            }
        }
        val followButton = binding.follow
        followButton.setOnClick {
            it.findFragment<NavFragment>().didClickFollowUser(item.user, it)
        }
        followButton.setOnLongClickListener {
            it.findFragment<NavFragment>()
                .didLongClickFollowUser(item.user, followButton)
            true
        }
        binding.unfollow.setOnClick {
            it.findFragment<NavFragment>().didClickFollowUser(item.user, it)
        }
    }
}

class UserIllustHolder(
    val illustResponse: LiveData<IllustResponse>,
    val loadState: LiveData<LoadState>,
    val refreshBlock: () -> Unit
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illustResponse.value == (other as? UserIllustHolder)?.illustResponse?.value
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illustResponse.value == (other as? UserIllustHolder)?.illustResponse?.value
    }
}


@ItemHolder(UserIllustHolder::class)
class UserIllustViewHolder(aa: ItemUserIllustBinding) :
    SlinkyViewHolder<ItemUserIllustBinding, UserIllustHolder>(aa) {

    override fun onBindViewHolder(item: UserIllustHolder) {
        super.onBindViewHolder(item)
        binding.includeItemLoading.setUpLoadingState(item.loadState, lifecycleOwner) {
            item.refreshBlock.invoke()
        }
        binding.illustList = item.illustResponse
        item.illustResponse.observe(lifecycleOwner) {
            val illustList = it.illusts
            if (illustList.isNotEmpty()) {
                val radius = context.dipToPx(6F)
                val operation: (Illust?, ImageView) -> Unit = { data, imageView ->
                    if (data != null) {
                        imageView.visibleOrInvisible = true
                        Glide.with(context).load(data.image_urls?.square_medium?.toGlideUrl())
                            .transform(RoundedCorners(radius)).into(imageView)
                        imageView.setOnClick {
                            it.findFragment<NavFragment>().onClickIllustImpl(data)
                        }
                    } else {
                        imageView.visibleOrInvisible = false
                    }
                }
                operation.invoke(illustList.getOrNull(0), binding.imageView1)
                operation.invoke(illustList.getOrNull(1), binding.imageView2)
                operation.invoke(illustList.getOrNull(2), binding.imageView3)
                operation.invoke(illustList.getOrNull(3), binding.imageView4)
                operation.invoke(illustList.getOrNull(4), binding.imageView5)
                operation.invoke(illustList.getOrNull(5), binding.imageView6)

                if (illustList.size <= 3) {
                    binding.secondLine.isVisible = false
                }
            }
        }
    }
}

class EmptyContentHolder : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return other is EmptyContentHolder
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return other is EmptyContentHolder
    }
}


@ItemHolder(EmptyContentHolder::class)
class EmptyContentViewHolder(aa: ItemEmptyContentBinding) :
    SlinkyViewHolder<ItemEmptyContentBinding, EmptyContentHolder>(aa)