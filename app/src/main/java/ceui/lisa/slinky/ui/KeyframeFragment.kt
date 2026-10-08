package ceui.lisa.slinky.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentKeyFrameBinding
import timber.log.Timber


class KeyframeFragment : NavFragment(R.layout.fragment_key_frame) {

    private val binding by viewBinding(FragmentKeyFrameBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//
//        val frame0: Keyframe = Keyframe.ofFloat(0F, 1F)
//        val frame1: Keyframe = Keyframe.ofFloat(666/2000F, 1F)
//        val frame2: Keyframe = Keyframe.ofFloat(999/2000F, 0F)
//        val frame3: Keyframe = Keyframe.ofFloat(1333/2000F, 1F)
//        val frame4: Keyframe = Keyframe.ofFloat(2000/2000F, 1F)
//        val frameHolder = PropertyValuesHolder.ofKeyframe("alpha", frame0, frame1, frame2, frame3, frame4)
//        val animator: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(binding.rotate, frameHolder)
//        animator.duration = 2000
//        animator.repeatCount = ObjectAnimator.INFINITE
//        animator.repeatMode = ObjectAnimator.RESTART
//        animator.start()

        val progressDrawable = CircularProgressDrawable(requireContext()).apply {
            setColorSchemeColors(Color.WHITE)
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 3.pxValue.toFloat()
            centerRadius = 10.pxValue.toFloat() / 2
        }
        progressDrawable.start()
        binding.rotate.setImageDrawable(progressDrawable)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.seekBarA.min = 3.pxValue
        }
        binding.seekBarA.max = 50.pxValue
        binding.seekBarA.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressDrawable.strokeWidth = progress.toFloat()
                Timber.d("asdasdawawdawd strokeWidth ${progress.toFloat()}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.seekBarB.min = 10.pxValue
        }
        binding.seekBarB.max = 100.pxValue
        binding.seekBarB.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressDrawable.centerRadius = progress / 2F
                Timber.d("asdasdawawdawd centerRadius ${progress / 2F}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
}