package ceui.lisa.slinky.styles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import ceui.lisa.slinky.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ShrinkLayout : HStack {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyle,
        defStyleRes
    )

}


abstract class StackLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int,
    defStyleRes: Int
) : ViewGroup(context, attrs, defStyle, defStyleRes) {

    protected var _spacing: Int
    protected var _gravity: Int

    var spacing
        get() = _spacing
        set(value) {
            if (_spacing == value)
                return

            _spacing = value
            requestLayout()
        }


    var gravity
        get() = _gravity
        set(value) {
            if (_gravity == value)
                return

            _gravity = value
            requestLayout()
        }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.StackLayout)
        _spacing = ta.getDimension(R.styleable.StackLayout_spacing, 0F).roundToInt()
        _gravity = ta.getInt(R.styleable.StackLayout_android_gravity, Gravity.TOP or Gravity.START)
        ta.recycle()
    }

    fun updateSpacing(s: Int) {
        _spacing = s
    }

    constructor(context: Context) : this(context, null, 0, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : this(
        context,
        attrs,
        defStyle,
        0
    )


    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams? {
        return LayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams? {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams? {
        return LayoutParams(p)
    }

    // Override to allow type-checking of LayoutParams.
    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LinearLayout.LayoutParams
    }

    class LayoutParams : LinearLayout.LayoutParams {

        var compressionPriority: Float = 0F

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.StackLayout_Layout)
            compressionPriority =
                ta.getFloat(R.styleable.StackLayout_Layout_layout_stack_compression_priority, 0F)
            ta.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(width: Int, height: Int, weight: Float) : super(width, height, weight)

        constructor(p: ViewGroup.LayoutParams?) : super(p)

        constructor(source: LinearLayout.LayoutParams?) : super(source)

        constructor(source: LayoutParams) : super(source) {
            compressionPriority = source.compressionPriority
        }

    }
}

class VisibleChildren(viewGroup: ViewGroup) {
    private val children = viewGroup.children.filter { it.visibility != View.GONE }.toList()
    fun getChildAt(position: Int): View {
        return children[position]
    }

    var childCount: Int = children.size

    fun getCompressionPrioritizedIndexesSmallToLarge(): List<Int> {
        return IntArray(childCount) { it }.sortedWith(compareBy {
            (getChildAt(it).layoutParams as? StackLayout.LayoutParams)?.compressionPriority ?: 0F
        })
    }
}

fun measureChildNoPadding(
    child: View,
    parentWidthMeasureSpec: Int,
    parentHeightMeasureSpec: Int
) {
    val lp = child.layoutParams
    val childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(
        parentWidthMeasureSpec,
        0, lp.width
    )
    val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(
        parentHeightMeasureSpec,
        0, lp.height
    )
    child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
}

fun View.getLayoutMargins(): Rect {
    val lp = (layoutParams as? ViewGroup.MarginLayoutParams) ?: return Rect()
    return Rect(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin)
}

fun View.getLayoutWeight(): Float {
    return (layoutParams as? LinearLayout.LayoutParams)?.weight ?: 0.0f
}

open class HStack(context: Context, attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) :
    StackLayout(context, attrs, defStyle, defStyleRes) {


    constructor(context: Context) : this(context, null, 0, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : this(
        context,
        attrs,
        defStyle,
        0
    )


    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val children = VisibleChildren(this)

        when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                var restWidth = widthSize - paddingLeft - paddingRight
                val weightedIndexes = children.getCompressionPrioritizedIndexesSmallToLarge()
                for (index in weightedIndexes) {

                    val child = children.getChildAt(index)
                    val margins = child.getLayoutMargins()
                    val childHeightSpec = MeasureSpec.makeMeasureSpec(
                        heightSize - paddingTop - paddingBottom - margins.top - margins.bottom,
                        heightMode
                    )

                    measureChildNoPadding(
                        child,
                        MeasureSpec.makeMeasureSpec(
                            max(
                                0,
                                restWidth - margins.left - margins.right
                            ), MeasureSpec.AT_MOST
                        ), childHeightSpec
                    )

                    restWidth -= (child.measuredWidth + _spacing + margins.left + margins.right)
                }
            }

            else -> {
                for (i in 0 until children.childCount) {
                    val child = children.getChildAt(i)
                    val margins = child.getLayoutMargins()
                    val childHeightSpec = MeasureSpec.makeMeasureSpec(
                        heightSize - paddingTop - paddingBottom - margins.top - margins.bottom,
                        heightMode
                    )
                    measureChildNoPadding(child, widthMeasureSpec, childHeightSpec)
                }
            }
        }

        var myWidth = 0
        var myHeight = 0

        for (i in 0 until children.childCount) {
            val child = children.getChildAt(i)
            val margins = child.getLayoutMargins()
            myHeight = max(myHeight, child.measuredHeight + margins.top + margins.bottom)
            myWidth += child.measuredWidth + margins.left + margins.right
            if (i > 0)
                myWidth += _spacing
        }

        myWidth += paddingLeft + paddingRight
        myHeight += paddingTop + paddingBottom

        myHeight = max(this.minimumHeight, myHeight)

        when (widthMode) {
            MeasureSpec.AT_MOST -> myWidth = min(myWidth, widthSize)
            MeasureSpec.EXACTLY -> {
                val surplus = widthSize - myWidth
                var totalWeight = 0F
                val validWeightedsIndexes = mutableListOf<Int>()
                for (index in 0 until children.childCount) {
                    val weight = children.getChildAt(index).getLayoutWeight()
                    if (weight > 0) {
                        validWeightedsIndexes.add(index)
                        totalWeight += weight
                    }
                }
                for (index in validWeightedsIndexes) {
                    val child = children.getChildAt(index)
                    val newWidth =
                        (child.measuredWidth + surplus * child.getLayoutWeight() / totalWeight).roundToInt()
                    val widthModeV2 =
                        if (child.layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) MeasureSpec.EXACTLY else MeasureSpec.AT_MOST

                    val margins = child.getLayoutMargins()
                    val childHeightSpec = MeasureSpec.makeMeasureSpec(
                        heightSize - paddingTop - paddingBottom - margins.top - margins.bottom,
                        heightMode
                    )
                    measureChildNoPadding(
                        child,
                        MeasureSpec.makeMeasureSpec(newWidth, widthModeV2),
                        childHeightSpec
                    )
                }

                myWidth = widthSize
            }

            MeasureSpec.UNSPECIFIED -> {
            }
        }

        when (heightMode) {
            MeasureSpec.AT_MOST -> myHeight = min(myHeight, heightSize)
            MeasureSpec.EXACTLY -> myHeight = heightSize
            MeasureSpec.UNSPECIFIED -> {
            }
        }

        setMeasuredDimension(myWidth, myHeight)
    }


    @SuppressLint("RtlHardcoded", "DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val totalWidth = r - l
        val totalHeight = b - t

        val children = VisibleChildren(this)

        val isRtl = (layoutDirection == LAYOUT_DIRECTION_RTL)

        var extraLeft = 0
        if (isRtl || (!isRtl && (_gravity and Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT)) {
            var childTotalWidth = 0
            for (i in 0 until children.childCount) {
                val child = children.getChildAt(i)
                val margins = child.getLayoutMargins()
                childTotalWidth += margins.left + child.measuredWidth + margins.right
                if (i > 0)
                    childTotalWidth += _spacing
            }

            if (childTotalWidth < (totalWidth - paddingLeft - paddingRight)) {
                extraLeft = (totalWidth - paddingLeft - paddingRight) - childTotalWidth
            }
        }


        var x = paddingLeft + extraLeft
        for (i in 0 until children.childCount) {
            val child =
                if (!isRtl) children.getChildAt(i) else children.getChildAt(children.childCount - i - 1)

            if (i > 0)
                x += _spacing


            var childGravity = (child.layoutParams as? LinearLayout.LayoutParams)?.gravity ?: -1
            if (childGravity == -1) {
                childGravity = this._gravity
            }
            childGravity = childGravity and Gravity.VERTICAL_GRAVITY_MASK

            val margins = child.getLayoutMargins()
            val childWithMarginTop = when (childGravity) {
                Gravity.TOP -> paddingTop
                Gravity.BOTTOM -> totalHeight - paddingBottom - (child.measuredHeight + margins.top + margins.bottom)
                Gravity.CENTER_VERTICAL, Gravity.CENTER -> (totalHeight - paddingTop - paddingBottom - (child.measuredHeight + margins.top + margins.bottom)) / 2 + paddingTop
                else -> 0
            }

            if (child.measuredWidth > 0) {
                child.layout(
                    x + margins.left,
                    childWithMarginTop + margins.top,
                    x + margins.left + child.measuredWidth,
                    childWithMarginTop + margins.top + child.measuredHeight
                )

                x += (margins.left + child.measuredWidth + margins.right)
            } else {
                child.layout(
                    x,
                    childWithMarginTop + margins.top,
                    x + child.measuredWidth,
                    childWithMarginTop + margins.top + child.measuredHeight
                )

                x += 0

            }
        }
    }


    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        requestLayout()
    }
}
