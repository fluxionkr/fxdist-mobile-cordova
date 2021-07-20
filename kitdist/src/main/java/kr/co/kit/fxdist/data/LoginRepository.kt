package kr.co.kit.kitdist.data

import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.kit.kitdist.api.ServiceAPI
import kr.co.kit.kitdist.utils.Event
import kr.co.kit.kitdist.utils.UserDataPrefHelper
import java.io.IOException

class LoginRepository private constructor(private val userDataPrefs: UserDataPrefHelper) {

    val userData = MutableLiveData<Event<UserData>>()

    val networkState = MutableLiveData<Event<NetworkState>>()
    val error = MutableLiveData<Event<Error>>()

    suspend fun autoLogin(): Unit = withContext(Dispatchers.IO) {
        with(getStoredLoginData()) {
            if (isAutoLogin) doLogin(this)
            else Logger.w("AutoLogin is off.")
        }
    }

    suspend fun login(loginData: LoginData): Unit = withContext(Dispatchers.IO) {
        doLogin(loginData)
        if (loginData.isAutoLogin) {
            storeLoginData(loginData)
        } else {
            clearStoredLoginData()
        }
    }

    private suspend fun doLogin(loginData: LoginData): Unit = withContext(Dispatchers.IO) {
        try {
            sendNetworkEvent(NetworkState.LOADING)
            ServiceAPI.getApi().login(LoginReqBody(loginData.userId, loginData.password)).run {
                if (isSuccessful) {
                    body()?.data?.let {
                        if (it.userId.isNotEmpty()) {
                            userDataPrefs.putUserInfo(
                                it.userId, it.userKey, it.name, it.division, it.mmcmId
                            )
                            userData.postValue(Event(it))
                        } else {
                            sendErrorEvent(Error(ErrorCode.Auth.EMPTY_DATA))
                        }
                    } ?: run { sendErrorEvent(Error(ErrorCode.Auth.EMPTY_DATA)) }
                } else {
                    Logger.e("error: ${code()} : ${message()}")
                    sendErrorEvent(Error(ErrorCode.Auth.FAILED))
                }
            }
            sendNetworkEvent(NetworkState.LOADED)
        } catch (e: IOException) {
            sendNetworkErrorEvent("${e.message}")
        }
    }

    private fun getStoredLoginData(): LoginData = userDataPrefs.getLoginData()

    private fun storeLoginData(loginData: LoginData): Unit = userDataPrefs.putLoginData(loginData)

    private fun clearStoredLoginData(): Unit = userDataPrefs.clearLoginData()

    private suspend fun sendErrorEvent(
        error: Error
    ): Unit = withContext(Dispatchers.Main) {
        this@LoginRepository.error.postValue(Event(error))
    }

    private suspend fun sendNetworkErrorEvent(
        message: String
    ): Unit = withContext(Dispatchers.Main) {
        this@LoginRepository.sendNetworkEvent(NetworkState.error(message))
    }

    private suspend fun sendNetworkEvent(
        networkState: NetworkState
    ): Unit = withContext(Dispatchers.Main) {
        this@LoginRepository.networkState.postValue(Event(networkState))
    }

    companion object {
        @Volatile
        private var instance: LoginRepository? = null
        fun getInstance(
            userDataUserDataPreferences: UserDataPrefHelper
        ): LoginRepository = instance ?: synchronized(this) {
            instance ?: LoginRepository(userDataUserDataPreferences).also { instance = it }
        }
    }
}

