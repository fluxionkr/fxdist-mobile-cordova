package kr.fluxion.cordova.plugin.nfc.device

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.fluxion.cordova.plugin.nfc.FeliCaLiteTag
import kr.fluxion.cordova.plugin.nfc.FeliCaLiteTagException
import kr.fluxion.cordova.plugin.nfc.NFCDevice
import kr.fluxion.cordova.plugin.nfc.NFCTagException
import kr.fluxion.fxdist.R
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToLong

interface BoardADL : NFCDevice {

    @Throws(NFCTagException::class)
    suspend fun getAAR(tag: Tag)

    @Throws(NFCTagException::class)
    suspend fun startConnect(tag: Tag)

    @Throws(NFCTagException::class)
    suspend fun stopConnect(tag: Tag)

    @Throws(NFCTagException::class)
    suspend fun writeStoredDataInfo(tag: Tag)

    @Throws(NFCTagException::class, IllegalArgumentException::class)
    suspend fun writeStoredData(tag: Tag, pageIndex: Int = 1, isSetDevice: Boolean)

    @Throws(NFCTagException::class)
    suspend fun readResponse(tag: Tag): NdefMessage?

    @Throws(NFCTagException::class)
    suspend fun getData(respMessage: NdefMessage, currentPageIndex: Int, totalPageCount: Int): Data

    companion object {

        private const val DEFAULT_PATTERN_FORMAT_TIME: String = "HH:mm:ss"
        private const val DEFAULT_PATTERN_FORMAT_DATE: String = "yyyy-MM-dd"
        private const val DEFAULT_PATTERN_FORMAT_DATE_TIME: String =
            "$DEFAULT_PATTERN_FORMAT_DATE $DEFAULT_PATTERN_FORMAT_TIME"

        const val DATA_SENSOR_TYPE_TEMPERATURE: Byte = 0x08.toByte()
        const val SIZE_DATA_SENSOR_TEMPERATURE = 8

        const val CNT_BLOCK_DATA = 128
        const val CNT_READ_NUMBER_BLOCK = 7

        @Suppress("BlockingMethodInNonBlockingContext")
        @Throws(NFCTagException::class)
        suspend fun NdefMessage.writeNdefMessage(tag: Tag): Unit = withContext(Dispatchers.IO) {
            Ndef.get(tag)?.let { ndef ->
                with(ndef) {
                    if (isConnected) close()
                    connect()
                    writeNdefMessage(this@writeNdefMessage)
                    close()
                }
            } ?: run {
                try {
                    FeliCaLiteTag(tag).run {
                        val data: ByteArray = readWithoutEncryption(tag.id, 0)
                        writeNdefMessage(tag.id, this@writeNdefMessage)
                        writeBytes(tag.id, 0, data)
                    }
                } catch (e: Exception) {
                    throw FeliCaLiteTagException(
                        e.message ?: "FeliCaLiteTag exception has been occurred."
                    )
                }
            }
        }

        fun getTemperature(temperatureADC: Int): Float = when {
            temperatureADC > 2285 -> ((-0.000000006523913 * temperatureADC.toDouble().pow(3.0)
                    + 0.000051670622148 * temperatureADC.toDouble().pow(2.0)
                    - 0.159545952169831 * temperatureADC
                    + 174.801936478017000) * 10)
                .roundToLong().toFloat() / 10
            temperatureADC >= 2026 -> ((-0.0232 * temperatureADC + 54.94) * 10)
                .roundToLong().toFloat() / 10
            else -> ((-0.000000004546348 * temperatureADC.toDouble().pow(3.0)
                    + 0.000027017597627 * temperatureADC.toDouble().pow(2.0)
                    - 0.077677405140403 * temperatureADC
                    + 92.244714245217700) * 10)
                .roundToLong().toFloat() / 10
        }

        fun getEventCodeName(@EventId eventId: Int): String = when (eventId) {
            EventId.NORMAL -> Event.NORMAL.codeName
            EventId.TEMPERATURE_START -> Event.TEMPERATURE_START.codeName
            EventId.TEMPERATURE_END -> Event.TEMPERATURE_END.codeName
            EventId.NFC_COMMUNICATION -> Event.NFC_COMMUNICATION.codeName
            else -> eventId.toString()
        }

        fun formattedDateTime(
            year: Int, month: Int, day: Int, dayOfHour: Int, minute: Int, second: Int,
            pattern: String = DEFAULT_PATTERN_FORMAT_DATE_TIME
        ): String = getDateTime(year, month, day, dayOfHour, minute, second)
            .format(DateTimeFormatter.ofPattern(pattern))

        private fun getDateTime(
            year: Int, month: Int, day: Int, dayOfHour: Int, minute: Int, second: Int
        ): LocalDateTime = LocalDateTime.of(year, month, day, dayOfHour, second, minute)

        @Throws(IllegalArgumentException::class)
        fun @receiver:Status Int.getMessage(context: Context): String = when (this) {
            Status.NO_MORE_DATA -> context.getString(R.string.nfc_status_no_more_data)
            Status.READING_DATA -> context.getString(R.string.nfc_status_reading_data)
            Status.FAILED_READ_DATA -> context.getString(R.string.nfc_status_failed_read_data)
            Status.READ_ALL_DATA -> context.getString(R.string.nfc_status_read_all_data)
            Status.READ_NOT_ALL_DATA -> context.getString(R.string.nfc_status_read_not_all_data)
            else -> throw IllegalArgumentException("Unknown status received >> $this")
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class EventId {
        companion object {
            const val NORMAL = 0
            const val TEMPERATURE_START = 3
            const val TEMPERATURE_END = 4
            const val NFC_COMMUNICATION = 5
        }
    }

    enum class Event(val codeName: String) {
        NORMAL("N"),
        TEMPERATURE_START("TS"),
        TEMPERATURE_END("TE"),
        NFC_COMMUNICATION("NC")
    }

    @Parcelize
    data class Data(
        @SerializedName("hasNext") val hasNext: Boolean,
        @SerializedName("currentPageIndex") val currentPageIndex: Int,
        @SerializedName("totalPageCount") val totalPageCount: Int,
        @SerializedName("logs") val logs: List<DataLog>
    ) : Parcelable

    @Parcelize
    data class DataLog(
        @SerializedName("dateTime") val dateTime: String,
        @SerializedName("eventCode") val eventCode: String,
        @SerializedName("temperature") val temperature: Float
    ) : Parcelable

    @Retention(AnnotationRetention.SOURCE)
    annotation class Status {
        companion object {
            const val NO_MORE_DATA = 0
            const val READING_DATA = 1
            const val FAILED_READ_DATA = 2
            const val READ_ALL_DATA = 3
            const val READ_NOT_ALL_DATA = 4
        }
    }
}