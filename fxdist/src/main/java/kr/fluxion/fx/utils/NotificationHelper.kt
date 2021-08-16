package kr.fluxion.fx.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kr.fluxion.fx.BuildConfig.APPLICATION_ID
import kr.fluxion.fx.R

object LocationServiceNotification {
    fun createForegroundServiceNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID_LOCATION_FOREGROUND_SERVICE,
                context.getString(R.string.channel_id_location_foreground_service_readable),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
            }.run {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(this)
            }
        }
    }

    fun getForegroundServiceNotification(
        context: Context
    ): Notification = NotificationCompat.Builder(
        context, CHANNEL_ID_LOCATION_FOREGROUND_SERVICE
    ).apply {
        setSmallIcon(R.mipmap.ic_launcher_round)
        color = ContextCompat.getColor(context, R.color.colorPrimary)
        setContentText(context.getString(R.string.location_foreground_service_content))
    }.build()

    private const val CHANNEL_ID_LOCATION_FOREGROUND_SERVICE =
        "$APPLICATION_ID.channel.LOCATION_FOREGROUND_SERVICE"
}