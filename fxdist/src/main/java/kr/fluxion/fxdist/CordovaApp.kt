package kr.fluxion

import android.app.Application
import androidx.annotation.Nullable
import com.jakewharton.threetenabp.AndroidThreeTen
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import kr.fluxion.fxdist.BuildConfig

class CordovaApp : Application() {

    override fun onCreate() {
        initLogger()
        super.onCreate()
        AndroidThreeTen.init(this)
    }

    companion object {
        private fun initLogger() {
            Logger.clearLogAdapters()
            Logger.addLogAdapter(object : AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder().tag(BuildConfig.LOG_TAG).build()
            ) {
                override fun isLoggable(priority: Int, @Nullable tag: String?): Boolean {
                    return BuildConfig.DEBUG
                }
            })
        }
    }
}