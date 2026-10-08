package ceui.lisa.slinky.core

import androidx.recyclerview.widget.DiffUtil
import ceui.lisa.slinky.ui.SlinkyItem

object AdapterDiffer : DiffUtil.ItemCallback<SlinkyItem>() {
    override fun areItemsTheSame(oldItem: SlinkyItem, newItem: SlinkyItem): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(
        oldItem: SlinkyItem, newItem: SlinkyItem
    ): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }
}