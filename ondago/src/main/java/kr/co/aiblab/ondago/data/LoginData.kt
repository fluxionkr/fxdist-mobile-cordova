package kr.co.aiblab.ondago.data

import com.google.gson.annotations.SerializedName

data class LoginData(
    val userId: String,
    val password: String,
    val isAutoLogin: Boolean
)

data class LoginReqBody(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_pass") val password: String
)

@Suppress("unused")
open class LoginResult(val isLoginSuccess: Boolean)

data class LoginResultSuccess(val userData: UserData) : LoginResult(true)

data class LoginResultFailed(val error: Error) : LoginResult(false)