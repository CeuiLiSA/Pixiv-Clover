package ceui.lisa.slinky.styles

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.MiniTagCellBinding
import ceui.lisa.slinky.databinding.SmallTagCellBinding
import ceui.lisa.slinky.databinding.TagPieceBinding
import ceui.lisa.slinky.models.Tag
import ceui.lisa.slinky.ui.setOnClick
import com.google.android.flexbox.FlexboxLayout

class TagsFlowView(context: Context, attrs: AttributeSet?, defStyle: Int) :
    FlexboxLayout(context, attrs, defStyle) {


    object Style {
        val NORMAL = 0
        val SMALL = 1
    }


    private val style: Int
    private val cellClickable: Boolean
    private val tagsMaxLines: Int

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TagsFlowView)
        style = ta.getInt(R.styleable.TagsFlowView_tfv_style, Style.NORMAL)
        cellClickable = ta.getBoolean(R.styleable.TagsFlowView_tfv_cell_clickable, true)
        tagsMaxLines = ta.getInt(R.styleable.TagsFlowView_tfv_max_lines, 0)
        ta.recycle()
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val tagList = mutableListOf<Tag>()

    private var onInverseBindingListener: InverseBindingListener? = null

    private var onTagsChangedListener: (() -> Unit)? = null

    fun setOnTagsChangedListener(listener: () -> Unit) {
        onTagsChangedListener = listener
    }

    fun notifyChanged() {
        onInverseBindingListener?.onChange()
        onTagsChangedListener?.invoke()
    }

    fun setOnTagsChangedInverseBindingListener(listener: InverseBindingListener) {
        onInverseBindingListener = listener
    }

    private var onCellClickListener: ((cell: View, index: Int) -> Unit)? = null
    fun setOnCellClickListner(listner: (cell: View, index: Int) -> Unit) {
        onCellClickListener = listner
    }


    fun getTags(): List<Tag> {
        return tagList.toList()
    }

    private var nonCellCount = -1

    fun setTags(tags: List<Tag>?) {
        if (nonCellCount == -1) {
            nonCellCount = childCount
        }

        val newtagList = tags ?: listOf()
        if (tagList == newtagList) {
            return
        }

        tagList.clear()
        tagList.addAll(newtagList)

        if (childCount - nonCellCount < tagList.size) {
            for (i in 0 until tagList.size - (childCount - nonCellCount)) {
                addChild(i)
            }
        }


        for (i in 0 until childCount - nonCellCount) {
            val child = getChildAt(i)

            if (i < tagList.size) {
                setupChild(child)
            } else {
                child.visibility = View.GONE
            }
        }
    }

    private fun addChild(selfIndex: Int): View {
        val cell = when (style) {
            Style.SMALL -> {
                SmallTagCellBinding.inflate(LayoutInflater.from(context), this, false)
            }

            Style.NORMAL -> {
                TagPieceBinding.inflate(LayoutInflater.from(context), this, false)
            }

            else -> {
                MiniTagCellBinding.inflate(LayoutInflater.from(context), this, false)
            }
        }
        cell.root.setOnClick {
            onCellClickListener?.invoke(it, selfIndex)
        }
        addView(cell.root, childCount - nonCellCount)
        return cell.root
    }


    private fun setupChild(child: View) {
        val index = indexOfChild(child)
        val tag = tagList[index]

        val height = child.layoutParams.height.toFloat()

        val color = Color.parseColor(ColorRandom.randomColor(tag.name))

        val normal = ShapedDrawables.getRoundedRect(
            height / 2,
            context.resources.getDimension(R.dimen.tag_border_width),
            color,
            Color.TRANSPARENT
        )
        val selected = ShapedDrawables.getRoundedRect(height / 2, 0F, Color.TRANSPARENT, Color.BLUE)


        val selector = StateListDrawable()
        selector.addState(intArrayOf(android.R.attr.state_selected), selected)
        selector.addState(intArrayOf(android.R.attr.state_enabled), normal)

        child.background = selector
        val textView = child.findViewById<TextView>(R.id.hashtag_name)
        textView.text = tag.name?.trim()

        val selectedTextColor = Color.WHITE

        val colorSelector = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_enabled)
            ),
            intArrayOf(selectedTextColor, color)
        )

        textView.setTextColor(colorSelector)


        val poundView = child.findViewById<ImageView>(R.id.pound_image)
        poundView.imageTintList = colorSelector

        child.visibility = View.VISIBLE
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return !cellClickable
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (childCount > 0 && tagsMaxLines > 0) {
            val child = getChildAt(0)
            val childHeight = child.layoutParams.height
            val spacing = dividerDrawableVertical?.intrinsicHeight ?: 0

            val maxHeight = spacing * (tagsMaxLines - 1) + childHeight * tagsMaxLines

            if (measuredHeight > maxHeight)
                setMeasuredDimension(measuredWidth, maxHeight)
        }
    }
}


@BindingAdapter("tags")
fun TagsFlowView.binding_setTags(tags: List<Tag>?) {
    setTags(tags)
}

@InverseBindingAdapter(attribute = "tags", event = "onTagsChanged")
fun TagsFlowView.binding_getTags(): List<Tag> {
    return getTags()
}

@BindingAdapter("onTagsChanged")
fun TagsFlowView.binding_setOnTagsChangedListener(listener: InverseBindingListener) {
    setOnTagsChangedInverseBindingListener(listener)
}
