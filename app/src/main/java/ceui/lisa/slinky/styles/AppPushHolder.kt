package ceui.lisa.slinky.styles

import android.view.View
import android.view.ViewGroup
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.AppPushHolderLayoutBinding
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.utils.visibleOrGone
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppPushData {
    val newMessage get() = _newMessage.asSharedFlow()
    private val _newMessage = MutableSharedFlow<AppPushMessage>()
    var count = 0

    suspend fun addMessage(message: AppPushMessage) {
        count++
        _newMessage.emit(message)
    }

    fun consumeOneMessage() {
        count--
    }
}

object PushDelayTime {
    const val INTERVAL = 50L
    const val MUST = 1500L
    const val EXTEND = 1000L
}

data class AppPushMessage(
    val title: String? = null,
    val body: String? = null,
    val type: Int? = 0
)

class AppPushHolder(val message: AppPushMessage) : SlinkyItem()

@ItemHolder(AppPushHolder::class)
class AppPushViewHolder(aa: AppPushHolderLayoutBinding) :
    SlinkyViewHolder<AppPushHolderLayoutBinding, AppPushHolder>(aa) {

    override fun onBindViewHolder(item: AppPushHolder) {
        super.onBindViewHolder(item)
        val spec = View.MeasureSpec.makeMeasureSpec(
            View.MeasureSpec.getSize(ViewGroup.LayoutParams.WRAP_CONTENT),
            View.MeasureSpec.UNSPECIFIED
        )
        binding.title.text = item.message.title
        binding.title.visibleOrGone = !item.message.title.isNullOrEmpty()
        binding.content.text = item.message.body
        binding.content.visibleOrGone = !item.message.body.isNullOrEmpty()
        binding.pushView.measure(spec, spec)
        val height = binding.pushView.measuredHeight.toFloat()
        val background = ShapedDrawables.getRoundedRect(
            height / 2,
            binding.root.resources.getDimension(R.dimen.hair_line_width),
            context.getColor(R.color.colorWhite30),
            context.getColor(R.color.colorBlack95)
        )
        binding.pushView.background = background
    }
}