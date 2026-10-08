package ceui.lisa.slinky.ui.novel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.core.RefreshHint
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.observeEvent
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.ui.NovelAction
import ceui.lisa.slinky.ui.SearchViewModel
import ceui.lisa.slinky.ui.onClickNovelImpl
import ceui.lisa.slinky.ui.slinkyLaunchWhenResumed
import ceui.lisa.slinky.ui.viewBinding
import ceui.lisa.slinky.utils.visibleOrGone

object SortType {
    const val DATE_DESC = "date_desc" //   最新
    const val POPULAR = "popular_desc" //  会员
    const val POPULAR_FOR_MALE = "popular_male_desc" //  受男性欢迎
    const val POPULAR_FOR_FEMALE = "popular_female_desc" //  受女性欢迎
    const val DATE_ASC = "date_asc" //    旧到新
}


class SearchNovelFragment : SlinkyListFragment(),
    NovelAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: SearchNovelFragmentArgs by navArgs()
    private val searchViewModel by viewModels<SearchViewModel>(ownerProducer = { requireParentFragment() })
    private val viewModel by listViewModel({ safeArgs.word }, { searchViewModel }) { word, vm ->
        NovelListRepository(
            loader = {
                val keyword = vm.tagList.value?.map { it.name }?.joinToString(separator = " ") ?: ""
                Client.appApi.searchNovel(keyword, SortType.DATE_DESC)
            },
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarContainer.visibleOrGone = false
        searchViewModel.normalRefreshEvent.observeEvent(viewLifecycleOwner) {
            slinkyLaunchWhenResumed {
                viewModel.refresh(RefreshHint.pullToRefresh(), this@SearchNovelFragment)
            }
        }
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun onClickNovel(novel: Novel) {
        onClickNovelImpl(novel)
    }
}

