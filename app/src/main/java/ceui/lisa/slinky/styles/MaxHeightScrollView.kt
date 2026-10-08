package ceui.lisa.slinky.styles

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import ceui.lisa.slinky.ui.screenHeight

class MaxHeightScrollView : NestedScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val max = screenHeight / 2
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(max, MeasureSpec.AT_MOST)
        )
    }
}