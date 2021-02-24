package kr.co.aiblab.cordova.plugin.navigation

import androidx.navigation.fragment.findNavController
import kr.co.aiblab.ondago.ui.MainFragmentDirections.Companion.actionLogout
import kr.co.aiblab.ondago.utils.UserDataPrefHelper
import kr.co.aiblab.ondago.utils.getCurrentFragment
import org.apache.cordova.SimpleCordovaPlugin
import org.json.JSONArray

class Navigation : SimpleCordovaPlugin() {

    override fun execute(args: JSONArray): Boolean = when (action) {
        ACTION_FINISH_APP -> activity.runOnUiThread { finishApp() }.run { true }
        ACTION_LOGOUT -> activity.runOnUiThread { logout() }.run { true }
        else -> false
    }

    private fun finishApp(): Boolean = with(activity) {
        runOnUiThread { finish() }.run { true }
    }

    private fun logout(): Boolean = with(activity) {
        getCurrentFragment()?.findNavController()?.navigate(actionLogout())
        UserDataPrefHelper.getInstance(context).clearLoginData()
    }.run { true }

    companion object {
        const val ACTION_FINISH_APP = "finishApp"
        const val ACTION_LOGOUT = "logout"
    }
}