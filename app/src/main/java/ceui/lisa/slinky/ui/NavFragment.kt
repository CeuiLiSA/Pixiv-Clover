package ceui.lisa.slinky.ui

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Outline
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import ceui.lisa.slinky.ActionBarViewModel
import ceui.lisa.slinky.R
import ceui.lisa.slinky.TaskQueue
import ceui.lisa.slinky.databinding.ActionItemBinding
import ceui.lisa.slinky.databinding.CommonToolbarBinding
import ceui.lisa.slinky.handleError
import ceui.lisa.slinky.requireLoggedInUserId
import ceui.lisa.slinky.safeCall
import ceui.lisa.slinky.ui.dialog.alertNotice
import ceui.lisa.slinky.ui.dialog.showSpinner
import com.blankj.utilcode.util.BarUtils
import com.yalantis.ucrop.UCrop
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


open class NavFragment(layoutId: Int) : Fragment(layoutId), ActionBarContainer {

    private val fragmentViewModel: NavFragmentViewModel by viewModels()
    private val actionBarViewModel: ActionBarViewModel by viewModels()
    protected val senderId: Long by lazy { requireLoggedInUserId() }
    val scaleLiveData = MutableLiveData<Float>()
    var cropFinishedBlock: (suspend (outputUri: Uri) -> Unit)? = null
    val requestCropImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intent = result.data
            if (intent != null) {
                slinkyLaunchWhenResumed {
                    if (result.resultCode == Activity.RESULT_OK) {
                        UCrop.getOutput(intent)?.let { outputUri ->
                            cropFinishedBlock?.invoke(outputUri)
                            cropFinishedBlock = null
                        }
                    } else if (result.resultCode == UCrop.RESULT_ERROR) {
                        (intent.getSerializableExtra(UCrop.EXTRA_ERROR) as? Throwable)?.let { ex ->
                            alertNotice(title = null, message = ex.message ?: ex.toString())
                        }
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        actionbarContent.title.value = ""
        actionbarContent.showTitle.value = true
        actionbarContent.showStartButton.value = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (fragmentViewModel.viewCreatedTime.value == null) {
            onViewFirstCreated(view)
        }
        fragmentViewModel.viewCreatedTime.value = System.currentTimeMillis()

        safeCall {
            if (!showFakeStatusBar()) {
                return@safeCall
            }

            val toolbarContainer = view.findViewById<FrameLayout?>(R.id.toolbar_container)
            if (toolbarContainer is FrameLayout) {
                val toolbarBinding = DataBindingUtil.inflate<CommonToolbarBinding>(
                    layoutInflater,
                    R.layout.common_toolbar,
                    null,
                    false
                )
                toolbarBinding.placeHolder.updateLayoutParams {
                    height = BarUtils.getStatusBarHeight()
                }

                actionBarViewModel.startButtonColor.observe(viewLifecycleOwner) {
                    if (it != 0) {
                        toolbarBinding.back.imageTintList = ColorStateList.valueOf(it)
                    } else {
                        toolbarBinding.back.imageTintList = null
                    }
                }


                actionBarViewModel.endItems.observe(viewLifecycleOwner) {
                    if (it != null && it.isNotEmpty()) {
                        toolbarBinding.endItems.removeAllViews()
                        it.forEach { actionItem ->
                            val actionBinding = DataBindingUtil.inflate<ActionItemBinding>(
                                layoutInflater,
                                R.layout.action_item,
                                null,
                                false
                            )
                            val actionView = actionBinding.root
                            actionView.setOnClick {
                                actionItem.action.invoke()
                            }
                            actionBinding.lifecycleOwner = viewLifecycleOwner
                            actionBinding.actionItem = actionItem
                            toolbarBinding.endItems.addView(actionBinding.root)
                        }
                    }
                }
                toolbarBinding.back.setOnClick {
                    performBack()
                }
                toolbarBinding.wtfTitle.setOnClick {
                }
                toolbarBinding.lifecycleOwner = viewLifecycleOwner
                toolbarBinding.actionBarItem = actionBarViewModel
                toolbarContainer.removeAllViews()
                toolbarContainer.addView(toolbarBinding.root)
            }
        }

        scaleLiveData.observe(viewLifecycleOwner) {
            view.pivotX = view.width / 2F
            view.pivotY = view.height / 2F
            view.scaleX = it
            view.scaleY = it
        }
    }

    fun pushFragment(id: Int, bundle: Bundle? = null, extras: FragmentNavigator.Extras? = null) {
        findNavController().navigate(
            id,
            bundle,
            NavOptions.Builder().setHorizontalSlide().build(),
            extras
        )
    }

    fun cropToRound() {
        val sender = requireView()
        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 20.pxValue.toFloat())
            }
        }
        sender.outlineProvider = outlineProvider
        sender.clipToOutline = true
        activity?.findViewById<View>(R.id.stack_bg)?.isVisible = true
    }

    fun backToSquare() {
        activity?.findViewById<View>(R.id.stack_bg)?.isVisible = false
        val sender = requireView()
        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 0F)
            }
        }
        sender.outlineProvider = outlineProvider
        sender.clipToOutline = true
    }

    open fun isAbandonedPage(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        if (TaskQueue.shouldBeInvoked != null) {
            TaskQueue.shouldBeInvoked?.invoke(this)
            TaskQueue.shouldBeInvoked = null
        }
    }

    open fun useDefaultBackground(): Boolean {
        return true
    }

    open fun onViewFirstCreated(view: View) {

    }

    open fun showFakeStatusBar(): Boolean {
        return true
    }

    override val actionbarContent: ActionBarViewModel get() = actionBarViewModel
}

fun Fragment.launchSuspend(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwnerLiveData.value?.lifecycleScope?.launch {
        block()
    }
}

fun Fragment.slinkyLaunchWhenResumed(block: suspend () -> Unit) {
    viewLifecycleOwnerLiveData.value?.lifecycleScope?.launchWhenResumed {
        block()
    }
}

fun Fragment.action(todo: suspend () -> Unit) {
    launchSuspend {
        val spinner = showSpinner()
        try {
            todo.invoke()
        } catch (ex: Exception) {
            handleError(ex)
        } finally {
            spinner.end()
        }
    }
}

fun Fragment.performBack() {
    val bottomSheet = findAncestorOrSelf<DialogFragment>()
    if (bottomSheet != null) {
        bottomSheet.dialog?.onBackPressed()
    } else {
        findNavController().popBackStack()
    }
}

fun buildItemAnimator(): SimpleItemAnimator? {
    val animator = OvershootInRightAnimator(0.5F)
    animator.addDuration = 300L
    animator.removeDuration = 300L
    animator.changeDuration = 300L
    animator.moveDuration = 300L
//    return animator
    return null
}

fun Fragment.hideKeyboard() {
    context?.hideKeyboard(activity?.window)
}

fun Context.hideKeyboard(window: Window?) {
    if (window != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }
}

interface ActionBarContainer {

    val actionbarContent: ActionBarViewModel
}

class NavFragmentViewModel(state: SavedStateHandle) : ViewModel() {

    val viewCreatedTime = state.getLiveData<Long>("viewCreatedTime")
}

