package kr.fluxion.cordova.plugin.signpad

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import kr.fluxion.cordova.plugin.signpad.SignPadDialogFragment.Companion.ENTRY_TAG_ERROR_CODE
import kr.fluxion.cordova.plugin.signpad.SignPadDialogFragment.Companion.ENTRY_TAG_SIGN_DATA
import kr.fluxion.fxdist.R
import kr.fluxion.fxdist.ui.MainFragmentDirections.Companion.actionSignPad
import kr.fluxion.fxdist.utils.getCurrentFragment
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaWebView
import org.apache.cordova.SimpleCordovaPlugin
import org.json.JSONArray

class SignPad : SimpleCordovaPlugin() {

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        activity.runOnUiThread {
            handleNavInteraction()
        }
    }

    override fun execute(args: JSONArray): Boolean = when (action) {
        // FIXME scale 값을 받아서 처리?
        ACTION_START_SIGNPAD -> activity.runOnUiThread { showSignPad() }.run { true }
        else -> false
    }

    private fun showSignPad(): Boolean = with(activity) {
        getCurrentFragment()?.findNavController()?.navigate(actionSignPad())
    }.run { true }

    private fun handleNavInteraction(): Unit? = activity.getCurrentFragment()?.let { fragment ->
        with(fragment.findNavController().getBackStackEntry(R.id.flow_main_dest)) {
            LifecycleEventObserver { _, event ->
                event.takeIf { it == Lifecycle.Event.ON_RESUME }?.apply {
                    with(savedStateHandle) {
                        get<String>(ENTRY_TAG_SIGN_DATA)?.let { signData ->
                            sendSuccessCallback(
                                context.getString(R.string.signpad_dialog_message_complete),
                                getSignDataResult(signData)
                            )
                        }.also { remove<String>(ENTRY_TAG_SIGN_DATA) }
                        get<Int>(ENTRY_TAG_ERROR_CODE)?.let {
                            Logger.d("Error: $it")
                            sendErrorCallback(it, "")
                        }.also { remove<Int>(ENTRY_TAG_ERROR_CODE) }
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

    private fun getSignDataResult(signData: String) =
        JsonObject().apply {
            addProperty("signData", signData)
        }.run { Gson().toJson(this) }

    companion object {
        const val ACTION_START_SIGNPAD = "startSignPad"
    }
}