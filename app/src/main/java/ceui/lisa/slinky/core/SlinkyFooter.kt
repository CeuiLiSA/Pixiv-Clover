package ceui.lisa.slinky.core

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import ceui.lisa.slinky.databinding.ItemLoadingBinding
import ceui.lisa.slinky.ui.pxValue
import com.scwang.smart.refresh.footer.ClassicsFooter

class SlinkyFooter(context: Context, attrs: AttributeSet? = null) : ClassicsFooter(context, attrs) {

    val loadingBinding =
        ItemLoadingBinding.inflate(LayoutInflater.from(context), null, false).apply {
            loadingFrame.isVisible = true
            loadingFrame.updateLayoutParams {
                height = 100.pxValue
            }
            progressCircular.showProgress(true)

            emptyFrame.isVisible = false
            emptyFrame.updateLayoutParams {
                height = 100.pxValue
            }
        }

    override fun getView(): View {
        return loadingBinding.root
    }
}