package ceui.lisa.slinky.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.DialogOperationSuccessBinding
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.viewBinding

class SuccessDialog : DialogFragment(R.layout.dialog_operation_success) {

    private val binding by viewBinding(DialogOperationSuccessBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SlinkyPrompt)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.okButton.setOnClick { dismiss() }
    }

    companion object {
        const val TAG = "SuccessDialog"
    }
}