package kr.co.aiblab.cordova.plugin.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class MyLocationManager private constructor(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    val currentLocationFlow: Flow<Location?> = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Logger.d("$locationResult")
                offer(locationResult.lastLocation)
                close()
            }
        }

        fusedLocationClient.requestLocationUpdates(
            LocationRequest().apply {
                interval = TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_IN_SECONDS)
                fastestInterval = TimeUnit.SECONDS.toMillis(FASTEST_UPDATE_INTERVAL_IN_SECONDS)
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            },
            locationCallback,
            Looper.getMainLooper()
        )

        launch {
            val period = TimeUnit.SECONDS.toMillis(60)
            delay(period)
            send(getLastLocation())
            close(IllegalStateException("Stop location update. Delay($period) time is over."))
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Logger.d("Location updating is stopped.")
        }
    }.flowOn(Dispatchers.Default)

    @SuppressLint("MissingPermission")
    @ExperimentalCoroutinesApi
    suspend fun getLastLocation(): Location? = withContext(Dispatchers.IO) {
        fusedLocationClient.lastLocation.await()
    }

    companion object {

        const val UPDATE_INTERVAL_IN_SECONDS: Long = 60
        const val FASTEST_UPDATE_INTERVAL_IN_SECONDS: Long = UPDATE_INTERVAL_IN_SECONDS / 2

        @Volatile
        private var instance: MyLocationManager? = null
        fun getInstance(context: Context): MyLocationManager {
            return instance ?: synchronized(this) {
                instance ?: MyLocationManager(context).also { instance = it }
            }
        }
    }
}