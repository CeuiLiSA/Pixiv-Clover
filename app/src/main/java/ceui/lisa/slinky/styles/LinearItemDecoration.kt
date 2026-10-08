package ceui.lisa.slinky.styles

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class LinearItemDecoration(
    private val space: Int,
    private val withLeftRight: Boolean = true,
    private val withTop: Boolean = true
) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        if (withLeftRight) {
            outRect.left = space
            outRect.right = space
        }

        outRect.bottom = space

        if (withTop) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = space
            }
        }
    }
}

class LinearItemHorizontalDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.right = space
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = space
        }
    }
}