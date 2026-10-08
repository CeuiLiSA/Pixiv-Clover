package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentRightBinding

class RightFragment : NavFragment(R.layout.fragment_right) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentRightBinding.bind(view)
        binding.childViewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                if (position == 0) {
                    return PostFragment()
                } else {
                    return FakeRecmdPostFragment()
                }
            }
        }
    }
}