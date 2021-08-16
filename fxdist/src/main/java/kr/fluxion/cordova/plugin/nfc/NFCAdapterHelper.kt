package kr.fluxion.cordova.plugin.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.os.Build

class NFCAdapterHelper private constructor(context: Context) {

    private val nfcAdapter: NfcAdapter by lazy {
        NfcAdapter.getDefaultAdapter(context)
    }

    fun enableForegroundDispatch(activity: Activity, pIntent: PendingIntent) {
        if (isNotRunningOnEmulator()) {
            nfcAdapter.enableForegroundDispatch(activity, pIntent, NFC_INTENT_FILTER, TECH_LISTS)
        }
    }

    fun disableForegroundDispatch(activity: Activity) {
        if (isNotRunningOnEmulator()) {
            nfcAdapter.disableForegroundDispatch(activity)
        }
    }

    private fun isNotRunningOnEmulator() : Boolean = isRunningOnEmulator().not()

    private fun isRunningOnEmulator(): Boolean {
        var result = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion"))
        if (result) return true
        result = result or (Build.BRAND.startsWith("generic")
                && Build.DEVICE.startsWith("generic"))
        if (result) return true
        result = result or ("google_sdk" == Build.PRODUCT)
        return result
    }

    companion object {

        private val NFC_INTENT_FILTER: Array<IntentFilter> = arrayOf(IntentFilter().apply {
            addAction(NfcAdapter.ACTION_TECH_DISCOVERED)
            addAction(NfcAdapter.ACTION_TAG_DISCOVERED)
            addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
        })

        private val TECH_LISTS: Array<Array<String>> = arrayOf(
            arrayOf(NfcF::class.java.name)
        )

        @Volatile
        private var instance: NFCAdapterHelper? = null
        fun getInstance(context: Context): NFCAdapterHelper = instance ?: synchronized(this) {
            instance ?: NFCAdapterHelper(context).also { instance = it }
        }
    }
}