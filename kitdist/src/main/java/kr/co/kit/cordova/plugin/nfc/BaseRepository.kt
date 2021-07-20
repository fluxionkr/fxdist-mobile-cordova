package kr.co.kit.cordova.plugin.nfc

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.kit.kitdist.data.NetworkState
import kr.co.kit.kitdist.utils.Event

open class BaseRepository {

    val networkState = MutableLiveData<Event<NetworkState>>()
    val error = MutableLiveData<Event<Error>>()

    suspend fun sendErrorEvent(error: Error) =
        withContext(Dispatchers.Main) {
            this@BaseRepository.error.postValue(Event(error))
        }

    suspend fun sendNetworkErrorEvent(message: String) =
        sendNetworkEvent(NetworkState.error(message))

    suspend fun sendNetworkEvent(networkState: NetworkState) =
        withContext(Dispatchers.Main) {
            this@BaseRepository.networkState.postValue(Event(networkState))
        }
}