package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentIllustViewPagerBinding

object ViewPagerMap {

    val map: HashMap<String, List<Long>> = hashMapOf()
}

class IllustViewPagerFragment : NavFragment(R.layout.fragment_illust_view_pager) {

    private val binding by viewBinding(FragmentIllustViewPagerBinding::bind)
    private val safeArgs: IllustViewPagerFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (safeArgs.uuid.isEmpty()) {
            return
        }

        val illustList = ViewPagerMap.map[safeArgs.uuid]
        if (illustList == null || illustList.isEmpty()) {
            return
        }

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return illustList.size
            }

            override fun createFragment(position: Int): Fragment {
                return IllustFragment().apply {
                    arguments = IllustFragmentArgs(illustList[position]).toBundle()
                }
            }
        }
    }
}