package kr.co.kit.cordova.plugin.nfc

import android.nfc.NdefMessage
import android.nfc.Tag
import kr.co.kit.cordova.plugin.nfc.device.BoardADL
import kr.co.kit.cordova.plugin.nfc.device.D0201

class NFCRepository private constructor(
    private val d0201: D0201
) : BaseRepository(), BoardADL {

    lateinit var currentDevice: String

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun getAAR(tag: Tag): Unit = when (currentDevice) {
        D0201.name -> d0201.getAAR(tag)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun startConnect(
        tag: Tag
    ): Unit = when (currentDevice) {
        D0201.name -> d0201.startConnect(tag)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun stopConnect(
        tag: Tag
    ): Unit = when (currentDevice) {
        D0201.name -> d0201.stopConnect(tag)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun writeStoredDataInfo(
        tag: Tag
    ): Unit = when (currentDevice) {
        D0201.name -> d0201.writeStoredDataInfo(tag)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun writeStoredData(
        tag: Tag, pageIndex: Int, isSetDevice: Boolean
    ): Unit = when (currentDevice) {
        D0201.name -> d0201.writeStoredData(tag, pageIndex, isSetDevice)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun readResponse(
        tag: Tag
    ): NdefMessage? = when (currentDevice) {
        D0201.name -> d0201.readResponse(tag)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun getData(
        respMessage: NdefMessage, currentPageIndex: Int, totalPageCount: Int
    ): BoardADL.Data = when (currentDevice) {
        D0201.name -> d0201.getData(respMessage, currentPageIndex, totalPageCount)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    fun isStartConnRespEqualsReq(respMessage: NdefMessage?): Boolean = when (currentDevice) {
        D0201.name -> d0201.isStartConnRespEqualsReq(respMessage)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    fun isSetDevice(respMessage: NdefMessage): Boolean = when (currentDevice) {
        D0201.name -> d0201.isSetDevice(respMessage)
        else -> throw NFCNotSupportedDeviceException(currentDevice)
    }

    companion object {

        @Volatile
        private var instance: NFCRepository? = null
        fun getInstance(d0201: D0201): NFCRepository = instance ?: synchronized(this) {
            instance ?: NFCRepository(d0201).also { instance = it }
        }
    }
}