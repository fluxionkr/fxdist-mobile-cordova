package kr.fluxion.fx.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.fluxion.fx.data.LoginRepository

class LoginViewModelFactory(
    private val application: Application,
    private val repository: LoginRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(application, repository) as T
    }
}
