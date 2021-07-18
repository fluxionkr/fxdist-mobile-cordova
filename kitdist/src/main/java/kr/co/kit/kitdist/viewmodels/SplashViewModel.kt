package kr.co.kit.kitdist.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.kit.kitdist.data.Error
import kr.co.kit.kitdist.data.ErrorCode
import kr.co.kit.kitdist.utils.Event
import kr.co.kit.kitdist.utils.RootChecker

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