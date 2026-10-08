package ceui.lisa.slinky.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.navArgs
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.DialogInfoBinding
import ceui.lisa.slinky.databinding.DialogInfoNewBinding
import ceui.lisa.slinky.ui.screenWidth
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.viewBinding
import ceui.lisa.slinky.utils.visibleOrGone
import kotlinx.coroutines.CompletableDeferred
import kotlin.math.roundToInt

suspend fun Fragment.alertNotice(title: String? = null, message: String): Boolean {
    val task = CompletableDeferred<Boolean>()
    val dialog = InfoDialog(task).apply {
        arguments = InfoDialogArgs(message, false, title).toBundle()
    }
    childFragmentManager.beginTransaction().add(dialog, "WTF").commitNowAllowingStateLoss()
    return task.await()
}

suspend fun Fragment.alertYesOrCancel(title: String? = null, message: String): Boolean {
    val task = CompletableDeferred<Boolean>()

    val rootBinding = DataBindingUtil.inflate<DialogInfoNewBinding>(
        layoutInflater,
        R.layout.dialog_info_new,
        null,
        false
    )
    val alertWidth = (screenWidth * 300 / 375F).roundToInt()
    val params = rootBinding.dialogLayout.layoutParams
    params.width = alertWidth
    rootBinding.dialogLayout.layoutParams = params
    if (title?.isNotEmpty() == true) {
        rootBinding.title.visibleOrGone = true
        rootBinding.title.text = title
    } else {
        rootBinding.title.visibleOrGone = false
    }
    rootBinding.message.text = message

    val dialogLayer = blurDialog(requireContext(), rootBinding.root)

    // cancel button
    rootBinding.cancelLayout.alertButton.text = getString(R.string.cancel)
    rootBinding.cancelLayout.alertButton.setTextColor(requireContext().getColor(R.color.destructive))
    rootBinding.cancelLayout.alertButton.setOnClick {
        task.complete(false)
        dialogLayer.dismiss()
    }

    // sure button
    rootBinding.sureLayout.alertButton.text = getString(R.string.button_sure)
    rootBinding.sureLayout.alertButton.setOnClick {
        task.complete(true)
        dialogLayer.dismiss()
    }


    return task.await()
}

suspend fun FragmentActivity.alertNotice(title: String? = null, message: String): Boolean {
    val task = CompletableDeferred<Boolean>()
    val dialog = InfoDialog(task).apply {
        arguments = InfoDialogArgs(message, false, title).toBundle()
    }
    dialog.show(supportFragmentManager, "InfoDialog")
    return task.await()
}

class InfoDialog(private val task: CompletableDeferred<Boolean>) :
    DialogFragment(R.layout.dialog_info) {

    private val binding by viewBinding(DialogInfoBinding::bind)
    private val safeArgs: InfoDialogArgs by navArgs()
    private var sureAction: Action? = null
    private var cancelAction: Action? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SlinkyAlert)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val alertWidth = (screenWidth * 300 / 375F).roundToInt()
        binding.root.updateLayoutParams {
            width = alertWidth
        }

        if (safeArgs.title?.isNotEmpty() == true) {
            binding.title.visibleOrGone = true
            binding.title.text = safeArgs.title
        } else {
            binding.title.visibleOrGone = false
        }

        binding.message.text = safeArgs.message


        binding.sureLayout.alertButton.text = getString(R.string.button_sure)
        binding.sureLayout.alertButton.setOnClick {
            sureAction?.handler?.invoke()
            sureAction = null
            task.complete(true)
            dismiss()
        }

        if (safeArgs.showCancel) {
            binding.cancelLayout.alertButton.text = getString(R.string.cancel)
            binding.cancelLayout.alertButton.setTextColor(requireContext().getColor(R.color.destructive))
            binding.cancelLayout.alertButton.setOnClick {
                task.complete(false)
                dismiss()
            }
        } else {
            binding.cancelLayout.root.visibility = View.GONE
        }
    }

    fun addCancelAction(handler: () -> Unit) {
        cancelAction = Action(handler)
    }

    fun addSureAction(handler: () -> Unit) {
        sureAction = Action(handler)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        cancelAction?.handler?.invoke()
    }
}

data class Action(val handler: () -> Unit)
