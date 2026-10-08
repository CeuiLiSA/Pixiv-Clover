package ceui.lisa.slinky.ui

import android.content.Context
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

abstract class SlinkyItem {

    open fun areItemsTheSame(other: SlinkyItem): Boolean {
        return this == other
    }

    open fun areContentsTheSame(other: SlinkyItem): Boolean {
        return this == other
    }

    fun getItemViewType(): Int {
        return this::class.java.hashCode()
    }

    private var onClickEvent: ((View) -> Unit)? = null

    fun bindOnClick(listener: (View) -> Unit): SlinkyItem {
        onClickEvent = listener
        return this
    }

    fun listener(): ((View) -> Unit)? {
        return onClickEvent
    }

    open fun getItemId(): Long {
        return -1
    }
}


open class SlinkyViewHolder<Binding : ViewDataBinding, T : SlinkyItem>(val binding: Binding) :
    RecyclerView.ViewHolder(binding.root) {

    protected val context: Context = binding.root.context
    protected lateinit var lifecycleOwner: LifecycleOwner

    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner): SlinkyViewHolder<Binding, T> {
        this.lifecycleOwner = lifecycleOwner
        return this
    }

    fun lifecycleOwnerBindView(item: T) {
        binding.lifecycleOwner = lifecycleOwner
        val listener = item.listener()
        if (listener != null) {
            binding.root.setOnClick { v ->
                listener.invoke(v)
            }
        }
        onBindViewHolder(item)
    }

    open fun onBindViewHolder(item: T) {

    }
}
