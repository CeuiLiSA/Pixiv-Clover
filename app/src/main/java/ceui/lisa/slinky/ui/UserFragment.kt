package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.slinky.ActionItem
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpDisablePage
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.models.IllustResponse
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.ui.novel.UserCreatedNovelFragmentArgs
import kotlinx.coroutines.delay

class TitleViewModel : ViewModel() {
    val isTitleDisplaying = MutableLiveData(false)
    val scrollOffset = MutableLiveData(0)
}


class UserFragment : SlinkyListFragment(), PostAction,
    SeeMoreAction, ShowImageViewPager {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: UserFragmentArgs by navArgs()
    private val titleViewModel: TitleViewModel by viewModels()
    private val viewModel by listViewModel({ safeArgs.userId }) { userId ->
        UserProfileRepository(userId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
        setUpDisablePage(binding.deletedFrame)
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
        val magic = dipToPx(142F)

        ObjectPool.get<User>(safeArgs.userId).observe(viewLifecycleOwner) {
            actionbarContent.title.value = it.name
        }
        titleViewModel.isTitleDisplaying.observe(viewLifecycleOwner) {
            actionbarContent.showTitle.value = it
        }

        actionbarContent.endItems.value = listOf(
            ActionItem(R.drawable.ic_more) {
                pushFragment(
                    R.id.userListFragment,
                    UserListFragmentArgs(type = UserListType.RELATED, safeArgs.userId).toBundle()
                )
            }
        )

        binding.listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                var offset = titleViewModel.scrollOffset.value ?: 0
                var isTitleShowing = titleViewModel.isTitleDisplaying.value ?: false

                offset += dy

                if (offset > magic) {
                    if (!isTitleShowing) {
                        isTitleShowing = true
                        titleViewModel.isTitleDisplaying.value = isTitleShowing
                    }
                } else {
                    if (isTitleShowing) {
                        isTitleShowing = false
                        titleViewModel.isTitleDisplaying.value = isTitleShowing
                    }
                }

                titleViewModel.scrollOffset.value = offset
            }
        })
        ObjectPool.get<User>(safeArgs.userId).value?.let {
            visitUser(user = it)
        }
    }

    override fun isDefaultLayoutManager(): Boolean {
        return false
    }

    override fun isAbandonedPage(): Boolean {
        val user = ObjectPool.get<User>(safeArgs.userId).value ?: return true
        val isAvailableUser = user.id.exist() && user.account?.isNotEmpty() == true
        return !isAvailableUser
    }

    override fun onClickUser(user: User) {
        if (user.id == safeArgs.userId) {
            binding.listView.smoothScrollToTopIfNeeded()
            return
        }

        onClickUserImpl(user)
    }

    override fun seeMore(type: Int) {
        if (type == SeeMoreType.CREATED_ILLUST) {
            pushFragment(
                R.id.userCreatedIllustFragment,
                UserCreatedIllustFragmentArgs(safeArgs.userId, ObjectType.ILLUST).toBundle()
            )
        } else if (type == SeeMoreType.CREATED_MANGA) {
            pushFragment(
                R.id.userCreatedIllustFragment,
                UserCreatedIllustFragmentArgs(safeArgs.userId, ObjectType.MANGA).toBundle()
            )
        } else if (type == SeeMoreType.CREATED_NOVEL) {
            pushFragment(
                R.id.navigation_user_created_novels_fragment,
                UserCreatedNovelFragmentArgs(safeArgs.userId).toBundle()
            )
        } else if (type == SeeMoreType.BOOKMARKED_ILLUST) {
            pushFragment(
                R.id.userBookmarkedIllustFragment,
                UserBookmarkedIllustFragmentArgs(
                    safeArgs.userId,
                    BookmarkType.PUBLIC,
                    true
                ).toBundle()
            )
        }
    }

    override fun showImageViewPager(illustId: Long, index: Int) {
        pushFragment(
            R.id.imageViewPagerFragment,
            OriginalImageViewPagerFragmentArgs(illustId, index).toBundle(),
        )
    }
}


fun Long?.exist(): Boolean {
    return (this != null && this > 0L)
}