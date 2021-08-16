package kr.fluxion.cordova.plugin.barcode

import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.zxing.integration.android.IntentIntegrator
import org.apache.cordova.SimpleCordovaPlugin
import org.json.JSONArray

class Barcode : SimpleCordovaPlugin() {

    override fun execute(args: JSONArray): Boolean = when (action) {
        kr.fluxion.cordova.plugin.barcode.Barcode.Companion.ACTION_OPEN_BARCODE_SCANNER -> activity.runOnUiThread { startBarcodeScanner() }.run { true }
        else -> false
    }

    private fun startBarcodeScanner() {
        IntentIntegrator(activity).apply {
            captureActivity = QRScannerActivity::class.java
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setOrientationLocked(false)
        }.createScanIntent().run {
            cordova.startActivityForResult(this@Barcode, this@run, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            kr.fluxion.cordova.plugin.barcode.Barcode.Companion.REQ_CODE_BARCODE_SCANNER -> IntentIntegrator.parseActivityResult(
                resultCode, intent
            )?.let {
                it.contents?.let { contents ->
                    sendSuccessCallback("success", getBarcodeData(contents))
                } ?: run { sendErrorCallback(4301, "barcode result is null.") }
            } ?: run { sendErrorCallback(4300, "barcode scanning failed.") }
            else -> super.onActivityResult(requestCode, resultCode, intent)
        }
    }

    private fun getBarcodeData(barcodeData: String): String = Gson().toJson(JsonObject().apply {
        addProperty("barcode", barcodeData)
    })

    companion object {
        const val ACTION_OPEN_BARCODE_SCANNER = "openBarcodeScanner"
        const val REQ_CODE_BARCODE_SCANNER = 100
    }
}