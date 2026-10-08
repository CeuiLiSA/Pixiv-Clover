package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.CommonToolbarBinding
import ceui.lisa.slinky.databinding.FragmentTabCommonListBinding
import ceui.lisa.slinky.list.ItemFragment
import ceui.lisa.slinky.models.IllustSeriesDetail
import ceui.lisa.slinky.models.ObjectType
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.utils.loadMedia
import ceui.lisa.slinky.utils.toGlideUrl
import com.blankj.utilcode.util.BarUtils
import com.bumptech.glide.Glide

class StyleFragment : NavFragment(R.layout.fragment_tab_common_list) {

    private val safeArgs: StyleFragmentArgs by navArgs<StyleFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTabCommonListBinding.bind(view)

        if (safeArgs.objectType == ObjectType.NOVEL) {
            ObjectPool.get<IllustSeriesDetail>(safeArgs.seriesId).observe(viewLifecycleOwner) { detail ->
                binding.headerImage.loadMedia(detail.url)
                actionbarContent.title.value = detail?.title
            }
        } else {
            ObjectPool.get<IllustSeriesDetail>(safeArgs.seriesId).observe(viewLifecycleOwner) { detail ->
                binding.headerImage.loadMedia(detail.cover_image_urls?.medium)
                actionbarContent.title.value = detail?.title
            }
        }
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 1
            }

            override fun createFragment(position: Int): Fragment {
                return IllustSeriesFragment().apply {
                    arguments = IllustSeriesFragmentArgs(safeArgs.seriesId, safeArgs.objectType).toBundle()
                }
            }

        }
    }
}