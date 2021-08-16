package kr.fluxion.cordova.plugin.nfc

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import kr.fluxion.cordova.plugin.nfc.NFCTagDialogFragment.Companion.ENTRY_TAG_DATA
import kr.fluxion.cordova.plugin.nfc.NFCTagDialogFragment.Companion.ENTRY_TAG_ERROR
import kr.fluxion.cordova.plugin.nfc.device.BoardADL
import kr.fluxion.cordova.plugin.signpad.SignPadDialogFragment
import kr.fluxion.cordova.plugin.utils.findFragment
import kr.fluxion.fxdist.R
import kr.fluxion.fxdist.ui.MainFragmentDirections.Companion.actionNfc
import kr.fluxion.fxdist.utils.getCurrentFragment
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaWebView
import org.apache.cordova.SimpleCordovaPlugin
import org.json.JSONArray

class NFC : SimpleCordovaPlugin() {

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        activity.runOnUiThread {
            handleNavInteraction()
        }
    }

    override fun execute(args: JSONArray): Boolean = when (action) {
        ACTION_SHOW_NFC_TAG_VIEW -> activity.runOnUiThread {
            val type: String = args.getString(0)
            val currentPageIndex: Int = args.getInt(1)
            val totalPageCount: Int = args.getInt(2)
            showNFCTagDialog(type, currentPageIndex, totalPageCount)
        }.run { true }
        else -> false
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.action == "android.nfc.action.TECH_DISCOVERED") {
            activity.findFragment(NFCTagDialogFragment::class.java)?.onNFCDataReceived(intent)
                ?: run { Logger.w("Can not find NFCTagDialogFragment.") }
        }
    }

    private fun showNFCTagDialog(
        @NFCTagDialogFragment.TagType tagType: String, currentPageIndex: Int, totalPageCount: Int
    ): Boolean =
        activity.getCurrentFragment()?.findNavController()?.navigate(
            actionNfc(tagType, currentPageIndex, totalPageCount)
        ).run {
            Logger.d("TagType: $tagType >> $currentPageIndex/$totalPageCount")
            true
        }

    private fun handleNavInteraction(): Unit? = activity.getCurrentFragment()?.let { fragment ->
        with(fragment.findNavController().getBackStackEntry(R.id.flow_main_dest)) {
            LifecycleEventObserver { _, event ->
                event.takeIf { it == Lifecycle.Event.ON_RESUME }?.apply {
                    with(savedStateHandle) {
                        get<BoardADL.Data>(ENTRY_TAG_DATA)?.let { data ->
                            sendSuccessCallback("Success", getTagDataResult(data))
                        }.also { remove<String>(ENTRY_TAG_DATA) }

                        get<Int>(ENTRY_TAG_ERROR)?.let { code ->
                            sendErrorCallback(code, "")
                        }.also { remove<String>(ENTRY_TAG_ERROR) }
                    }
                }
            }.run {
                lifecycle.addObserver(this)
                fragment.viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                    event.takeIf { it == Lifecycle.Event.ON_DESTROY }?.apply {
                        lifecycle.removeObserver(this@run)
                    }
                })
            }
        }
    }

    private fun getTagDataResult(data: BoardADL.Data): String =
        JsonObject().apply {
            add("tagData", Gson().toJsonTree(data, BoardADL.Data::class.java))
        }.run { Gson().toJson(this) }

    companion object {
        const val ACTION_SHOW_NFC_TAG_VIEW = "showNFCTagView"
    }
}