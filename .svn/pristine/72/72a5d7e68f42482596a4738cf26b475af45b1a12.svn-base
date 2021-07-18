package kr.co.kit.cordova.plugin.nfc

import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import kr.co.kit.cordova.plugin.nfc.NFCTagDialogFragment.TagType.Companion.READ_LOGS_DATA
import kr.co.kit.cordova.plugin.nfc.device.BoardADL.Companion.getMessage
import kr.co.kit.cordova.plugin.nfc.device.BoardADL.Status.Companion.FAILED_READ_DATA
import kr.co.kit.cordova.plugin.nfc.device.BoardADL.Status.Companion.NO_MORE_DATA
import kr.co.kit.cordova.plugin.nfc.device.D0201
import kr.co.kit.kitdist.databinding.FragmentDialogNfcBinding

class NFCTagDialogFragment : BottomSheetDialogFragment(), NFCDataListener {

    private val args: NFCTagDialogFragmentArgs by navArgs()

    private lateinit var binding: FragmentDialogNfcBinding

    private val viewModel: NFCViewModel by viewModels {
        NFCViewModelFactory(
            application = requireActivity().application,
            owner = this,
            repository = NFCRepository.getInstance(D0201())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.saveCurrentDeviceName(D0201.name)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogNfcBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            cancelButtonClickListener = View.OnClickListener {dialog?.cancel() }
        }
        context ?: return binding.root
        subscribeUI()
        return binding.root
    }

    override fun onNFCDataReceived(intent: Intent?) {
        (intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG) as Tag?)?.let {tag ->
            Logger.d("Tag = $tag")
            when (val type: String = args.tagType) {
                READ_LOGS_DATA -> viewModel.getData(tag, args.currentPageIndex, args.totalPageCount)
                else -> throw IllegalArgumentException("Unknown tag type has been received.[$type]")
            }
        } ?: run { Logger.w("Tag is null.") }
    }

    private fun subscribeUI() {
        viewModel.status.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { status ->
                status.getMessage(requireContext()).run {
                    updateStatusMessage(this)
                    Logger.d("##### Status >> $this")
                }
                when (status) {
                    NO_MORE_DATA, FAILED_READ_DATA -> updateProgress("")
                }
            }
        }

        viewModel.progress.observe(viewLifecycleOwner) {
            val currentPageIndex: Int = it.first
            val totalPageIndex: Int = it.second
            "$currentPageIndex/$totalPageIndex".run {
                updateProgress(this)
                Logger.i("##### Progress >> $this")
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { data ->
            Logger.d("##### Data: $data")
            with(findNavController()) {
                previousBackStackEntry?.savedStateHandle?.set(ENTRY_TAG_DATA, data)
                popBackStack()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        with(findNavController()) {
            previousBackStackEntry?.savedStateHandle?.set(ENTRY_TAG_ERROR, ErrorCode.CANCELED)
        }
        super.onCancel(dialog)
    }

    private fun updateStatusMessage(statusMessage: String) {
        binding.mtvInProgress.text = statusMessage
    }

    private fun updateProgress(progress: String) {
        binding.mtvProgress.text = progress
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class TagType {
        companion object {
            const val READ_LOGS_DATA = "read_logs_data"
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class ErrorCode {
        companion object {
            const val CANCELED = 4800
        }
    }

    companion object {
        const val ENTRY_TAG_DATA = "tag_logs_data"
        const val ENTRY_TAG_ERROR = "tag_error"
    }
}

interface NFCDataListener {
    fun onNFCDataReceived(intent: Intent?)
}