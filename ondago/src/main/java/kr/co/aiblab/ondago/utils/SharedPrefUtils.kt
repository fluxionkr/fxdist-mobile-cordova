package kr.co.aiblab.ondago.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kr.co.aiblab.ondago.data.LoginData

class UserDataPrefHelper private constructor(context: Context) {

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "${context.packageName}.$PREF_FILE_NAME",
            MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS).apply {
                setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            }.build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getLoginData(): LoginData = prefs.let {
        val userId = it.getString(PREF_KEY_LOGIN_INFO_USER_ID, "") ?: ""
        val password = it.getString(PREF_KEY_LOGIN_INFO_PASSWORD, "") ?: ""
        val isAutoLogin = it.getBoolean(PREF_KEY_LOGIN_INFO_AUTO_LOGIN, false)
        LoginData(userId, password, isAutoLogin)
    }

    fun putLoginData(loginData: LoginData): Unit = prefs.edit().apply {
        putString(PREF_KEY_LOGIN_INFO_USER_ID, loginData.userId)
        putString(PREF_KEY_LOGIN_INFO_PASSWORD, loginData.password)
        putBoolean(PREF_KEY_LOGIN_INFO_AUTO_LOGIN, loginData.isAutoLogin)
    }.apply()

    fun putUserInfo(
        userId: String, userKey: String, userName: String, userDiv: String, mmcmId: String
    ): Unit = prefs.edit().apply {
        putString(PREF_KEY_LOGIN_USER_ID, userId)
        putString(PREF_KEY_LOGIN_USER_KEY, userKey)
        putString(PREF_KEY_LOGIN_USER_NAME, userName)
        putString(PREF_KEY_LOGIN_USER_DIV, userDiv)
        putString(PREF_KEY_LOGIN_USER_MMCM_IC, mmcmId)
    }.apply()

    fun getUserId(): String = prefs.getString(PREF_KEY_LOGIN_USER_ID, "") ?: ""

    fun getUserKey(): String = prefs.getString(PREF_KEY_LOGIN_USER_KEY, "") ?: ""

    fun getUserName(): String = prefs.getString(PREF_KEY_LOGIN_USER_NAME, "") ?: ""

    fun getUserDiv(): String = prefs.getString(PREF_KEY_LOGIN_USER_DIV, "") ?: ""

    fun getUserMMCMId(): String = prefs.getString(PREF_KEY_LOGIN_USER_MMCM_IC, "") ?: ""

    fun getUserInfo(): Pair<String, String> = Pair(getUserId(), getUserKey())

    fun clearUserInfo(): Unit = prefs.edit().apply {
        remove(PREF_KEY_LOGIN_USER_ID)
        remove(PREF_KEY_LOGIN_USER_KEY)
        remove(PREF_KEY_LOGIN_USER_NAME)
        remove(PREF_KEY_LOGIN_USER_DIV)
        remove(PREF_KEY_LOGIN_USER_MMCM_IC)
    }.apply()

    fun clearLoginData(): Unit = prefs.edit().apply {
        remove(PREF_KEY_LOGIN_INFO_USER_ID)
        remove(PREF_KEY_LOGIN_INFO_PASSWORD)
        remove(PREF_KEY_LOGIN_INFO_AUTO_LOGIN)
    }.apply()

    companion object {

        private const val PREF_FILE_NAME = ".prefs_user_data"
        private const val PREF_KEY_LOGIN_USER_ID = "login_user_id"
        private const val PREF_KEY_LOGIN_USER_KEY = "login_user_key"
        private const val PREF_KEY_LOGIN_USER_NAME = "login_user_name"
        private const val PREF_KEY_LOGIN_USER_DIV = "login_user_division"
        private const val PREF_KEY_LOGIN_USER_MMCM_IC = "login_user_mmcm_id"
        private const val PREF_KEY_LOGIN_INFO_USER_ID = "login_info_id"
        private const val PREF_KEY_LOGIN_INFO_PASSWORD = "login_info_password"
        private const val PREF_KEY_LOGIN_INFO_AUTO_LOGIN = "login_info_auto_login"

        @Volatile
        private var instance: UserDataPrefHelper? = null
        fun getInstance(context: Context): UserDataPrefHelper = instance ?: synchronized(this) {
            instance ?: UserDataPrefHelper(context).also { instance = it }
        }
    }
}
