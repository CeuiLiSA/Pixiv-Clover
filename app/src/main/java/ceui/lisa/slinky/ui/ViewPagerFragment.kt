package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import ceui.lisa.slinky.ActionItem
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.Event
import ceui.lisa.slinky.core.combineLatest
import ceui.lisa.slinky.core.showKeyboard
import ceui.lisa.slinky.databinding.FragmentViewPagerBinding
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.models.Tag
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.requireLoggedInAccountImpl
import ceui.lisa.slinky.requireLoggedInUserId
import ceui.lisa.slinky.ui.dialog.Action
import ceui.lisa.slinky.ui.dialog.moveCursorToEnd
import ceui.lisa.slinky.ui.dialog.showActionMenu
import ceui.lisa.slinky.ui.novel.FollowUsersNovelFragment
import ceui.lisa.slinky.ui.novel.LatestNovelFragment
import ceui.lisa.slinky.ui.novel.NovelHistoryFragment
import ceui.lisa.slinky.ui.novel.PopularNovelFragment
import ceui.lisa.slinky.ui.novel.PopularNovelFragmentArgs
import ceui.lisa.slinky.ui.novel.RankingNovelFragment
import ceui.lisa.slinky.ui.novel.RankingNovelFragmentArgs
import ceui.lisa.slinky.ui.novel.RecmdNovelFragment
import ceui.lisa.slinky.ui.novel.SearchNovelFragment
import ceui.lisa.slinky.ui.novel.SearchNovelFragmentArgs
import ceui.lisa.slinky.ui.novel.SortType
import ceui.lisa.slinky.ui.novel.UserBookmarkedNovelFragment
import ceui.lisa.slinky.ui.novel.UserBookmarkedNovelFragmentArgs
import ceui.lisa.slinky.ui.novel.UserCreatedNovelFragment
import ceui.lisa.slinky.ui.novel.UserCreatedNovelFragmentArgs
import ceui.lisa.slinky.ui.watchlist.WatchlistFragment
import ceui.lisa.slinky.ui.watchlist.WatchlistFragmentArgs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import per.goweii.layer.design.cupertino.CupertinoPopoverLayer
import per.goweii.layer.dialog.ktx.contentView

object ViewPagerContentType {
    const val TYPE_ILLUST_RANK = 1
    const val TYPE_SEARCH_ILLUST = 2
    const val TYPE_MY_BOOKMARK_ILLUST = 3
    const val TYPE_TRENDING_TAG = 4
    const val TYPE_ARTICLES = 5
    const val TYPE_MANGA_RANK = 6
    const val TYPE_SEARCH_NOVEL = 7
    const val TYPE_MY_FOLLOWING = 8
    const val TYPE_SEARCH_KEY_WORD = 9
    const val TYPE_VIEW_HISTORY = 10
    const val TYPE_LATEST_CONTENTS = 11
    const val TYPE_NOVEL_CENTER = 12
    const val TYPE_WATCHLIST = 13
    const val TYPE_CREATED_BY_ME = 14
    const val TYPE_MY_BOOKMARK_NOVEL = 15
    const val TYPE_RANKING_NOVEL = 16
}

interface ViewPagerContainer {

}

class ViewPagerViewModel : ViewModel() {
    val tabLiveData = MutableLiveData<String>()
}

class ViewPagerFragment : NavFragment(R.layout.fragment_view_pager), ViewPagerContainer {

    private val binding by viewBinding(FragmentViewPagerBinding::bind)
    private val safeArgs: ViewPagerFragmentArgs by navArgs()
    private val viewModel by viewModels<SearchViewModel>()
    private val viewPagerViewModel by viewModels<ViewPagerViewModel>()

    override fun onViewFirstCreated(view: View) {
        super.onViewFirstCreated(view)
        viewModel.word.value = safeArgs.word
        viewModel.tagList.value = safeArgs.word?.split(" ")?.map { Tag(it) } ?: listOf()
        viewModel.popularSortType.value = SortType.POPULAR
        viewModel.regularSortType.value = SortType.DATE_DESC
    }

    private fun commitEditingTag() {
        val draft = viewModel.inputDraft.value ?: ""
        if (draft.isNotEmpty()) {
            (viewModel.tagList.value ?: listOf()).toMutableList().also {
                it.add(Tag(draft))
                viewModel.tagList.value = it
                viewModel.inputDraft.value = ""
                binding.tagEditer.clearFocus()
                binding.searchParent.requestFocus()
                hideKeyboard()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (safeArgs.type == ViewPagerContentType.TYPE_SEARCH_ILLUST ||
            safeArgs.type == ViewPagerContentType.TYPE_SEARCH_KEY_WORD ||
            safeArgs.type == ViewPagerContentType.TYPE_SEARCH_NOVEL
        ) {
            binding.clearText.setOnClick {
                viewModel.tagList.value = listOf()
                viewModel.word.value = ""
                viewModel.inputDraft.value = ""
            }
            combineLatest(viewModel.tagList, viewModel.inputDraft).observe(viewLifecycleOwner) {
                val tags = it?.first ?: listOf()
                val inputing = it?.second ?: ""
                binding.search.isEnabled = tags.isNotEmpty() == true || inputing.isNotEmpty() == true
            }
            binding.viewModel = viewModel
            binding.searchLayout.isVisible = true
            binding.search.setOnClick {
                commitEditingTag()
                viewModel.normalRefreshEvent.value = Event(Unit)
                viewModel.popularRefreshEvent.value = Event(Unit)
                viewModel.usersRefreshEvent.value = Event(Unit)
            }
            binding.tagEditer.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    commitEditingTag()
                }
                true
            }
            binding.tagsFlowView.setOnCellClickListner { cell, index ->
                showActionMenu(cell) {
                    buildList {
                        add(Action(getString(R.string.edit)) {
                            viewModel.tagList.value?.let {
                                val copied = it.toMutableList()
                                viewModel.inputDraft.value = copied.getOrNull(index)?.name
                                copied.removeAt(index)
                                viewModel.tagList.value = copied
                                launch {
                                    delay(30L)
                                    binding.tagEditer.moveCursorToEnd()
                                    binding.tagEditer.requestLayout()
                                    binding.tagEditView.requestLayout()
                                    showKeyboard(binding.tagEditer)
                                }
                            }
                        })
                        add(Action(getString(R.string.delete)) {
                            viewModel.tagList.value?.let {
                                val copied = it.toMutableList()
                                copied.removeAt(index)
                                viewModel.tagList.value = copied
                            }
                        })
                    }
                }
            }
        } else {
            binding.searchLayout.isVisible = false
        }

        when (safeArgs.type) {
            ViewPagerContentType.TYPE_ILLUST_RANK -> {
                actionbarContent.title.value = getString(R.string.rank_list)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return ILLUST_RANK_MODE.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return RankFragment().apply {
                            arguments = RankFragmentArgs(ILLUST_RANK_MODE[position]).toBundle()
                        }
                    }
                }
                binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = ILLUST_RANK_MODE_STRING[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_RANKING_NOVEL -> {
                actionbarContent.title.value = getString(R.string.rank_list)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return ILLUST_RANK_MODE.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return RankingNovelFragment().apply {
                            arguments = RankingNovelFragmentArgs(ILLUST_RANK_MODE[position]).toBundle()
                        }
                    }
                }
                binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = ILLUST_RANK_MODE_STRING[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_SEARCH_ILLUST -> {
                val isVip = requireLoggedInAccountImpl().user?.isPremium() == true
                val searchModel = listOf(
                    getString(R.string.mode_normal),
                    if (isVip) getString(R.string.mode_popular) else getString(R.string.mode_popular_preview),
                )
                actionbarContent.title.value = safeArgs.word
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return searchModel.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return if (position == 0) {
                            SearchIllustFragment().apply {
                                arguments = SearchIllustFragmentArgs(0).toBundle()
                            }
                        } else {
                            if (isVip) {
                                SearchIllustFragment().apply {
                                    arguments = SearchIllustFragmentArgs(1).toBundle()
                                }
                            } else {
                                PopularIllustFragment()
                            }
                        }
                    }
                }
                actionbarContent.endItems.value = listOf(
                    ActionItem(R.drawable.ic_filter) {

                    }
                )
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = searchModel[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_SEARCH_KEY_WORD -> {
                val isVip = requireLoggedInAccountImpl().user?.isPremium() == true
                val searchModel = listOf(
                    getString(R.string.mode_normal),
                    if (isVip) getString(R.string.mode_popular) else getString(R.string.mode_popular_preview),
                    getString(R.string.user),
                )
                actionbarContent.title.value = safeArgs.word
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return searchModel.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return if (position == 0) {
                            SearchIllustFragment().apply {
                                arguments = SearchIllustFragmentArgs(0).toBundle()
                            }
                        } else if (position == 1) {
                            if (isVip) {
                                SearchIllustFragment().apply {
                                    arguments = SearchIllustFragmentArgs(1).toBundle()
                                }
                            } else {
                                PopularIllustFragment()
                            }
                        } else {
                            UserListFragment().apply {
                                arguments =
                                    UserListFragmentArgs(UserListType.SEARCH_USER).toBundle()
                            }
                        }
                    }
                }
                actionbarContent.endItems.value = listOf(
                    ActionItem(R.drawable.ic_filter) {

                    }
                )
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = searchModel[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_SEARCH_NOVEL -> {
                val searchModel = listOf(
                    getString(R.string.mode_normal),
                    getString(R.string.mode_popular),
                )
                actionbarContent.title.value = safeArgs.word
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return searchModel.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return if (position == 0) {
                            SearchNovelFragment().apply {
                                arguments = SearchNovelFragmentArgs(safeArgs.word ?: "").toBundle()
                            }
                        } else {
                            PopularNovelFragment().apply {
                                arguments = PopularNovelFragmentArgs(safeArgs.word ?: "").toBundle()
                            }
                        }
                    }
                }
                actionbarContent.endItems.value = listOf(
                    ActionItem(R.drawable.ic_filter) {

                    }
                )
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = searchModel[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_MY_BOOKMARK_ILLUST -> {
                val bookmarkMode = listOf(
                    getString(R.string.mode_public),
                    getString(R.string.mode_private),
                )
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return bookmarkMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return UserBookmarkedIllustFragment().apply {
                            arguments = UserBookmarkedIllustFragmentArgs(
                                senderId,
                                if (position == 0) BookmarkType.PUBLIC else BookmarkType.PRIVATE,
                                false
                            ).toBundle()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = bookmarkMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_MY_BOOKMARK_NOVEL -> {
                val bookmarkMode = listOf(
                    getString(R.string.mode_public),
                    getString(R.string.mode_private),
                )
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return bookmarkMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return UserBookmarkedNovelFragment().apply {
                            arguments = UserBookmarkedNovelFragmentArgs(
                                senderId,
                                if (position == 0) BookmarkType.PUBLIC else BookmarkType.PRIVATE,
                                false
                            ).toBundle()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = bookmarkMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_MY_FOLLOWING -> {
                val bookmarkMode = listOf(
                    getString(R.string.mode_public),
                    getString(R.string.mode_private),
                )
                actionbarContent.title.value = getString(R.string.my_following)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return bookmarkMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        val type =
                            if (position == 0) UserListType.FOLLOWING else UserListType.FOLLOWING_PRIVATE
                        return UserListFragment().apply {
                            arguments = UserListFragmentArgs(type, senderId).toBundle()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = bookmarkMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_NOVEL_CENTER -> {
                val tagMode = listOf(
                    getString(R.string.recommend),
                    getString(R.string.follow),
                )
                actionbarContent.title.value = getString(R.string.novel)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return tagMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return when (position) {
                            0 -> {
                                RecmdNovelFragment()
                            }

                            1 -> {
                                FollowUsersNovelFragment()
                            }

                            else -> Fragment()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = tagMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_LATEST_CONTENTS -> {
                val tagMode = listOf(
                    getString(R.string.illust),
                    getString(R.string.manga),
                    getString(R.string.novel),
                )
                actionbarContent.title.value = getString(R.string.latest_content)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return tagMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return when (position) {
                            0 -> {
                                LatestIllustFragment().apply {
                                    arguments =
                                        LatestIllustFragmentArgs(ObjectType.ILLUST).toBundle()
                                }
                            }

                            1 -> {
                                LatestIllustFragment().apply {
                                    arguments =
                                        LatestIllustFragmentArgs(ObjectType.MANGA).toBundle()
                                }
                            }

                            2 -> {
                                LatestNovelFragment()
                            }

                            else -> Fragment()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = tagMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_CREATED_BY_ME -> {
                val tagMode = listOf(
                    getString(R.string.illust),
                    getString(R.string.manga),
                    getString(R.string.novel),
                )
                val senderId = requireLoggedInUserId()
                actionbarContent.title.value = getString(R.string.my_works)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return tagMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return when (position) {
                            0 -> {
                                UserCreatedIllustFragment().apply {
                                    arguments =
                                        UserCreatedIllustFragmentArgs(senderId, ObjectType.ILLUST).toBundle()
                                }
                            }

                            1 -> {
                                UserCreatedIllustFragment().apply {
                                    arguments =
                                        UserCreatedIllustFragmentArgs(senderId, ObjectType.MANGA).toBundle()
                                }
                            }

                            2 -> {
                                UserCreatedNovelFragment().apply {
                                    arguments =
                                        UserCreatedNovelFragmentArgs(senderId).toBundle()
                                }
                            }

                            else -> Fragment()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = tagMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_TRENDING_TAG -> {
                val tagMode = listOf(
                    getString(R.string.illust_or_manga),
                    getString(R.string.novel),
                )
                val tagAPI = arrayOf(ObjectType.ILLUST, ObjectType.NOVEL)
                actionbarContent.title.value = getString(R.string.trending_tags)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return tagMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return TrendingTagsFragment().apply {
                            arguments = TrendingTagsFragmentArgs(tagAPI[position]).toBundle()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = tagMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_WATCHLIST -> {
                val tagMode = listOf(
                    getString(R.string.illust_or_manga),
                    getString(R.string.novel),
                )
                val types = arrayOf(ObjectType.MANGA, ObjectType.NOVEL)
                actionbarContent.title.value = getString(R.string.trending_tags)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return tagMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return WatchlistFragment().apply {
                            arguments = WatchlistFragmentArgs(types[position]).toBundle()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = tagMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_ARTICLES -> {
                val tagMode = listOf(
                    getString(R.string.illust_or_manga),
                    getString(R.string.novel),
                )
                val tagAPI = arrayOf(ObjectType.ILLUST, ObjectType.NOVEL)
                actionbarContent.title.value = getString(R.string.trending_tags)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return tagMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return TrendingTagsFragment().apply {
                            arguments = TrendingTagsFragmentArgs(tagAPI[position]).toBundle()
                        }
                    }
                }
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = tagMode[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_MANGA_RANK -> {
                actionbarContent.title.value = getString(R.string.rank_list)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return MANGA_RANK_MODE.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return RankFragment().apply {
                            arguments = RankFragmentArgs(MANGA_RANK_MODE[position]).toBundle()
                        }
                    }
                }
                binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                    tab.text = MANGA_RANK_MODE_STRING[position]
                }.attach()
            }

            ViewPagerContentType.TYPE_VIEW_HISTORY -> {
                val tagMode = listOf(
                    getString(R.string.illust),
                    getString(R.string.novel),
                    getString(R.string.user),
                )
                actionbarContent.title.value = getString(R.string.view_history)
                binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int {
                        return tagMode.size
                    }

                    override fun createFragment(position: Int): Fragment {
                        return when (position) {
                            0 -> {
                                IllustHistoryFragment()
                            }

                            1 -> {
                                NovelHistoryFragment()
                            }

                            2 -> {
                                UserHistoryFragment()
                            }

                            else -> {
                                Fragment()
                            }
                        }
                    }
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val illustCount = RoomDB.db().historyDao().getCountByType(HistoryType.ILLUST)
                        val novelCount = RoomDB.db().historyDao().getCountByType(HistoryType.NOVEL)
                        val userCount = RoomDB.db().historyDao().getCountByType(HistoryType.USER)
                        val countArray = arrayOf(illustCount, novelCount, userCount)
                        withContext(Dispatchers.Main) {
                            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                                tab.text = tagMode[position] + "(${countArray[position]})"
                                viewPagerViewModel.tabLiveData.observe(viewLifecycleOwner) {
                                    tab.text = it
                                }
                            }.attach()
                        }
                    }
                }
            }
        }
    }

    override fun showFakeStatusBar(): Boolean {
        return false
    }

    companion object {

        val ILLUST_RANK_MODE = listOf(
            "day",
            "week",
            "month",
            "day_male",
            "day_female",
            "week_original",
            "week_rookie",
//            "day_r18",
//            "week_r18",
//            "day_male_r18",
//            "day_female_r18"
        )
        val ILLUST_RANK_MODE_STRING = listOf(
            "日榜",
            "周榜",
            "月榜",
            "男性向",
            "女性向",
            "原创",
            "新人",
//            "day_r18",
//            "week_r18",
//            "day_male_r18",
//            "day_female_r18"
        )

        private val MANGA_RANK_MODE = listOf(
            "day_manga",
            "week_manga",
            "month_manga",
            "week_rookie_manga",
            "day_r18_manga"
        )

        private val MANGA_RANK_MODE_STRING = listOf(
            "日榜",
            "周榜",
            "月榜",
            "新人",
            "R18"
        )
    }
}