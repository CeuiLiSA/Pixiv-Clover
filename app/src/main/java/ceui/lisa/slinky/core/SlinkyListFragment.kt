package ceui.lisa.slinky.core

import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.DeletedFrameLayoutBinding
import ceui.lisa.slinky.databinding.ItemLoadingBinding
import ceui.lisa.slinky.getHumanReadableMessage
import ceui.lisa.slinky.styles.WaterMark
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.performBack
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.setUpLinearlayoutManager
import com.scwang.smart.refresh.header.FalsifyFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout

abstract class SlinkyListFragment(layoutId: Int = R.layout.fragment_slinky_list) :
    NavFragment(layoutId) {


    open fun isDefaultLayoutManager(): Boolean {
        return true
    }
}

inline fun <reified FragmentT : SlinkyListFragment> FragmentT.setUpSlinkyList(
    listView: RecyclerView,
    refreshLayout: SmartRefreshLayout,
    itemLoading: ItemLoadingBinding,
    viewModel: SlinkyListViewModel<FragmentT>
) {
    if (isAbandonedPage()) {
        return
    }

    val adapter = SLAdapter(viewLifecycleOwner)
    listView.adapter = adapter
    if (isDefaultLayoutManager()) {
        listView.setUpLinearlayoutManager(requireContext())
    }
    viewModel.holderList.observe(viewLifecycleOwner) { list ->
        adapter.submitList(list)
    }
    itemLoading.setUpRefreshState(
        this,
        refreshLayout,
        viewModel.refreshState,
        refreshBlock = { viewModel.refresh(RefreshHint.pullToRefresh(), this) },
        viewModel.loadMoreState,
        loadMoreBlock = { viewModel.loadMore(this) },
        retryBlock = { viewModel.refresh(RefreshHint.retry(), this) },
    )
    viewModel.attachFragment(this)
}

fun ItemLoadingBinding.setUpRefreshState(
    fragment: NavFragment,
    refreshLayout: SmartRefreshLayout,
    refreshState: LiveData<LoadState>,
    refreshBlock: () -> Unit,
    loadMoreState: LiveData<LoadState>,
    loadMoreBlock: () -> Unit,
    retryBlock: () -> Unit,
) {
    with(fragment) {
        val context = requireContext()
        refreshLayout.setRefreshHeader(MaterialHeader(context))
        refreshLayout.setOnRefreshListener {
            refreshBlock.invoke()
        }
        refreshLayout.setOnLoadMoreListener {
            loadMoreBlock.invoke()
        }
        emptyActionButton.setOnClick {
            retryBlock.invoke()
        }

        val slinkyFooter = SlinkyFooter(context)
        refreshState.observe(viewLifecycleOwner) { state ->
            if (state is LoadState.LOADED) {
                progressCircular.showProgress(false)
                loadingFrame.isVisible = false
                refreshLayout.finishRefresh()

                if (state.hasContent) {
                    emptyFrame.isVisible = false
                } else {
                    emptyFrame.isVisible = true
                    emptyActionButton.text = getString(R.string.refresh)
                    emptyTitle.text = getString(R.string.empty_content_here)
                }

                if (state.hasNext) {
                    refreshLayout.setRefreshFooter(slinkyFooter)
                } else {
                    refreshLayout.setRefreshFooter(FalsifyFooter(context))
                }
            } else if (state is LoadState.LOADING) {
                emptyFrame.isVisible = false
                if (state.refreshHint?.cause == RefreshHint.Cause.PULL_TO_REFRESH) {
                    loadingFrame.isVisible = false
                    progressCircular.showProgress(false)
                } else if (state.refreshHint?.cause == RefreshHint.Cause.INITIAL_LOAD || state.refreshHint?.cause == RefreshHint.Cause.RETRY) {
                    loadingFrame.isVisible = true
                    progressCircular.showProgress(true)
                }
            } else if (state is LoadState.ERROR) {
                progressCircular.showProgress(false)
                loadingFrame.isVisible = false
                refreshLayout.finishRefresh()
                refreshLayout.finishLoadMore()

                emptyFrame.isVisible = true
                emptyActionButton.text = getString(R.string.retry)
                emptyTitle.text = state.exception.getHumanReadableMessage(context)
            }
        }

        loadMoreState.observe(viewLifecycleOwner) { state ->
            if (state is LoadState.LOADED) {
                refreshLayout.finishLoadMore()
                if (state.hasNext) {
                    refreshLayout.setRefreshFooter(slinkyFooter)
                } else {
                    refreshLayout.setRefreshFooter(FalsifyFooter(context))
                }
            } else if (state is LoadState.LOADING) {
                slinkyFooter.loadingBinding.loadingFrame.isVisible = true
                slinkyFooter.loadingBinding.emptyFrame.isVisible = false
            } else if (state is LoadState.ERROR) {
                slinkyFooter.loadingBinding.loadingFrame.isVisible = false
                slinkyFooter.loadingBinding.emptyFrame.isVisible = true
                slinkyFooter.loadingBinding.emptyActionButton.setOnClick {
                    loadMoreBlock.invoke()
                }
            }
        }
    }
}

fun ItemLoadingBinding.setUpLoadingState(
    refreshState: LiveData<LoadState>,
    lifecycleOwner: LifecycleOwner,
    refreshBlock: () -> Unit
) {
    val context = root.context
    refreshState.observe(lifecycleOwner) { loadState ->
        when (loadState) {
            is LoadState.LOADING -> {
                progressCircular.showProgress(true)
                loadingFrame.isVisible = true
                emptyFrame.isVisible = false
            }

            is LoadState.LOADED -> {
                progressCircular.showProgress(false)
                loadingFrame.isVisible = false
                if (loadState.hasContent) {
                    emptyFrame.isVisible = false
                } else {
                    emptyFrame.isVisible = true
                    emptyActionButton.text =
                        context.getString(R.string.refresh)
                    emptyTitle.text =
                        context.getString(R.string.empty_content_here)
                }
            }

            is LoadState.ERROR -> {
                progressCircular.showProgress(false)
                loadingFrame.isVisible = false
                emptyFrame.isVisible = true
                emptyActionButton.text =
                    context.getString(R.string.retry)
                emptyTitle.text =
                    loadState.exception.getHumanReadableMessage(context)
            }
        }
    }
    emptyActionButton.setOnClick {
        refreshBlock.invoke()
    }
}

fun <FragmentT : NavFragment> FragmentT.setUpDisablePage(binding: DeletedFrameLayoutBinding) {
    if (!isAbandonedPage()) {
        return
    }

    val toolbarContainer = requireView().findViewById<FrameLayout?>(R.id.toolbar_container)
    if (toolbarContainer is FrameLayout) {
        toolbarContainer.isVisible = false
    }

    binding.deletedFrame.isVisible = true
    binding.deletedFrame.isClickable = true

    binding.waterMark.setImageDrawable(WaterMark(getString(R.string.disabled)))
    binding.leaveButton.setOnClick {
        performBack()
    }
}