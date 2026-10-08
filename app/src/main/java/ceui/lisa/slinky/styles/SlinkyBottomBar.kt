package ceui.lisa.slinky.styles

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.BottomBarBinding
import ceui.lisa.slinky.databinding.BottomBarItemBinding
import ceui.lisa.slinky.ui.setOnClick


class SlinkyBottomBar : FrameLayout {

    private lateinit var binding: BottomBarBinding
    private var selectedItemIndex = MutableLiveData(0)
    private var onTabSelected: OnTabSelected? = null
    private val items = mutableListOf<BottomItem>()
    private var start: Float = 0F
    private var itemWidth: Float = 0F

    constructor(context: Context) : super(context) {
        loadBinding()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        loadBinding()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        loadBinding()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        loadBinding()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putInt("stuff", selectedItemIndex.value ?: 0) // ... save stuff
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            selectedItemIndex.value = state.getInt("stuff") // ... load stuff
            super.onRestoreInstanceState(state.getParcelable("superState"))
        }
    }

    fun setItems(lifecycleOwner: LifecycleOwner, list: List<BottomItem>) {
        binding.lifecycleOwner = lifecycleOwner

        if (items.size > 0) {
            items.clear()
        }

        if (list.isNotEmpty()) {
            items.addAll(list)
            loadView()
        }
    }

    private fun loadBinding() {
        binding =
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_bar, this, true)
    }

    private fun loadView() {
        if (items.isNotEmpty()) {
            binding.container.removeAllViews()
            items.forEachIndexed { index, item ->
                val lifecycleOwner = binding.lifecycleOwner
                val itemView = DataBindingUtil.inflate<BottomBarItemBinding>(
                    LayoutInflater.from(context),
                    R.layout.bottom_bar_item,
                    null,
                    false
                )
                if (lifecycleOwner != null) {
                    selectedItemIndex.observe(lifecycleOwner) { selectedIndex ->
                        if (selectedIndex == index) {
                            itemView.title.setTextColor(context.getColor(R.color.purple_700))
                        } else {
                            itemView.title.setTextColor(context.getColor(R.color.purple_200))
                        }
                    }
                }

                itemView.title.text = item.title
                itemView.titleFrame.setOnClick {
                    if (selectedItemIndex.value == index) {
                        onTabSelected?.onReSelected(index)
                        return@setOnClick
                    }

                    selectedItemIndex.value = index
                    onTabSelected?.onSelected(index)
                }

                binding.container.addView(
                    itemView.root,
                    LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1F
                    )
                )
            }
        }
    }

    fun updatePageScrolled(float: Float) {
    }

    fun updateSelectedIndex(index: Int) {
        if (selectedItemIndex.value == index) {
            return
        }

        selectedItemIndex.value = index
    }

    fun setOnTabSelectedListener(listener: OnTabSelected) {
        onTabSelected = listener
    }

    interface OnTabSelected {
        fun onSelected(index: Int)
        fun onReSelected(index: Int)
    }
}

data class BottomItem(val title: String)