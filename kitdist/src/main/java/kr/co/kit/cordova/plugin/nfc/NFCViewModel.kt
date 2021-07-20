package kr.co.kit.cordova.plugin.nfc

import android.app.Application
import android.nfc.NdefMessage
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.lifecycle.*
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kr.co.kit.cordova.plugin.nfc.device.BoardADL
import kr.co.kit.cordova.plugin.nfc.device.D0201.Companion.checkValidGetDataInfoPayload
import kr.co.kit.cordova.plugin.nfc.device.D0201.Companion.checkValidGetDataPayload
import kr.co.kit.cordova.plugin.nfc.device.D0201.Companion.checkValidStartConnectionPayload
import kr.co.kit.cordova.plugin.nfc.device.D0201.Companion.checkValidStopConnectionPayload
import kr.co.kit.kitdist.BuildConfig
import kr.co.kit.kitdist.utils.Event
import kr.co.kit.kitdist.utils.showHexData
import java.util.*
import kotlin.system.measureTimeMillis

class NFCViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val nfcRepository: NFCRepository
) : AndroidViewModel(application) {

    val device: LiveData<String> = getSavedDeviceName().switchMap {
        Logger.i("CurrentDeviceName: $it")
        liveData { emit(it) }
    }

    private val _status = MutableLiveData<Event<@BoardADL.Status Int>>()
    val status: LiveData<Event<Int>> = _status

    private val _progress = MutableLiveData<Pair<Int, Int>>()
    val progress: LiveData<Pair<Int, Int>> = _progress

    private val _data = MutableLiveData<BoardADL.Data>()
    val data: LiveData<BoardADL.Data> = _data

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    @Suppress("BlockingMethodInNonBlockingContext")
    fun getData(tag: Tag, currentPageIndex: Int, totalPageCount: Int = 0) {
        Logger.d("[GetData] PageIndex: $currentPageIndex")
        viewModelScope.launch {
            val hasNextPage: Boolean = (totalPageCount == 0) or (currentPageIndex <= totalPageCount)
            if (hasNextPage.not()) {
                postStatusLiveData(BoardADL.Status.NO_MORE_DATA)
                cancel()
            }
            var data: BoardADL.Data = BoardADL.Data(
                hasNext = hasNextPage,
                currentPageIndex = currentPageIndex,
                totalPageCount = totalPageCount,
                logs = emptyList()
            )
            try {
                // Start connection
                val isSetDevice: Boolean = getStartConnMessage(tag).run {
                    postStatusLiveData(BoardADL.Status.READING_DATA)
                    Logger.i(
                        "[START_CONN] UID: ${tag.id.showHexData()}\n" +
                                "Resp: ${toByteArray().showHexData()}\n" +
                                "Records: ${Arrays.toString(records)}(${records.size})"
                    )
                    nfcRepository.isSetDevice(this)
                }
                Logger.d("isSetDevice? $isSetDevice")

                // Get stored data info
                getDataInfoMessage(tag).run {
                    Logger.i(
                        "[GET_DATA_INFO] UID: ${tag.id.showHexData()}\n" +
                                "Resp: ${toByteArray().showHexData()}\n" +
                                "Records: ${Arrays.toString(records)}(${records.size})"
                    )
                }

                // Get stored data
                var currentIndex: Int = data.currentPageIndex
                val dataLogs: MutableList<BoardADL.DataLog> = mutableListOf()
                do {
                    getDataMessage(tag, currentIndex, isSetDevice).run {
                        val payload: ByteArray = records.first().payload
                        Logger.i(
                            "[GET_DATA] UID: ${tag.id.showHexData()}\n" +
                                    "Resp: ${toByteArray().showHexData()}\n" +
                                    "Records: ${Arrays.toString(records)}(${records.size})\n" +
                                    "Payload: ${payload.showHexData()}"
                        )
                        data = nfcRepository.getData(
                            this, currentIndex, data.totalPageCount
                        ).also { respData ->
                            data = BoardADL.Data(
                                hasNext = respData.hasNext,
                                currentPageIndex = respData.currentPageIndex.also { currentIndex++ },
                                totalPageCount = respData.totalPageCount,
                                logs = dataLogs.apply { addAll(respData.logs) }
                            )
                            postProgressLiveData(data.currentPageIndex, data.totalPageCount)
                        }
                    }
                } while (data.hasNext)

                // Stop connection
                getStopConnMessage(tag).run {
                    Logger.i(
                        "[STOP_CONN] UID: ${tag.id.showHexData()}\n" +
                                "Resp: ${toByteArray().showHexData()}\n" +
                                "Records: ${Arrays.toString(records)}(${records.size})"
                    )
                }

                postStatusLiveData(BoardADL.Status.READ_ALL_DATA)
                postDataLiveData(data)
            } catch (e: NFCTagException) {
                Logger.e("[GET_DATA] NFCTagException: ${e.message}")
                Ndef.get(tag)?.close()
                if (data.logs.isNullOrEmpty()) {
                    postStatusLiveData(BoardADL.Status.FAILED_READ_DATA)
                } else {
                    postStatusLiveData(BoardADL.Status.READ_NOT_ALL_DATA)
                    postDataLiveData(data)
                }
                if (BuildConfig.DEBUG) {
                    when (e) {
                        is NFCEqualsResponseException,
                        is FeliCaLiteTagException,
                        is NFCReadTimeOutException -> postErrorMessageLiveData(
                            e.message ?: "NFC tag error has been occurred."
                        )
                    }
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext", "unused")
    @Throws(NFCTagException::class)
    private suspend fun getAAR(
        tag: Tag
    ): NdefMessage = withContext(Dispatchers.IO) {
        nfcRepository.getAAR(tag)
        delay(NFC_READ_MSG_DELAY_TIME)
        nfcRepository.readResponse(tag) ?: run { throw NFCNullResponseException() }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    private suspend fun getStartConnMessage(tag: Tag): NdefMessage = withContext(Dispatchers.IO) {
        nfcRepository.startConnect(tag)
        var response: NdefMessage?
        var readTime: Long = 0
        do {
            readTime += measureTimeMillis {
                delay(NFC_READ_MSG_DELAY_TIME)
                response = nfcRepository.readResponse(tag)
            }
            response?.let {
                Logger.d(
                    "[START_CONN_READ_MSG]\n" +
                            "message: ${it.toByteArray().showHexData()}\n" +
                            "ReadTime: $readTime"
                )
            } ?: Logger.d("Response is null >> $readTime")
            if (nfcRepository.isStartConnRespEqualsReq(response)) throw NFCEqualsResponseException()
            if (readTime > NFC_READ_MSG_CONNECTION_EXPIRE_TIME) throw NFCReadTimeOutException()
        } while (response?.records?.first()?.checkValidStartConnectionPayload() != true)
        response ?: run { throw NFCNullResponseException() }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    private suspend fun getStopConnMessage(tag: Tag): NdefMessage = withContext(Dispatchers.IO) {
        nfcRepository.stopConnect(tag)
        var response: NdefMessage?
        var readTime: Long = 0
        do {
            readTime += measureTimeMillis {
                delay(NFC_READ_MSG_DELAY_TIME)
                response = nfcRepository.readResponse(tag)
            }
            response?.let {
                Logger.d(
                    "[STOP_CONN_READ_MSG]\n" +
                            "message: ${it.toByteArray().showHexData()}\n" +
                            "ReadTime: $readTime"
                )
            } ?: Logger.d("Response is null >> $readTime")
            if (readTime > NFC_READ_MSG_CONNECTION_EXPIRE_TIME) throw NFCReadTimeOutException()
        } while (response?.records?.first()?.checkValidStopConnectionPayload() != true)
        response ?: run { throw NFCNullResponseException() }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    private suspend fun getDataInfoMessage(tag: Tag): NdefMessage = withContext(Dispatchers.IO) {
        nfcRepository.writeStoredDataInfo(tag)
        var response: NdefMessage?
        var readTime: Long = 0
        do {
            readTime += measureTimeMillis {
                delay(NFC_READ_MSG_DELAY_TIME)
                response = nfcRepository.readResponse(tag)
            }
            response?.let {
                Logger.d(
                    "[GET_DATA_INFO_READ_MSG]\n" +
                            "message: ${response?.toByteArray()?.showHexData()}\n" +
                            "ReadTime: $readTime"
                )
            } ?: Logger.d("Response is null >> $readTime")
            if (readTime > NFC_READ_MSG_GET_DATA_INFO_EXPIRE_TIME) throw NFCReadTimeOutException()
        } while (response?.records?.first()?.checkValidGetDataInfoPayload() != true)
        response ?: run { throw NFCNullResponseException() }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    private suspend fun getDataMessage(
        tag: Tag, pageIndex: Int, isSetDevice: Boolean
    ): NdefMessage = withContext(Dispatchers.IO) {
        nfcRepository.writeStoredData(tag, pageIndex, isSetDevice)
        var response: NdefMessage?
        var readTime: Long = 0
        do {
            readTime += measureTimeMillis {
                delay(NFC_READ_MSG_DELAY_TIME)
                response = nfcRepository.readResponse(tag)
            }
            response?.let {
                Logger.d(
                    "[GET_DATA_READ_MSG] PageIndex: $pageIndex\n" +
                            "message: ${it.toByteArray().showHexData()}\n" +
                            "ReadTime: $readTime"
                )
            } ?: Logger.d("Response is null >> $readTime")

            if (readTime > NFC_READ_MSG_GET_DATA_EXPIRE_TIME) throw NFCReadTimeOutException()
        } while (response?.records?.first()?.checkValidGetDataPayload() != true)
        response ?: run { throw NFCNullResponseException() }
    }

    private fun getSavedDeviceName(): MutableLiveData<String> =
        savedStateHandle.getLiveData(KEY_STATE_DEVICE_NAME)

    fun saveCurrentDeviceName(deviceName: String): Unit =
        savedStateHandle.set(KEY_STATE_DEVICE_NAME, deviceName).also {
            nfcRepository.currentDevice = deviceName
        }

    private fun postStatusLiveData(@BoardADL.Status status: Int): Unit =
        _status.postValue(Event(status))

    private fun postProgressLiveData(currentPageIndex: Int, totalPageCount: Int): Unit =
        _progress.postValue(Pair(currentPageIndex, totalPageCount))

    private fun postErrorMessageLiveData(errorMessage: String): Unit =
        _errorMessage.postValue(Event(errorMessage))

    private suspend fun postDataLiveData(data: BoardADL.Data): Unit =
        withContext(Dispatchers.Main) {
            delay(500)
            _data.postValue(data)
        }

    companion object {
        const val KEY_STATE_DEVICE_NAME = "device_name"

        const val NFC_READ_MSG_DELAY_TIME = 20L
        const val NFC_READ_MSG_CONNECTION_EXPIRE_TIME = 1000L
        const val NFC_READ_MSG_GET_DATA_INFO_EXPIRE_TIME = 3000L
        const val NFC_READ_MSG_GET_DATA_EXPIRE_TIME = 5000L
    }
}