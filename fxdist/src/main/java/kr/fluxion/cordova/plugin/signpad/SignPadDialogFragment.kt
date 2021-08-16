package kr.fluxion.cordova.plugin.signpad

import android.graphics.Bitmap
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.github.gcacace.signaturepad.views.SignaturePad
import com.orhanobut.logger.Logger
import kr.fluxion.cordova.plugin.signpad.SignPadDialogFragment.ErrorCode.Companion.CANCELED
import kr.fluxion.cordova.plugin.signpad.SignPadDialogFragment.ErrorCode.Companion.NOT_SIGNED
import kr.fluxion.fxdist.R
import kr.fluxion.fxdist.databinding.FragmentDialogSignpadBinding
import kr.fluxion.fxdist.utils.getBase64

class SignPadDialogFragment : DialogFragment(), SignaturePad.OnSignedListener {

    private lateinit var binding: FragmentDialogSignpadBinding
    private lateinit var signPad: SignaturePad

    private var isSigned: Boolean = false

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.AppTheme)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogSignpadBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            signPad = signaturePad.apply { setOnSignedListener(this@SignPadDialogFragment) }
            setCancelClickListener { sendError(CANCELED).also { dismissSignPad() } }
            setConfirmClickListener {
                try {
                    checkValidSignData(isSigned)
                    getSign().also { dismissSignPad() }
                } catch (e: IllegalStateException) {
                    Logger.w("${e.message}")
                    e.message?.let { sendError(it.toInt()).also { dismissSignPad() } }
                }
            }
            isCancelable = false
        }
        context ?: return binding.root
        return binding.root
    }

    @Throws(IllegalStateException::class)
    private fun checkValidSignData(isSigned: Boolean) {
        check(isSigned) { NOT_SIGNED }
    }

    private fun getSign(): Unit = signPad.signatureBitmap.also {
        Formatter.formatFileSize(requireContext(), (it.width * it.height).toLong()).run {
            Logger.d("originImageSize: $this")
        }
    }.run {
        val scaledWidth: Int = width / 4
        val scaledHeight: Int = height / 4
        Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, false)
    }.getBase64().run {
        Formatter.formatFileSize(requireContext(), this.length.toLong()).run {
            Logger.d("ScaledImageSize: $this")
        }
        findNavController().previousBackStackEntry?.savedStateHandle
            ?.set(ENTRY_TAG_SIGN_DATA, this)
    }

    private fun sendError(code: Int) {
        findNavController().previousBackStackEntry?.savedStateHandle
            ?.set(ENTRY_TAG_ERROR_CODE, code)
    }

    private fun dismissSignPad() = findNavController().popBackStack()

    override fun onStart() {
        super.onStart()
//        activity?.requestedOrientation = SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        val width = resources.getDimensionPixelSize(R.dimen.popup_width);
        val height = resources.getDimensionPixelSize(R.dimen.popup_height);
        dialog?.window?.setLayout(width, height);
    }

//    override fun onStop() {
//        super.onStop()
//        activity?.requestedOrientation = SCREEN_ORIENTATION_SENSOR_PORTRAIT
//    }

    override fun onStartSigning() {}

    override fun onSigned() {
        isSigned = true
    }

    override fun onClear() {
        isSigned = false
    }

    companion object {
        const val ENTRY_TAG_SIGN_DATA = "sign_data"
        const val ENTRY_TAG_ERROR_CODE = "error_code"
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class ErrorCode {
        companion object {
            const val CANCELED = 4601
            const val NOT_SIGNED = 4602
        }
    }
}