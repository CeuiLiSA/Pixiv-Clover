package ceui.lisa.slinky.ui.dialog

import android.app.Activity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import ceui.lisa.slinky.R
import per.goweii.layer.design.cupertino.CupertinoAlertLayer

class SlinkyCupertinoAlertLayer(activity: Activity) : CupertinoAlertLayer(activity) {

    override fun onCreateAction(inflater: LayoutInflater, parent: LinearLayout): TextView {
        return inflater.inflate(
            R.layout.layer_slinky_cupertino_alert_action,
            parent,
            false
        ) as TextView
    }
}