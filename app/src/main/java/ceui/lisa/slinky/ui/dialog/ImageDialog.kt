package ceui.lisa.slinky.ui.dialog

import androidx.fragment.app.DialogFragment
import ceui.lisa.slinky.R
import ceui.lisa.slinky.ui.NavFragment

class ImageDialog : DialogFragment(R.layout.dialog_image)

fun NavFragment.showImage(url: String) {
    val dialog = ImageDialog()
    dialog.show(childFragmentManager, "ImageDialog")
}