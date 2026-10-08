package ceui.lisa.slinky.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentButtonTestBinding
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.utils.openWebPageInApp


class ButtonFragment : NavFragment(R.layout.fragment_button_test) {

    private val mWindowBackgroundBackup = -1
    private val mDecorBackgroundBackup: Drawable? = null
    private val mDecorChildBackgroundBackup: Drawable? = null

    private var mAnimationAnimatedFraction = 0f
    private val mDecorChildCornerRadius = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentButtonTestBinding.bind(view)
        val context = requireContext()
        binding.small.setOnClick {
//            openWebPageInApp("https://www.pixiv.net")
            pushFragment(R.id.navigation_web_fragment)
        }

        binding.big.setOnClick {
            action {
                Client.webApi.getMessageList()
            }
        }
    }

}
