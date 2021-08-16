package kr.fluxion.fx.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.fluxion.fx.data.Error
import kr.fluxion.fx.data.ErrorCode
import kr.fluxion.fx.utils.Event
import kr.fluxion.fx.utils.RootChecker

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _result = MutableLiveData<Boolean>()
    val result: LiveData<Boolean> = _result

    private val _error = MutableLiveData<Event<Error>>()
    val error: LiveData<Event<Error>> = _error

    fun doSplashTask() {
        viewModelScope.launch {
            delay(1000)
            if (RootChecker.isRooted()) {
                _error.postValue(Event(Error(ErrorCode.Common.APP_ROOTED)))
                cancel()
            }

            // TODO what more??
            _result.postValue(true)
        }
    }
}