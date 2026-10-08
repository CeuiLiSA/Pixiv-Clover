package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentImageBinding
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.glide.GlideProgress
import ceui.lisa.slinky.glide.LiveDataProgressListener
import ceui.lisa.slinky.glide.ProgressListener
import ceui.lisa.slinky.utils.toGlideUrl
import com.github.panpf.sketch.displayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class ImageViewModel : ViewModel() {

    val shouldHideStatusBar = MutableLiveData(true)

    fun toggle() {
        val current = shouldHideStatusBar.value ?: false
        shouldHideStatusBar.value = !current
    }
}

class ImageFileViewModel : ViewModel() {
    val fileLiveData = MutableLiveData<File>()
    var isHighQualityImageLoaded: Boolean = false
}

class ImageFragment : NavFragment(R.layout.fragment_image) {

    private val binding by viewBinding(FragmentImageBinding::bind)
    private val safeArgs: ImageFragmentArgs by navArgs()
    private val imageViewModel: ImageViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val fileViewModel by viewModels<ImageFileViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bigImage.setOnClick {
            imageViewModel.toggle()
        }

        val progressbar = binding.progressCircularSmall
        val existing = GlideProgress.get(safeArgs.url)
        val loadListener = if (existing is LiveDataProgressListener) {
            existing
        } else {
            val newly = LiveDataProgressListener(MutableLiveData(), MutableLiveData())
            GlideProgress.add(safeArgs.url, this.lifecycle, newly)
            newly
        }
        loadListener.progressLiveData.observe(viewLifecycleOwner) {
            progressbar.progress = it
        }

        if (safeArgs.lowQualityUrl?.isNotEmpty() == true) {
            launchSuspend {
                withContext(Dispatchers.IO) {
                    try {
                        val file = GlideApp.with(this@ImageFragment)
                            .asFile()
                            .load(safeArgs.lowQualityUrl)
                            .submit()
                            .get()
                        if (!fileViewModel.isHighQualityImageLoaded) {
                            fileViewModel.fileLiveData.postValue(file)
                        }
                    } catch (ex: Exception) {
                        Timber.e(ex)
                    }
                }
            }
        }

        fileViewModel.fileLiveData.observe(viewLifecycleOwner) { file ->
            binding.bigImage.displayImage(file)
        }

        if (safeArgs.url.isNotEmpty()) {
            launchSuspend {
                withContext(Dispatchers.IO) {
                    try {
                        val file = GlideApp.with(this@ImageFragment)
                            .asFile()
                            .load(safeArgs.url)
                            .submit()
                            .get()
                        withContext(Dispatchers.Main) {
                            progressbar.isVisible = false
                        }
                        fileViewModel.isHighQualityImageLoaded = true
                        fileViewModel.fileLiveData.postValue(file)
                    } catch (ex: Exception) {
                        Timber.e(ex)
                    }
                }
            }
        }
    }
}

fun View.animateFadeInQuickly() {
    alpha = 0F
    SpringAnimation(this, DynamicAnimation.ALPHA, 1F).apply {
        spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        spring.stiffness = SpringForce.STIFFNESS_MEDIUM
        start()
    }
}

fun View.animateFadeOutQuickly() {
    SpringAnimation(this, DynamicAnimation.ALPHA, 0F).apply {
        spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        spring.stiffness = SpringForce.STIFFNESS_MEDIUM
        start()
    }
}