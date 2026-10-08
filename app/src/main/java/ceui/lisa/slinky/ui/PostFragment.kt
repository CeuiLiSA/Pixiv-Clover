package ceui.lisa.slinky.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemPostBinding
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.utils.toGlideUrl
import ceui.lisa.slinky.utils.visibleOrGone
import com.blankj.utilcode.util.BarUtils
import com.bumptech.glide.Glide
import com.kproduce.roundcorners.RoundImageView
import timber.log.Timber
import kotlin.math.roundToInt

class PostRepository : PixivListRepository<Illust, PostFragment>(
    loader = { Client.appApi.followUserIllust("all") },
    dataMapper = { PostItem(it) }
) {
    override suspend fun applyRefreshData(
        fragment: PostFragment,
        displayList: List<Illust>
    ) {
        holderList.value = listOf(
            SpaceHolder(height = BarUtils.getActionBarHeight() + 16.pxValue),
            RedSectionHeaderHolder(
                fragment.getString(R.string.post),
                seeMoreString = fragment.getString(R.string.more),
                type = SeeMoreType.FOLLOWING_POST
            ),
        ) + displayList.map(dataMapper)
    }
}

class PostFragment : SlinkyListFragment(), PostAction,
    SeeMoreAction, ReselectAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel { PostRepository() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        binding.toolbarContainer.visibleOrGone = false
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onClickUser(user: User) {

    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    override fun seeMore(type: Int) {
        if (type == SeeMoreType.FOLLOWING_POST) {
            pushFragment(R.id.postListFragment)
        }
    }

    override fun onReselected(index: Int) {
        binding.listView.smoothScrollToTopIfNeeded()
    }
}

class PostItem(
    val illust: Illust,
    val showUserLayout: Boolean = true
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return illust.id == (other as? PostItem)?.illust?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return illust == (other as? PostItem)?.illust && showUserLayout == other.showUserLayout
    }
}


@ItemHolder(PostItem::class)
class PostHolder(aa: ItemPostBinding) : SlinkyViewHolder<ItemPostBinding, PostItem>(aa) {

    override fun onBindViewHolder(item: PostItem) {
        super.onBindViewHolder(item)
        val illust = item.illust
        val userId = illust.user?.id ?: 0L
        if (userId != 0L) {
            binding.userSnapLayout.root.visibleOrGone = item.showUserLayout
            binding.userSnapLayout.lifecycleOwner = lifecycleOwner
            binding.dateTime.isVisible = !item.showUserLayout
            binding.dateTime.text = context.getString(R.string.publish_time, item.illust.displayCreateDate())
            val liveDataUser = ObjectPool.get<User>(userId)
            binding.user = liveDataUser
            binding.secondTitle = item.illust.displayCreateDate()

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

            val onClickUser = View.OnClickListener { v ->
                liveDataUser.value?.let {
                    v.findFragment<NavFragment>().onClickUserImpl(it)
                }
            }
            binding.userSnapLayout.userName.setOnClickListener(onClickUser)
            binding.userSnapLayout.userHead.setOnClickListener(onClickUser)
        } else {
            binding.dateTime.isVisible = false
        }

        if (illust.caption?.isNotEmpty() == true) {
            binding.content.isVisible = true
            binding.content.movementMethod = LinkMovementMethod.getInstance()
            val lineCount = binding.content.lineCount
            binding.seeAllCaption.isVisible = lineCount > 4
            binding.seeAllCaption.setOnClick {
                it.findFragmentOrNull<NavFragment>()?.pushFragment(
                    R.id.navigation_caption_fragment,
                    CaptionFragmentArgs(illust.id).toBundle()
                )
            }
            binding.content.setCaption(illust.caption)
            binding.content.setOnClick {
                it.findFragmentOrNull<NavFragment>()?.pushFragment(
                    R.id.navigation_caption_fragment,
                    CaptionFragmentArgs(illust.id).toBundle()
                )
            }
        } else {
            binding.seeAllCaption.isVisible = false
            binding.content.isVisible = false
        }

        binding.title.text = illust.title ?: "Untitled"

        if (item.illust.page_count == 1) {
            if (item.illust.width < item.illust.height) {
                val w = (screenWidth * 0.7f).roundToInt()
                val h = (w * item.illust.height / item.illust.width.toFloat()).roundToInt()
                binding.singleMedia.updateLayoutParams {
                    width = w
                    height = h
                }
            } else {
                val w = screenWidth - 36.pxValue
                val h = (w * item.illust.height / item.illust.width.toFloat()).roundToInt()
                binding.singleMedia.updateLayoutParams {
                    width = w
                    height = h
                }
            }

            Glide.with(context)
                .load(item.illust.image_urls?.findMaxSizeUrl()?.toGlideUrl())
                .into(binding.singleMedia)

            binding.singleMedia.setOnClick {
                it.findFragmentOrNull<NavFragment>()?.onClickIllustImpl(item.illust)
            }
        } else if (item.illust.page_count == 2) {
            Glide.with(context).load(item.illust.meta_pages?.get(0)?.image_urls?.large?.toGlideUrl()).into(binding.doubleMedia1)
            Glide.with(context).load(item.illust.meta_pages?.get(1)?.image_urls?.large?.toGlideUrl()).into(binding.doubleMedia2)


            binding.doubleMedia1.setOnClick {
                it.findFragmentOrNull<NavFragment>()?.onClickIllustImpl(item.illust)
            }
            binding.doubleMedia2.setOnClick {
                it.findFragmentOrNull<NavFragment>()?.onClickIllustImpl(item.illust)
            }
        } else {
            val imageViews = listOf(
                binding.multiMedia11,
                binding.multiMedia12,
                binding.multiMedia13,
                binding.multiMedia21,
                binding.multiMedia22,
                binding.multiMedia23,
                binding.multiMedia31,
                binding.multiMedia32,
                binding.multiMedia33,
            )
            val func : (RoundImageView, Int) -> Unit = { iv, index ->
                val url = item.illust.meta_pages?.getOrNull(index)?.image_urls?.large?.toGlideUrl()
                if (url != null) {
                    iv.visibility = View.VISIBLE
                    iv.setOnClick {
                        it.findFragmentOrNull<NavFragment>()?.onClickIllustImpl(item.illust)
                    }
                    Glide.with(context).load(url).into(iv)
                } else {
                    iv.visibility = View.INVISIBLE
                }
            }
            for (index in 0 until 9) {
                func(imageViews[index], index)
            }
            if (item.illust.page_count >= 9) {
                binding.endFrameLayout.visibility = View.VISIBLE
            } else {
                binding.endFrameLayout.visibility = View.INVISIBLE
            }
            binding.line2.isVisible = item.illust.page_count > 3
            binding.line3.isVisible = item.illust.page_count > 6
            binding.extraSize.isVisible = item.illust.page_count > 9
            binding.extraSize.text = "+${item.illust.page_count - 9}"
        }

        binding.singleMedia.isVisible = item.illust.page_count == 1
        binding.doubleMedia.isVisible = item.illust.page_count == 2
        binding.multiMedia.isVisible = item.illust.page_count >= 3
    }
}