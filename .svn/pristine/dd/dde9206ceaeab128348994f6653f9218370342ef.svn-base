package kr.co.kit.cordova.plugin.location

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kr.co.kit.cordova.plugin.location.LocationService.Companion.ACTION_GET_CURRENT_LOCATION
import kr.co.kit.kitdist.BuildConfig.APPLICATION_ID
import kr.co.kit.kitdist.api.ServiceAPI
import kr.co.kit.kitdist.data.LocationReqBody
import kr.co.kit.kitdist.utils.LocationServiceNotification.createForegroundServiceNotificationChannel
import kr.co.kit.kitdist.utils.LocationServiceNotification.getForegroundServiceNotification
import kr.co.kit.kitdist.utils.UserDataPrefHelper
import java.util.concurrent.TimeUnit

class LocationService : LifecycleService() {

    private var isServiceRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()
        createForegroundServiceNotificationChannel(this)
    }

    @ExperimentalCoroutinesApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d("onStartCommand LocationService >> ${intent?.action}")
        val context: Context = this@LocationService.applicationContext
        intent?.action?.let {
            Logger.d("isServiceRunning? $isServiceRunning")
            when (it) {
                ACTION_START_LOCATION_SERVICE -> lifecycleScope.launch {
                    if (isServiceRunning.not()) {
                        startForeground()
                        delay(500)
                        startGettingLocation(context)
                    }
                    isServiceRunning = true
                }
                ACTION_STOP_LOCATION_SERVICE -> lifecycleScope.launch {
                    if (isServiceRunning) {
                        cancelAlarm(context)
                        stopForeground()
                        delay(500)
                        stopGettingLocation(context)
                    }
                    isServiceRunning = false
                }
                ACTION_GET_CURRENT_LOCATION -> lifecycleScope.launch {
                    startGettingLocation(context)
                    isServiceRunning = true
                }
                else -> throw IllegalArgumentException("Unknown action has been received. >> $it")
            }
        } ?: run { Logger.e("Intent is null.") }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Logger.d("onDestroy LocationService")
        super.onDestroy()
    }

    @ExperimentalCoroutinesApi
    private suspend fun startGettingLocation(
        context: Context
    ): Unit = withContext(Dispatchers.Default) {
        Logger.i("START Get current location!!!")
        val userId = UserDataPrefHelper.getInstance(baseContext).getUserId()
        val userKey = UserDataPrefHelper.getInstance(baseContext).getUserKey()
        val userName = UserDataPrefHelper.getInstance(baseContext).getUserName()
        val userDiv = UserDataPrefHelper.getInstance(baseContext).getUserDiv()
        val userMMCMId = UserDataPrefHelper.getInstance(baseContext).getUserMMCMId()

        MyLocationManager.getInstance(context).currentLocationFlow.catch { exception ->
            Logger.w("Exception: $exception")
        }.collect { lastLocation ->
            Logger.d("LastLocation: $lastLocation")
            lastLocation?.let {
                val reqBody = LocationReqBody(
                    userId, userKey, userName, userDiv, userMMCMId,
                    it.latitude.toString(), it.latitude.toString()
                )
                ServiceAPI.getApi().sendLocation(reqBody).run {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            Logger.d("Send location info successfully.")
                            if (resp.data.turnGPSInfoOff) {
                                Logger.d("STOP to Send location info.")
                                stopSelfLocationService(context)
                                cancel()
                            }
                        } ?: run { Logger.w("Failed to send location info.") }
                    } else Logger.w("error: ${code()} : ${message()}")
                }
            } ?: run { Logger.w("LastLocation is null.") }
        }
        setAlarm(context)
    }

    private suspend fun stopGettingLocation(
        context: Context
    ): Unit = withContext(Dispatchers.Default) {
        Logger.i("STOP Get current location!!!")
        cancelAlarm(context)
    }

    private fun startForeground(): Unit = getForegroundServiceNotification(this).run {
        startForeground(NOTIFICATION_ID_FOREGROUND_LOCATION_SERVICE, this)
    }

    private fun stopForeground(): Unit = stopForeground(true).also { stopSelf() }

    private fun Context.getAlarmManager(): AlarmManager =
        getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private suspend fun setAlarm(
        context: Context,
        periodSeconds: Long = TimeUnit.MINUTES.toSeconds(LOCATION_ALARM_PERIOD_MINUTES)
    ): Unit = withContext(Dispatchers.Default) {
        context.getAlarmManager().setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            getTriggerAtMillis(periodSeconds),
            PendingIntent.getBroadcast(
                context,
                RC_ALARM_GET_CURRENT_LOCATION,
                getLocationAlarmReceiverIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        Logger.d("Set NEW alarm!!")
    }

    private suspend fun cancelAlarm(
        context: Context
    ): Unit = withContext(Dispatchers.Default) {
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(
            PendingIntent.getBroadcast(
                context,
                RC_ALARM_GET_CURRENT_LOCATION,
                getLocationAlarmReceiverIntent(context),
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        )
        Logger.d("Cancel alarm!!")
    }

    private fun getTriggerAtMillis(seconds: Long): Long =
        SystemClock.elapsedRealtime() + TimeUnit.SECONDS.toMillis(seconds)

    companion object {
        const val LOCATION_ALARM_PERIOD_MINUTES = 10L
        const val NOTIFICATION_ID_FOREGROUND_LOCATION_SERVICE = 10
        const val ACTION_START_LOCATION_SERVICE = "$APPLICATION_ID.action.START_LOCATION_SERVICE"
        const val ACTION_GET_CURRENT_LOCATION = "$APPLICATION_ID.action.GET_CURRENT_LOCATION"
        const val ACTION_STOP_LOCATION_SERVICE = "$APPLICATION_ID.action.STOP_LOCATION_SERVICE"
        const val RC_ALARM_GET_CURRENT_LOCATION = 100
    }
}

fun getLocationServiceIntent(context: Context): Intent =
    Intent(context, LocationService::class.java)

fun startForegroundLocationService(context: Context, intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
    else context.startService(intent)
}

private fun stopSelfLocationService(context: Context): Unit =
    getLocationServiceIntent(context).apply {
        action = LocationService.ACTION_STOP_LOCATION_SERVICE
    }.run { startForegroundLocationService(context, this) }

val LOCATION_PERMISSIONS: Array<String> = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@RequiresApi(Build.VERSION_CODES.Q)
val LOCATION_BG_PERMISSION: String = Manifest.permission.ACCESS_BACKGROUND_LOCATION

class LocationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("LocationAlarmReceiver::Intent: $intent")
        getLocationServiceIntent(context).apply {
            action = ACTION_GET_CURRENT_LOCATION
        }.run { startForegroundLocationService(context, this) }
    }
}

fun getLocationAlarmReceiverIntent(context: Context): Intent =
    Intent(context, LocationAlarmReceiver::class.java)
