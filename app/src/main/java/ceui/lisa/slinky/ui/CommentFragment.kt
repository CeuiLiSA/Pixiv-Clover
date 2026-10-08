package ceui.lisa.slinky.ui

import androidx.core.view.isVisible
import androidx.fragment.app.findFragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CMFragment
import ceui.lisa.slinky.core.SLAdapter
import ceui.lisa.slinky.databinding.ItemCommentBinding
import ceui.lisa.slinky.databinding.ItemCommentMiniBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.models.Comment
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.utils.toGlideUrl
import ceui.lisa.slinky.utils.visibleOrGone
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class CommentHolder(
    val authorId: Long,
    val comment: Comment,
    val user: LiveData<User>,
    val childComments: List<Comment> = listOf()
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return comment.id == (other as? CommentHolder)?.comment?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        val oldUser = user.value
        val newUser = (other as? CommentHolder)?.user?.value
        return comment == (other as? CommentHolder)?.comment && oldUser == newUser && childComments == (other as? CommentHolder)?.childComments
    }
}


@ItemHolder(CommentHolder::class)
class CommentViewHolder(aa: ItemCommentBinding) :
    SlinkyViewHolder<ItemCommentBinding, CommentHolder>(aa) {

    override fun onBindViewHolder(item: CommentHolder) {
        super.onBindViewHolder(item)
        binding.holder = item
        binding.userSnapLayout.lifecycleOwner = lifecycleOwner
        binding.userSnapLayout.authorId = item.authorId
        binding.userSnapLayout.userHead.setOnClick {
            item.user.value?.let { user ->
                it.findActionReceiverOrNull<UserAction>()?.onClickUser(user)
            }
        }
        binding.userSnapLayout.userName.setOnClick {
            item.user.value?.let { user ->
                it.findActionReceiverOrNull<UserAction>()?.onClickUser(user)
            }
        }
        val followButton = binding.userSnapLayout.follow
        followButton.setOnClick {
            it.findFragment<NavFragment>()
                .didClickFollowUser(item.user, it)
        }
        followButton.setOnLongClickListener {
            it.findFragment<NavFragment>()
                .didLongClickFollowUser(item.user, followButton)
            true
        }
        binding.userSnapLayout.unfollow.setOnClick {
            it.findFragment<NavFragment>()
                .didClickFollowUser(item.user, it)
        }
        binding.userSnapLayout.user = item.user
        if (item.comment.comment?.isNotEmpty() == true) {
            binding.commentText.visibleOrGone = true
            binding.commentText.text = item.comment.comment
            binding.commentText.setOnLongClickListener {
                it.findFragmentOrNull<NavFragment>()?.let { frag ->
                    with(frag) {
                        TextUtil.copyToPB(frag.requireContext(), item.comment.comment)
                        showPush(title = getString(R.string.copied))
                    }
                }
                true
            }
        } else {
            binding.commentText.visibleOrGone = false
        }
        binding.date.text = item.comment.displayCommentDate()

        if (item.comment.stamp != null) {
            val radius = context.dipToPx(6F)
            binding.commentImage.visibleOrGone = true
            Glide.with(context)
                .load(item.comment.stamp.stamp_url?.toGlideUrl())
                .transform(RoundedCorners(radius))
                .into(binding.commentImage)
        } else {
            binding.commentImage.visibleOrGone = false
        }

        binding.showReplay.setOnClick {
            it.findFragmentOrNull<CMFragment>()?.showReply(it, item.comment)
        }
        if (item.childComments.isNotEmpty()) {
            val adapter = SLAdapter(lifecycleOwner)
            binding.childCommentList.adapter = adapter
            binding.childCommentList.layoutManager = LinearLayoutManager(context)
            adapter.submitList(
                item.childComments.map {
                    CommentMiniHolder(item.authorId, it, ObjectPool.get(it.user.id))
                }
            ) {
                binding.childCommentList.isVisible = true
            }
        } else {
            binding.childCommentList.isVisible = false
        }
        binding.replyButton.setOnClick {
            it.findFragmentOrNull<CMFragment>()?.replayToComment(item.comment)
        }
    }
}


class CommentMiniHolder(
    val authorId: Long,
    val comment: Comment,
    val user: LiveData<User>
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return comment.id == (other as? CommentMiniHolder)?.comment?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        val oldUser = user.value
        val newUser = (other as? CommentMiniHolder)?.user?.value
        return comment == (other as? CommentMiniHolder)?.comment && oldUser == newUser
    }
}


@ItemHolder(CommentMiniHolder::class)
class CommentMiniViewHolder(aa: ItemCommentMiniBinding) :
    SlinkyViewHolder<ItemCommentMiniBinding, CommentMiniHolder>(aa) {

    override fun onBindViewHolder(item: CommentMiniHolder) {
        super.onBindViewHolder(item)
        binding.holder = item
        binding.userSnapLayout.lifecycleOwner = lifecycleOwner
        binding.userSnapLayout.authorId = item.authorId
        binding.userSnapLayout.userHead.setOnClick {
            item.user.value?.let { user ->
                it.findActionReceiverOrNull<UserAction>()?.onClickUser(user)
            }
        }
        binding.userSnapLayout.userName.setOnClick {
            item.user.value?.let { user ->
                it.findActionReceiverOrNull<UserAction>()?.onClickUser(user)
            }
        }
        val followButton = binding.userSnapLayout.follow
        followButton.setOnClick {
            it.findFragment<NavFragment>()
                .didClickFollowUser(item.user, it)
        }
        followButton.setOnLongClickListener {
            it.findFragment<NavFragment>()
                .didLongClickFollowUser(item.user, followButton)
            true
        }
        binding.userSnapLayout.unfollow.setOnClick {
            it.findFragment<NavFragment>()
                .didClickFollowUser(item.user, it)
        }
        binding.userSnapLayout.user = item.user
        if (item.comment.comment?.isNotEmpty() == true) {
            binding.commentText.visibleOrGone = true
            binding.commentText.text = item.comment.comment
            binding.commentText.setOnLongClickListener {
                it.findFragmentOrNull<NavFragment>()?.let { frag ->
                    with(frag) {
                        TextUtil.copyToPB(frag.requireContext(), item.comment.comment)
                        showPush(title = getString(R.string.copied))
                    }
                }
                true
            }
        } else {
            binding.commentText.visibleOrGone = false
        }
        binding.date.text = item.comment.displayCommentDate()

        if (item.comment.stamp != null) {
            val radius = context.dipToPx(6F)
            binding.commentImage.visibleOrGone = true
            Glide.with(context)
                .load(item.comment.stamp.stamp_url?.toGlideUrl())
                .transform(RoundedCorners(radius))
                .into(binding.commentImage)
        } else {
            binding.commentImage.visibleOrGone = false
        }
    }
}