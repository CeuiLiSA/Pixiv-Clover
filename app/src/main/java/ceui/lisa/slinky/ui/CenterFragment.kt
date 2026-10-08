package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.findFragment
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.databinding.ItemButtonLayoutBinding
import ceui.lisa.slinky.databinding.ItemCategoryLayoutBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.models.TrendingTag
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.utils.visibleOrGone
import com.bumptech.glide.Glide


class CenterPageRepository : CustomRepository<CenterFragment>() {

    private val recommendUsers by lazy { RecommendUsersValueContent(coroutineScope) }
    private val pixivisions by lazy { PixivisionsValueContent(coroutineScope) }

    override suspend fun suspendRefresh(
        fragment: CenterFragment
    ) {
        val items = mutableListOf<SlinkyItem>()
        items.add(SpaceHolder(height = 50.pxValue))
        items.add(CategoryHolder())
        items.add(
            RedSectionHeaderHolder(
                fragment.getString(R.string.recommend_user),
                seeMoreString = fragment.getString(R.string.more),
                type = SeeMoreType.RECOMMEND_USER
            )
        )
        items.add(RecommendUserHolder(recommendUsers))
        items.add(PixivisionHolder(fragment, pixivisions))
        items.add(ButtonHolder())
        items.add(SpaceHolder(height = 60.pxValue))
        holderList.value = items
        refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
        recommendUsers.refresh()
        pixivisions.refresh()
    }
}

class CenterFragment : SlinkyListFragment(), PostAction, SeeMoreAction,
    TrendingTagAction, ReselectAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel { CenterPageRepository() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarContainer.visibleOrGone = false
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun onClickUser(user: User) {
        onClickUserImpl(user)
    }

    override fun seeMore(type: Int) {
        if (type == SeeMoreType.RECOMMEND_USER) {
            pushFragment(
                R.id.userListFragment,
                UserListFragmentArgs(UserListType.RECOMMEND).toBundle()
            )
        } else if (type == SeeMoreType.TRENDING_TAG) {
            pushFragment(
                R.id.viewPagerFragment,
                ViewPagerFragmentArgs(ViewPagerContentType.TYPE_ARTICLES).toBundle()
            )
        }
    }

    override fun onLongClickTrendingTag(trendingTag: TrendingTag) {
        onLongClickTrendingTagImpl(trendingTag)
    }

    override fun searchTag(name: String) {
        searchTagImpl(name, false)
    }

    override fun onReselected(index: Int) {
        binding.listView.smoothScrollToTopIfNeeded()
    }
}


class ButtonHolder : SlinkyItem()

@ItemHolder(ButtonHolder::class)
class ButtonViewHolder(aa: ItemButtonLayoutBinding) :
    SlinkyViewHolder<ItemButtonLayoutBinding, ButtonHolder>(aa) {

    override fun onBindViewHolder(item: ButtonHolder) {
        super.onBindViewHolder(item)
        Glide.with(binding.partiesAnim).load(R.raw.home_parties).into(binding.partiesAnim)
        Glide.with(binding.circlesAnim).load(R.raw.home_circles).into(binding.circlesAnim)
        binding.illustButton.setOnClick {
            it.findFragment<NavFragment>().pushFragment(
                R.id.viewPagerFragment,
                ViewPagerFragmentArgs(ViewPagerContentType.TYPE_ILLUST_RANK).toBundle()
            )
        }
        binding.mangaButton.setOnClick {
            it.findFragment<NavFragment>().pushFragment(
                R.id.viewPagerFragment,
                ViewPagerFragmentArgs(ViewPagerContentType.TYPE_MANGA_RANK).toBundle()
            )
        }
    }
}

class CategoryHolder : SlinkyItem() {
    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return other is CategoryHolder
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return other is CategoryHolder
    }
}

@ItemHolder(CategoryHolder::class)
class CategoryViewHolder(aa: ItemCategoryLayoutBinding) :
    SlinkyViewHolder<ItemCategoryLayoutBinding, CategoryHolder>(aa) {

    override fun onBindViewHolder(item: CategoryHolder) {
        super.onBindViewHolder(item)
        binding.latestFrame.setOnClick {
            it.findFragment<NavFragment>().pushFragment(
                R.id.viewPagerFragment,
                ViewPagerFragmentArgs(ViewPagerContentType.TYPE_LATEST_CONTENTS).toBundle()
            )
        }
        binding.novelFrame.setOnClick {
            it.findFragment<NavFragment>().pushFragment(
                R.id.viewPagerFragment,
                ViewPagerFragmentArgs(ViewPagerContentType.TYPE_NOVEL_CENTER).toBundle()
            )
        }
        binding.manga.setOnClick {
            it.findFragment<NavFragment>().pushFragment(
                R.id.navigation_recmd_manga_fragment,
            )
        }
        binding.hotTag.setOnClick {
            it.findFragment<NavFragment>().pushFragment(
                R.id.viewPagerFragment,
                ViewPagerFragmentArgs(ViewPagerContentType.TYPE_TRENDING_TAG).toBundle()
            )
        }
    }
}