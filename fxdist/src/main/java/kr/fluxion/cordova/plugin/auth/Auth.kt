package kr.fluxion.cordova.plugin.auth

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kr.fluxion.cordova.plugin.utils.findFragment
import kr.fluxion.fxdist.data.UserData
import kr.fluxion.fxdist.ui.MainFragment
import org.apache.cordova.SimpleCordovaPlugin
import org.json.JSONArray

class Auth : SimpleCordovaPlugin() {

    override fun execute(args: JSONArray): Boolean = when (action) {
        ACTION_GET_LOGIN_INFO -> getUserInfo().run { true }
        else -> false
    }

    private fun getUserInfo() {
        activity.findFragment(MainFragment::class.java)?.getUserData()?.let {
            sendSuccessCallback("Success", getUserDataResult(it))
        } ?: run { sendErrorCallback(3000, "UserData is empty!")}
    }

    private fun getUserDataResult(userData: UserData): String =
        JsonObject().apply {
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            add(DATA_PROP_USER_DATA, gson.toJsonTree(userData, UserData::class.java))
        }.run { Gson().toJson(this) }

    companion object {
        const val ACTION_GET_LOGIN_INFO = "getLoginInfo"
        const val DATA_PROP_USER_DATA = "userData"
    }
}