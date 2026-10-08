package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.styles.LinearItemDecoration
import ceui.lisa.slinky.ui.novel.NovelSeriesRepository

class IllustSeriesFragment : SlinkyListFragment(R.layout.fragment_slinky_list) {

    private val safeArgs by navArgs<IllustSeriesFragmentArgs>()
    private val viewModel by listViewModel({ safeArgs }) { args ->
        if (args.objectType == ObjectType.ILLUST) {
            IllustSeriesRepository(args.seriesId)
        } else {
            NovelSeriesRepository(args.seriesId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSlinkyListBinding.bind(view)
        binding.toolbarContainer.isVisible = false
        binding.listView.addItemDecoration(LinearItemDecoration(18.pxValue))
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }
}