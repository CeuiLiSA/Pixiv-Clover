package ceui.lisa.slinky.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import ceui.lisa.slinky.R
import ceui.lisa.slinky.safeCall
import ceui.lisa.slinky.styles.ProgressImageButton
import ceui.lisa.slinky.ui.screenWidth
import kotlin.math.roundToInt

class SpinnerDialog : DialogFragment(R.layout.dialog_spinner) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SlinkyHud)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val minWidth = (screenWidth * 130 / 375F).roundToInt()
        view.minimumWidth = minWidth

        val progress = view.findViewById<ProgressImageButton>(R.id.progress_circular)
        progress.showProgress(true)
    }

    fun end() {
        safeCall { dismiss() }
    }
}

fun Fragment.showSpinner(): SpinnerDialog {
    return requireActivity().showSpinner()
}

fun FragmentActivity.showSpinner(): SpinnerDialog {
    val dialog = SpinnerDialog()
    dialog.show(supportFragmentManager, "FragmentActivity#SpinnerDialog")
    return dialog
}
