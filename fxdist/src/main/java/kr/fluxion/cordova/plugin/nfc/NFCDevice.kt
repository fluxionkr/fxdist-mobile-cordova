package kr.fluxion.cordova.plugin.nfc

import java.io.IOException

interface NFCDevice {

    companion object {

        fun getReqLog(request: String?): String =
            "REQ: ${request?.let { getChunkByteArrayData(it) }}"

        fun getRespLog(response: String?): String =
            "RESP: ${response?.let { getChunkByteArrayData(it) }}"

        private fun getChunkByteArrayData(hexString: String): String = with(hexString) {
            if ((startsWith("Exception") || startsWith("NULL")).not()) {
                chunkedSequence(2).joinToString(" ")
            } else hexString
        }
    }

    /*enum class Error {
        EXCEPTION_IO,
        FAILED_GET_NDEF_MESSAGE,
        NOT_AVAILABLE_PAYLOAD,
        NOT_SUPPORTED_DEVICE,
        FAILED_GET_DEVICE_INFO,
        NDEF_NOT_AVAILABLE
    }*/

    /*fun getMessage(context: Context, error: Error): String = when (error) {
        EXCEPTION_IO -> context.getString(R.string.nfc_error_io_exception)
        NOT_AVAILABLE_PAYLOAD -> context.getString(R.string.nfc_error_not_available_payload)
        FAILED_GET_DEVICE_INFO -> context.getString(R.string.nfc_error_failed_get_device_info)
        FAILED_GET_NDEF_MESSAGE -> context.getString(R.string.nfc_error_failed_get_ndef_message)
        NOT_SUPPORTED_DEVICE -> context.getString(R.string.nfc_error_not_supported_device)
        NDEF_NOT_AVAILABLE -> context.getString(R.string.nfc_error_ndef_not_available)
    }*/
}

open class NFCTagException(message: String) : IOException(message)

class NFCReadTimeOutException(
    message: String = "Time out of reading NFC message"
) : NFCTagException(message)

class FeliCaLiteTagException(
    message: String = "FeliCaLiteTag exception has been occurred."
) : NFCTagException(message)

class NFCNullResponseException(
    message: String = "Response is null."
) : NFCTagException(message)

class NFCEqualsResponseException(
    message: String = "Response bytes equals request bytes."
) : NFCTagException(message)

class NFCNotSupportedDeviceException(
    device: String,
    message: String = "$device is NOT supported device"
) : NFCTagException(message)