package kr.fluxion.fx.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kr.fluxion.fx.R
import kr.fluxion.fx.ui.ConfirmDialogFragmentArgs

class ConfirmDialogFragment : DialogFragment() {

    private val args: ConfirmDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).apply {
                if (TextUtils.isEmpty(args.title).not()) {
                    setTitle(args.title)
                }
                setMessage(args.message)
                if (args.isConfirmCancel) {
                    val negativeBtnTextLabel = args.btnTextNegative
                        ?: getString(R.string.alert_dialog_btn_label_cancel)
                    setNegativeButton(negativeBtnTextLabel) { _, _ -> dismiss(false) }
                }
                val positiveBtnTextLabel = args.btnTextPositive
                    ?: getString(R.string.alert_dialog_btn_label_confirm)
                setPositiveButton(positiveBtnTextLabel) { _, _ -> dismiss(true) }
                setCancelable(args.isCancelable)
            }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun dismiss(isConfirm: Boolean) {
        with(findNavController()) {
            val result = bundleOf(
                RESULT_KEY_ID to args.id,
                RESULT_KEY_IS_CONFIRM to isConfirm
            )
            previousBackStackEntry?.savedStateHandle?.set(ENTRY_KEY_CONFIRM, result)
            popBackStack()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        dismiss(false)
    }

    companion object {
        const val ENTRY_KEY_CONFIRM = "dialog_confirm"
        const val RESULT_KEY_IS_CONFIRM = "is_confirmed"
        const val RESULT_KEY_ID = "dialog_id"
    }
}