package kr.fluxion.cordova.plugin.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.orhanobut.logger.Logger
import kr.fluxion.fxdist.utils.UserDataPrefHelper
import org.apache.cordova.SimpleCordovaPlugin
import org.json.JSONArray

class Location : SimpleCordovaPlugin() {

    private var isServiceRunning: Boolean = false

    override fun execute(args: JSONArray): Boolean = when (action) {
        ACTION_START_LOCATION_SERVICE -> startService().run { true }
        ACTION_STOP_LOCATION_SERVICE -> stopService().run { true }
        else -> false
    }

    private fun startService() {
        UserDataPrefHelper.getInstance(context).getUserId().takeIf { it.isNotEmpty() }?.apply {
            if (hasLocationPermissions() and hasLocationBGPermission()) {
                Logger.d("isEnableGPS = ${isEnableGPS(context)}")
                if (isEnableGPS(context)) startLocationService()
                else turnGPSOn(context)
            } else requestLocationPerms()
        } ?: run {
            sendErrorCallback(4301, "UserId is not exist.")
        }
    }

    private fun isEnableGPS(context: Context): Boolean =
        context.getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun turnGPSOn(context: Context) {
        LocationServices.getSettingsClient(context)
            .checkLocationSettings(
                LocationSettingsRequest.Builder().apply {
                    addLocationRequest(
                        LocationRequest.create().apply {
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        }
                    )
                    setAlwaysShow(true)
                }.build()
            ).addOnSuccessListener {
                Logger.d("Resp: $it")
                startLocationService()
            }.addOnFailureListener { exception ->
                val statusCode = (exception as ApiException).statusCode
                Logger.w("Exception: $statusCode")
                if (exception is ResolvableApiException) {
                    try {
                        cordova.setActivityResultCallback(this)
                        exception.startResolutionForResult(activity, REQ_CODE_GPS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Logger.w("PendingIntent unable to execute request.")
                    }
                }
            }
    }

    private fun Context.getLocationManager(): LocationManager =
        getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private fun stopService(): Unit = stopLocationService()

    private fun startLocationService(): Unit = getLocationServiceIntent(context).apply {
        action = LocationService.ACTION_START_LOCATION_SERVICE
    }.run { startForegroundLocationService(context, this) }.also { isServiceRunning = true }

    private fun stopLocationService(): Unit = getLocationServiceIntent(context).apply {
        action = LocationService.ACTION_STOP_LOCATION_SERVICE
    }.run {
        if (isServiceRunning) startForegroundLocationService(context, this)
        else Logger.d("LocationService is not running. nothing to do stop.")
    }.also { isServiceRunning = false }

    private fun hasLocationPermissions(): Boolean = LOCATION_PERMISSIONS.all {
        cordova.hasPermission(it)
    }

    private fun requestLocationPerms(): Unit =
        cordova.requestPermissions(this, REQ_CODE_PERMS_LOCATION, LOCATION_PERMISSIONS)

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationBGPerms(): Unit =
        cordova.requestPermission(this, REQ_CODE_PERMS_LOCATION_BG, LOCATION_BG_PERMISSION)

    private fun hasLocationBGPermission(): Boolean = cordova.hasPermission(LOCATION_BG_PERMISSION)

    override fun onRequestPermissionResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQ_CODE_PERMS_LOCATION ->
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) requestLocationBGPerms()
                    else startLocationService()
                    sendSuccessCallback("Start Location service successfully.")
                } else {
                    Logger.w("Location perms are not granted.")
                    sendErrorCallback(4300, "Location perms are not granted.")
                }
            REQ_CODE_PERMS_LOCATION_BG -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startLocationService()
                    sendSuccessCallback("Stop Location service successfully.")
                } else {
                    Logger.w("Location BG perm is not granted.")
                    sendErrorCallback(4300, "Location BG perm is not granted.")
                }
            }
            else -> super.onRequestPermissionResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        Logger.d("onActivityResult: $requestCode")
        if (requestCode == REQ_CODE_GPS) {
            if (resultCode == Activity.RESULT_OK) startLocationService()
        } else super.onActivityResult(requestCode, resultCode, intent)
    }

    companion object {
        const val REQ_CODE_GPS = 10
        const val REQ_CODE_PERMS_LOCATION = 100
        const val REQ_CODE_PERMS_LOCATION_BG = 101
        const val ACTION_START_LOCATION_SERVICE = "startLocationService"
        const val ACTION_STOP_LOCATION_SERVICE = "stopLocationService"
    }
}