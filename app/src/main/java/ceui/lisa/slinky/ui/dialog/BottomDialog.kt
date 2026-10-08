package ceui.lisa.slinky.ui.dialog

import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.navArgs
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.SLAdapter
import ceui.lisa.slinky.databinding.BottomSheetBinding
import ceui.lisa.slinky.databinding.ItemIllustDownloadPreviewBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.glide.saveImageImpl
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.ui.DownloadIllustAction
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.ui.findActionReceiverOrNull
import ceui.lisa.slinky.ui.screenHeight
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.setUpLinearlayoutManager
import ceui.lisa.slinky.ui.viewBinding
import ceui.lisa.slinky.utils.toGlideUrl
import com.blankj.utilcode.util.BarUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber


class BottomDialog(private val parentFragment: NavFragment) : BottomSheetDialogFragment(),
    DownloadIllustAction {

    private val binding by viewBinding(BottomSheetBinding::bind)
    private val safeArgs: BottomDialogArgs by navArgs()

    companion object {
        const val TAG = "BottomDialog"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentFragment.cropToRound()
        val animator = ValueAnimator.ofFloat(1F, 0.9F).apply {
            duration = 300L
            interpolator = AccelerateDecelerateInterpolator()
        }
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            parentFragment.scaleLiveData.value = value
            Timber.d("asdasdw2423423 ${value}")
        }
        animator.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SlinkyBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listView.setUpLinearlayoutManager(requireContext())

        // this is to show whole layout when expanded (item beneath RecycleView)
        requireView().updateLayoutParams {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = (screenHeight - BarUtils.getStatusBarHeight())
        }

        ObjectPool.get<Illust>(safeArgs.illustId).observe(viewLifecycleOwner) { illust ->
            if (illust.page_count <= 1) {
                return@observe
            }

            val adapter = SLAdapter(viewLifecycleOwner)
            binding.listView.adapter = adapter
            val items = mutableListOf<SlinkyItem>()
            illust.meta_pages?.forEachIndexed { index, metaPage ->
                items.add(PreviewHolder(metaPage.image_urls?.large, index))
            }
            adapter.submitList(items)
        }

    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.halfExpandedRatio = 0.01f
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                //always skip half expanded
                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (view == null) {
                    return
                }
                val mapped = map(slideOffset)
                parentFragment.scaleLiveData.value = mapped
            }
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if ((parentFragment.scaleLiveData.value ?: 0.9F) < 0.95F) {
            val animator = ValueAnimator.ofFloat(0.9F, 1F).apply {
                duration = 300L
                interpolator = AccelerateDecelerateInterpolator()
            }
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                parentFragment.scaleLiveData.value = value
            }
            animator.addListener(onEnd = {
                parentFragment.backToSquare()
            })
            animator.start()
        } else {
            parentFragment.backToSquare()
        }
    }

    private fun map(x: Float): Float {
        return -0.05F * x + 0.95F
    }

    override fun download(index: Int) {
        saveImageImpl(safeArgs.illustId, index)
    }
}

class PreviewHolder(val url: String?, val index: Int) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return index == (other as? PreviewHolder)?.index && url == (other as? PreviewHolder)?.url
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return index == (other as? PreviewHolder)?.index && url == (other as? PreviewHolder)?.url
    }
}


@ItemHolder(PreviewHolder::class)
class PreviewViewHolder(aa: ItemIllustDownloadPreviewBinding) :
    SlinkyViewHolder<ItemIllustDownloadPreviewBinding, PreviewHolder>(aa) {

    override fun onBindViewHolder(item: PreviewHolder) {
        binding.index.text = "P${item.index}"
        binding.url.text = item.url
        binding.download.setOnClick {
            it.findActionReceiverOrNull<DownloadIllustAction>()?.download(item.index)
        }
        Glide.with(context)
            .load(item.url?.toGlideUrl())
            .into(binding.imageView)
    }
}