package ceui.lisa.slinky.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentCaptionBinding
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.ObjectPool
import java.util.Objects

class CaptionFragment : NavFragment(R.layout.fragment_caption) {

    private val binding by viewBinding(FragmentCaptionBinding::bind)
    private val safeArgs: CaptionFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ObjectPool.get<Illust>(safeArgs.illustId).observe(viewLifecycleOwner) { illust ->
            binding.textView.movementMethod = LinkMovementMethod.getInstance()
            binding.textView.setCaption(illust.caption)
        }
    }
}