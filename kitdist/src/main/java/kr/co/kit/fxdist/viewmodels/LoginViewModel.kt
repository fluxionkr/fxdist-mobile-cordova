package kr.co.kit.kitdist.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.kit.kitdist.data.*
import kr.co.kit.kitdist.utils.Event

class LoginViewModel(
    application: Application,
    private val repository: LoginRepository
) : AndroidViewModel(application) {

    private val _loginData = MutableLiveData<LoginData>()
    val loginData: LiveData<LoginData> = _loginData

    fun handleNetworkState(): LiveData<Event<NetworkState>> = repository.networkState

    fun handleError(): LiveData<Event<Error>> = repository.error

    fun navigateToMainDest(): LiveData<Event<UserData>> = repository.userData

    fun autoLogin() {
        viewModelScope.launch(Dispatchers.Default) {
            repository.autoLogin()
        }
    }

    fun login(loginData: LoginData) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.login(loginData)
        }
    }
}