package ceui.lisa.slinky.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updatePadding
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import ceui.lisa.slinky.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.max

data class InsetState(val margin: Rect, val padding: Rect) {
    companion object {
        fun create(view: View): InsetState {
            val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
            return InsetState(
                if (lp != null) Rect(
                    lp.leftMargin,
                    lp.topMargin,
                    lp.rightMargin,
                    lp.bottomMargin
                ) else Rect(0, 0, 0, 0),
                Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
            )
        }
    }
}

class BottomBarContainer(context: Context, attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) :
    FrameLayout(context, attrs, defStyle, defStyleRes) {
    constructor(context: Context) : this(context, null, 0, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : this(
        context,
        attrs,
        defStyle,
        0
    )
}

open class InsetLayout(context: Context, attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) :
    FrameLayout(context, attrs, defStyle, defStyleRes) {

    constructor(context: Context) : this(context, null, 0, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : this(
        context,
        attrs,
        defStyle,
        0
    )

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        if (child != null) {
            val lp = child.layoutParams as? LayoutParams
            if (lp != null) {
                val marginTarget = child.findViewById(lp.marginTargetId) ?: child
                val paddingTarget = child.findViewById(lp.paddingTargetId) ?: child

                paddingTarget.setTag(R.id.initial_insets, InsetState.create(paddingTarget))
                if (paddingTarget != marginTarget) {
                    marginTarget.setTag(R.id.initial_insets, InsetState.create(marginTarget))
                }

                if (lp.barType == LayoutParams.BarType.BOTTOM) {
                    lp.gravity = Gravity.BOTTOM
                } else if (lp.barType == LayoutParams.BarType.TOP) {
                    lp.gravity = Gravity.TOP
                }
            }
        }
    }

    object BarHelper {

        private fun getThisBarInsets(viewGroup: ViewGroup, keyboardOpened: Boolean): Rect {

            when (viewGroup) {
                is InsetLayout -> {
                    var topBarInset = 0
                    var bottomBarInset = 0
                    for (i in 0 until viewGroup.childCount) {
                        val child = viewGroup.getChildAt(i)
                        val lp = child.layoutParams as? LayoutParams
                        if (lp != null) {
                            // if bottom bar height > keyboard height and ignoresKeyboard, the bottom insets will be incorrect
                            if (lp.barType == LayoutParams.BarType.TOP || (lp.barType == LayoutParams.BarType.BOTTOM && (!keyboardOpened || !lp.ignoresKeyboard))) {
                                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                                if (lp.barType == LayoutParams.BarType.TOP) {
                                    topBarInset =
                                        max(topBarInset, (child.measuredHeight - child.paddingTop))
                                } else {
                                    bottomBarInset = max(
                                        bottomBarInset,
                                        (child.measuredHeight - child.paddingBottom)
                                    )
                                }
                            }
                        }
                    }

                    return Rect(0, topBarInset, 0, bottomBarInset)
                }

                is CoordinatorLayout -> {
                    var bottomBarInset = 0
                    for (i in 0 until viewGroup.childCount) {
                        val child = viewGroup.getChildAt(i)
                        if (child is BottomBarContainer) {
                            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                            bottomBarInset =
                                max(bottomBarInset, (child.measuredHeight - child.paddingBottom))
                        }
                    }
                    return Rect(0, 0, 0, bottomBarInset)
                }

                else -> {
                    return Rect(0, 0, 0, 0)
                }
            }
        }

        fun getOverallBarInsets(viewGroup: ViewGroup, keyboardOpened: Boolean): Rect {
            val thisInsets = getThisBarInsets(viewGroup, keyboardOpened)

            val parent = viewGroup.parent as? ViewGroup ?: return thisInsets

            val otherInsets = getOverallBarInsets(parent, keyboardOpened)
            return Rect(
                0,
                max(thisInsets.top, otherInsets.top),
                0,
                max(thisInsets.bottom, otherInsets.bottom)
            )
        }

    }

    private var _currentTopInset: Int = 0
    private var _currentBottomInset: Int = 0

    val currentTopInset: Int get() = _currentTopInset
    val currentBottomInset: Int get() = _currentBottomInset


    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets {
        val fragment = findFragmentOrNull<Fragment>()
        val isBottomSheet = fragment?.findAncestorOrSelf<BottomSheetDialogFragment>() != null

        if (insets != null) {
            val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets, this)
            val systemInsets = compatInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val imeInsets = compatInsets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardOpened = compatInsets.isVisible(WindowInsetsCompat.Type.ime())
            val bottomInset = max(systemInsets.bottom, imeInsets.bottom)
            _currentTopInset = systemInsets.top
            _currentBottomInset = bottomInset

            val barInsets = BarHelper.getOverallBarInsets(this, keyboardOpened)

            val isIgnoreInset = shouldIgnoreInset()
            val isEmbeddedInViewPager = getIsEmbeddedInViewPager()
            val isAttachedToWindow = isAttachedToWindow
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val lp = child.layoutParams as? LayoutParams ?: continue
                val childBottomInset = if (lp.ignoresKeyboard) systemInsets.bottom else bottomInset

                val ignoreTopBarInset = lp.barType == LayoutParams.BarType.TOP
                val ignoreBottomBarInset = lp.barType == LayoutParams.BarType.BOTTOM

                val marginTarget = child.findViewById(lp.marginTargetId) ?: child

                val ignoreAllTops =
                    isIgnoreInset || isEmbeddedInViewPager || !isAttachedToWindow || isBottomSheet

                if (marginTarget.layoutParams is MarginLayoutParams) {
                    var marginChanged = false
                    val initial = marginTarget.getTag(R.id.initial_insets) as InsetState
                    if ((lp.marginAdjust and LayoutParams.EdgeMask.TOP > 0) && !ignoreAllTops) {
                        (marginTarget.layoutParams as MarginLayoutParams).topMargin =
                            initial.margin.top + systemInsets.top + (if (ignoreTopBarInset) 0 else barInsets.top)
                        marginChanged = true
                    }

                    if (lp.marginAdjust and LayoutParams.EdgeMask.BOTTOM > 0 && !isIgnoreInset) {
                        (marginTarget.layoutParams as MarginLayoutParams).bottomMargin =
                            initial.margin.bottom + childBottomInset + (if (ignoreBottomBarInset) 0 else barInsets.bottom)
                        marginChanged = true
                    }

                    if (marginChanged) {
                        marginTarget.requestLayout() // Android 11 doesn't relayout after change the margin
                    }
                }

                if (lp.paddingAdjust > 0) {
                    val paddingTarget = child.findViewById(lp.paddingTargetId) ?: child
                    val initial = paddingTarget.getTag(R.id.initial_insets) as InsetState

                    var paddingTop = initial.padding.top
                    var paddingBottom = initial.padding.bottom
                    if ((lp.paddingAdjust and LayoutParams.EdgeMask.TOP > 0) && !ignoreAllTops) {
                        paddingTop += systemInsets.top + (if (ignoreTopBarInset) 0 else barInsets.top)
                    }

                    if (lp.paddingAdjust and LayoutParams.EdgeMask.BOTTOM > 0 && !isIgnoreInset) {
                        paddingBottom += childBottomInset + (if (ignoreBottomBarInset) 0 else barInsets.bottom)
                    }

                    paddingTarget.updatePadding(top = paddingTop, bottom = paddingBottom)
                }
            }
        }

        return super.onApplyWindowInsets(insets)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams? {
        return LayoutParams(context, attrs)
    }

    class LayoutParams(context: Context, attrs: AttributeSet?) :
        FrameLayout.LayoutParams(context, attrs) {
        object BarType {
            const val NONE = 0
            const val TOP = 1
            const val BOTTOM = 2
        }

        object EdgeMask {
            const val NONE = 0
            const val TOP = 1
            const val BOTTOM = 1 shl 1
        }

        val barType: Int
        var ignoresKeyboard: Boolean
        var marginAdjust: Int
        var paddingAdjust: Int
        val paddingTargetId: Int
        val marginTargetId: Int

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.InsetLayout_Layout)
            barType = ta.getInteger(
                R.styleable.InsetLayout_Layout_layout_inset_bar_type,
                BarType.NONE
            )
            ignoresKeyboard =
                ta.getBoolean(R.styleable.InsetLayout_Layout_layout_inset_ignores_keyboard, false)
            marginAdjust = ta.getInteger(
                R.styleable.InsetLayout_Layout_layout_inset_adjust_margin,
                EdgeMask.NONE
            )
            paddingAdjust = ta.getInteger(
                R.styleable.InsetLayout_Layout_layout_inset_adjust_padding,
                EdgeMask.NONE
            )
            marginTargetId = ta.getResourceId(
                R.styleable.InsetLayout_Layout_layout_inset_margin_adjust_target,
                View.NO_ID
            )
            paddingTargetId = ta.getResourceId(
                R.styleable.InsetLayout_Layout_layout_inset_padding_adjust_target,
                View.NO_ID
            )
            ta.recycle()
        }
    }
}

private fun View.getIsEmbeddedInViewPager(): Boolean {
    return findAncestorOrNull<ViewPager2>() != null
}

private fun View.shouldIgnoreInset(): Boolean {
    return findAncestorOrNull<IgnoreInsetLayout>() != null
}

inline fun <reified T : View> View.findAncestorOrNull(): T? {
    var itr = this.parent
    while (itr != null) {
        if (itr is T) {
            return itr
        }
        itr = itr.parent
    }
    return null
}

inline fun <reified T : View> View.findAncestorOrSelfOrNull(): T? {
    var itr = this as? ViewParent
    while (itr != null) {
        if (itr is T) {
            return itr
        }
        itr = itr.parent
    }
    return null
}


class IgnoreInsetLayout : FrameLayout {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, style: Int) : super(context, attrs, style)
}
