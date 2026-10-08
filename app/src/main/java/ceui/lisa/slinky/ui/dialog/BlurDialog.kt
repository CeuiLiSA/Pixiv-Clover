package ceui.lisa.slinky.ui.dialog

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import ceui.lisa.slinky.R
import per.goweii.anylayer.AnyLayer
import per.goweii.anylayer.dialog.DialogLayer

fun blurDialog(context: Context, contentView: View): DialogLayer {
    val dialogLayer = AnyLayer.dialog(context)
    dialogLayer.contentView(contentView)
        .cancelableOnTouchOutside(false)
        .backgroundBlurPercent(0.02f)
        .backgroundColorInt(ContextCompat.getColor(context, R.color.transparent))
        .show()
    return dialogLayer
}

fun DialogLayer.end() {
    try {
        dismiss(true)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

