package kr.fluxion.cordova.plugin.nfc.device

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.fluxion.cordova.plugin.nfc.NFCDevice.Companion.getReqLog
import kr.fluxion.cordova.plugin.nfc.NFCTagException
import kr.fluxion.cordova.plugin.nfc.device.BoardADL.Companion.CNT_BLOCK_DATA
import kr.fluxion.cordova.plugin.nfc.device.BoardADL.Companion.CNT_READ_NUMBER_BLOCK
import kr.fluxion.cordova.plugin.nfc.device.BoardADL.Companion.DATA_SENSOR_TYPE_TEMPERATURE
import kr.fluxion.cordova.plugin.nfc.device.BoardADL.Companion.SIZE_DATA_SENSOR_TEMPERATURE
import kr.fluxion.cordova.plugin.nfc.device.BoardADL.Companion.formattedDateTime
import kr.fluxion.cordova.plugin.nfc.device.BoardADL.Companion.getEventCodeName
import kr.fluxion.cordova.plugin.nfc.device.BoardADL.Companion.getTemperature
import kr.fluxion.cordova.plugin.nfc.device.BoardADL.Companion.writeNdefMessage
import kr.fluxion.fx.utils.convertToBytes
import kr.fluxion.fx.utils.convertToInt
import kr.fluxion.fx.utils.toHexString
import kotlin.experimental.xor
import kotlin.math.ceil

class D0201 : BoardADL {
    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun getAAR(
        tag: Tag
    ): Unit = withContext(Dispatchers.IO) {
        // Nothing to do
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun startConnect(
        tag: Tag
    ): Unit = withContext(Dispatchers.IO) {
        NdefMessage(START_CONNECT_BYTES).apply {
            Logger.d("[START_CONN]\n${getReqLog(toByteArray().toHexString())}")
        }.writeNdefMessage(tag)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun stopConnect(
        tag: Tag
    ): Unit = withContext(Dispatchers.IO) {
        NdefMessage(STOP_CONNECT_BYTES).apply {
            Logger.d("[STOP_CONN]\n${getReqLog(toByteArray().toHexString())}")
        }.writeNdefMessage(tag)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun writeStoredDataInfo(
        tag: Tag
    ): Unit = withContext(Dispatchers.IO) {
        NdefMessage(GET_DATA_INFO_BYTES).apply {
            Logger.d("[GET_DATA_INFO]\n${getReqLog(toByteArray().toHexString())}")
        }.writeNdefMessage(tag)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class, IllegalArgumentException::class)
    override suspend fun writeStoredData(
        tag: Tag, pageIndex: Int, isSetDevice: Boolean
    ): Unit = withContext(Dispatchers.IO) {
        if (pageIndex < 1) throw IllegalArgumentException("PageIndex must be over 1.")
        NdefMessage(GET_DATA_BYTES((pageIndex - 1), isSetDevice)).apply {
            Logger.d("[GET_DATA]\n${getReqLog(toByteArray().toHexString())}")
        }.writeNdefMessage(tag)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun readResponse(
        tag: Tag
    ): NdefMessage? = withContext(Dispatchers.IO) {
        Ndef.get(tag)?.let { ndef ->
            with(ndef) {
                if (isConnected) close()
                connect()
                ndefMessage.also { close() }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(NFCTagException::class)
    override suspend fun getData(
        respMessage: NdefMessage, currentPageIndex: Int, totalPageCount: Int
    ): BoardADL.Data = withContext(Dispatchers.Default) {
        with(respMessage) {
            val payload: ByteArray = records.first().payload
            val totalLogsSize: Int = payload.sliceArray(4..5).convertToInt()
            val currentLogSize: Int = payload.sliceArray(6..7).convertToInt()
            val totalPageSize: Int = if (totalPageCount > 0) totalPageCount
            else ceil(totalLogsSize.toDouble() / currentLogSize.toDouble()).toInt()
            val currentPageIdx: Int = currentPageIndex
            val hasNext: Boolean = currentPageIdx < totalPageSize
            val pageSize: Int = getDataCount(this).let { dataCount ->
                if (hasNext) dataCount else currentLogSize % dataCount
            }
            Logger.i(
                "Page: $currentPageIdx / $totalPageSize\n" +
                        "Logs: $currentLogSize / $totalLogsSize\n" +
                        "hasNext: $hasNext\n" +
                        "PageSize: $pageSize"
            )

            val sensorType: Byte = getSensorType(respMessage)
            val dataLogs: MutableList<BoardADL.DataLog> = mutableListOf()
            for (i: Int in 0 until pageSize) {
                var index: Int = getDataIndex(respMessage, i)
                val year: Int = payload[index].convertToInt() + 2000
                val month: Int = payload[++index].convertToInt()
                val day: Int = payload[++index].convertToInt()
                val hour: Int = payload[++index].convertToInt()
                val minute: Int = payload[++index].convertToInt()
                val second = 0
                val event: Int = payload[++index].convertToInt()
                val temperatureADC: Int = payload.sliceArray(++index..index + 1).convertToInt()
                val dateTime: String = formattedDateTime(year, month, day, hour, minute, second)
                val eventCodeName: String = getEventCodeName(event)
                val temperature: Float = getTemperature(temperatureADC)
                dataLogs.add(BoardADL.DataLog(dateTime, eventCodeName, temperature))

                // FIXME 로그 개수가 너무 많음
                if (i < 3)
                    Logger.d(
                        "$i Item\n" +
                                "Date= $dateTime\n" +
                                "Event: $event($eventCodeName)\n" +
                                "Temperature: $temperature\n" +
                                "SensorType: $sensorType"
                    )
            }
            BoardADL.Data(hasNext, currentPageIdx, totalPageSize, dataLogs.toList())
        }
    }

    fun isStartConnRespEqualsReq(respMessage: NdefMessage?): Boolean =
        respMessage?.toByteArray().contentEquals(START_CONNECT_BYTES)

    fun isSetDevice(respMessage: NdefMessage): Boolean =
        respMessage.records.first().payload[4].run { this == 0x01.toByte() }

    private fun getDataIndex(respMessage: NdefMessage, index: Int): Int =
        (getDataSize(respMessage) * index) + 8

    @Throws(IllegalArgumentException::class)
    private fun getDataSize(respMessage: NdefMessage): Int =
        when (val type: Byte = getSensorType(respMessage)) {
            DATA_SENSOR_TYPE_TEMPERATURE -> SIZE_DATA_SENSOR_TEMPERATURE
            else -> throw IllegalArgumentException("Not support sensor type >> $type")
        }

    @Throws(IllegalArgumentException::class)
    private fun getDataCount(respMessage: NdefMessage): Int = getDataSize(respMessage).run {
        (CNT_BLOCK_DATA / this) * CNT_READ_NUMBER_BLOCK
    }

    private fun getSensorType(respMessage: NdefMessage): Byte =
        respMessage.records.first().payload[3]

    companion object {

        val name: String = D0201::class.java.simpleName

        fun NdefRecord.checkValidStartConnectionPayload(): Boolean = with(this) {
            (tnf == NdefRecord.TNF_MIME_MEDIA) and
                    (payload.sliceArray(0..2)
                        .contentEquals(byteArrayOf(0x01.toByte(), 0x02.toByte(), 0x03.toByte())))
        }

        fun NdefRecord.checkValidStopConnectionPayload(): Boolean = with(this) {
            (tnf == NdefRecord.TNF_MIME_MEDIA) and (payload != null)
        }

        fun NdefRecord.checkValidGetDataInfoPayload(): Boolean = with(this) {
            (tnf == NdefRecord.TNF_MIME_MEDIA) and (payload != null)
        }

        fun NdefRecord.checkValidGetDataPayload(): Boolean = with(this) {
            (tnf == NdefRecord.TNF_MIME_MEDIA) and (payload != null) and
                    ((payload[0] == 0x16.toByte()) or (payload[0] == 0x1F.toByte()))
        }

        private val CMD_PREFIX: ByteArray = byteArrayOf(0xD2.toByte(), 0x08.toByte(), 0x06.toByte())
        private val MIME_TYPE_DATA: ByteArray = "app/data".toByteArray()

        private val START_CONNECT_BYTES: ByteArray = CMD_PREFIX.plus(MIME_TYPE_DATA).plus(
            "start".toByteArray().plus(0x00.toByte()).apply {
                this[lastIndex] = getChecksum(sliceArray(0 until lastIndex))
            }
        )

        private val STOP_CONNECT_BYTES: ByteArray = CMD_PREFIX.plus(MIME_TYPE_DATA).plus(
            "stop".toByteArray().plus(0xFF.toByte()).plus(0x00.toByte()).apply {
                this[lastIndex] = getChecksum(sliceArray(0 until lastIndex))
            }
        )

        private val GET_DATA_INFO_BYTES: ByteArray = CMD_PREFIX.plus(MIME_TYPE_DATA).plus(
            byteArrayOf(0x02.toByte(), 0x00.toByte(), 0x02.toByte())
                .plus("tm".toByteArray()).plus(0x00.toByte()).apply {
                    this[lastIndex] = getChecksum(sliceArray(0 until lastIndex))
                }
        )

        private val GET_DATA_BYTES: (Int, Boolean) -> ByteArray =
            { pageIndex: Int, isSetDevice: Boolean ->
                val firstByte: Byte = if (isSetDevice) 0x06.toByte() else 0x0F.toByte()
                CMD_PREFIX.plus(MIME_TYPE_DATA).plus(
                    byteArrayOf(firstByte, 0x00.toByte(), 0x02.toByte())
                        .plus(pageIndex.convertToBytes(2)).plus(0x00.toByte()).apply {
                            this[lastIndex] = getChecksum(sliceArray(0 until lastIndex))
                        }
                )
            }

        private fun getChecksum(byteArray: ByteArray): Byte {
            var checksum: Short = 0x00
            byteArray.forEach { checksum = checksum xor it.toShort() }
            return checksum.toByte()
        }
    }
}