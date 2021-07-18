package kr.co.kit.kitdist.utils

import android.annotation.SuppressLint
import java.io.File

object RootChecker {

    // TODO 언제 호출할지 확인 필요
    //  https://github.com/darvincisec/DetectMagiskHide 적용?
    fun isRooted(): Boolean = fileNameBasedRootChecker() or fileNameBasedMagiskRootChecker() or
            commandBasedRootChecker()

    private fun commandBasedRootChecker(): Boolean = try {
        Runtime.getRuntime().exec("su")
        true
    } catch (e: Exception) {
        false
    }

    @SuppressLint("SdCardPath")
    private fun fileNameBasedRootChecker(): Boolean = fileNameBasedRootCheck(
        arrayOf(
            "/sbin/su",
            "/system/su",
            "/system/sbin/su",
            "/system/xbin/su",
            "/data/data/com.noshufou.android.su",
            "/system/app/Superuser.apk",
            "/system/bin/.ext",
            "/system/xbin/.ext"
        )
    )

    private fun fileNameBasedMagiskRootChecker(): Boolean = fileNameBasedRootCheck(
        arrayOf(
            "/sbin/.magisk/",
            "/sbin/.core/mirror",
            "/sbin/.core/img",
            "/sbin/.core/db-0/magisk.db"
        )
    )

    private fun fileNameBasedRootCheck(fileNames: Array<String>): Boolean =
        fileNames.find { File(it).exists() }?.let { true } ?: false
}

