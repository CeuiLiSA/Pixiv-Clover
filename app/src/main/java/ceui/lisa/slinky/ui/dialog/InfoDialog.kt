package ceui.lisa.slinky.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.LayoutOptionsBinding
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.pxValue
import ceui.lisa.slinky.ui.setOnClick
import kotlinx.coroutines.CompletableDeferred
import per.goweii.layer.core.ktx.onShow
import per.goweii.layer.design.cupertino.CupertinoPopoverLayer
import per.goweii.layer.popup.PopupLayer
import per.goweii.popupshadowlayout.PopupShadowLayout
import per.goweii.layer.dialog.ktx.contentView


suspend fun Fragment.alertYesOrCancel(title: String? = null, message: String): Boolean {
    return requireActivity().alertYesOrCancel(title, message)
}

suspend fun FragmentActivity.alertYesOrCancel(title: String? = null, message: String): Boolean {
    val task = CompletableDeferred<Boolean>()
    val layer = SlinkyCupertinoAlertLayer(this)
        .contentView(R.layout.layer_design_cupertino_alert)
        .addAction(getString(R.string.button_sure)) { layer, _ ->
            task.complete(true)
            layer.dismiss()
        }
        .addAction(getString(R.string.cancel)) { layer, _ ->
            task.complete(false)
            layer.dismiss()
        }
    if (title?.isNotEmpty() == true) {
        layer.setTitle(title)
    }
    layer.setDesc(message)
    layer.show()

    return task.await()
}



suspend fun Fragment.alertTwoChoicesOrCancel(message: String, option1: String, option2: String): Int {
    return requireActivity().alertTwoChoicesOrCancel(message, option1, option2)
}

suspend fun FragmentActivity.alertTwoChoicesOrCancel(message: String, option1: String, option2: String): Int {
    val task = CompletableDeferred<Int>()
    val layer = SlinkyCupertinoAlertLayer(this)
        .contentView(R.layout.layer_design_cupertino_alert)
        .addAction(option1) { layer, _ ->
            task.complete(1)
            layer.dismiss()
        }
        .addAction(option2) { layer, _ ->
            task.complete(2)
            layer.dismiss()
        }
        .addAction(getString(R.string.cancel)) { layer, _ ->
            task.complete(0)
            layer.dismiss()
        }
    layer.setDesc(message)
    layer.show()

    return task.await()
}



suspend fun Fragment.alertNotice(title: String? = null, message: String): Boolean {
    return requireActivity().alertNotice(title, message)
}

suspend fun FragmentActivity.alertNotice(title: String? = null, message: String): Boolean {
    val task = CompletableDeferred<Boolean>()
    val layer = SlinkyCupertinoAlertLayer(this)
        .contentView(R.layout.layer_design_cupertino_alert)
        .addAction(getString(R.string.button_sure)) { layer, _ ->
            task.complete(true)
            layer.dismiss()
        }
    if (title?.isNotEmpty() == true) {
        layer.setTitle(title)
    }
    layer.setDesc(message)
    layer.show()

    return task.await()
}

class Action(val name: String, val block: () -> Unit)

fun NavFragment.showActionMenu(sender: View, actionsProducer: () -> List<Action>) {
    val inflater = LayoutInflater.from(requireContext())
    val layer = CupertinoPopoverLayer(sender)
    layer.contentView(R.layout.layout_options)
    layer.setUseDefaultConfig()
    // 默认按锚点居中，菜单宽度被锚点到屏幕边缘的距离卡住；锚点贴边（比如工具栏最右的按钮）
    // 时会被挤成一两个字宽，所以改成和锚点同侧对齐。箭头离菜单边缘要留出圆角的距离，
    // 指不到贴边的锚点，干脆不画。离边缘够远的锚点保持原来的居中样式。
    val anchorLocation = IntArray(2)
    sender.getLocationInWindow(anchorLocation)
    val anchorCenterX = anchorLocation[0] + sender.width / 2
    val windowWidth = sender.rootView.width
    val minHalfWidth = 80.pxValue
    if (windowWidth - anchorCenterX < minHalfWidth) {
        layer.setHorizontal(PopupLayer.Align.Horizontal.ALIGN_RIGHT)
        layer.setArrowSide(PopupShadowLayout.PopupShadowOutlineProvider.ARROW_SIDE_NONE)
    } else if (anchorCenterX < minHalfWidth) {
        layer.setHorizontal(PopupLayer.Align.Horizontal.ALIGN_LEFT)
        layer.setArrowSide(PopupShadowLayout.PopupShadowOutlineProvider.ARROW_SIDE_NONE)
    }
    layer.onShow {
        val optionsLayout = layer.viewHolder.content.findViewById<LinearLayout>(R.id.options_layout)
        val actions = actionsProducer()
        actions.forEach { action ->
            val tv = inflater.inflate(
                R.layout.layer_slinky_cupertino_alert_action,
                optionsLayout,
                false
            ) as TextView
            optionsLayout.addView(
                tv
            )
            tv.text = action.name
            tv.setOnClick {
                layer.dismiss()
                action.block()
            }
        }
    }
    layer.show()
}

fun EditText.moveCursorToEnd() {
    setSelection(text?.length ?: 0)
}