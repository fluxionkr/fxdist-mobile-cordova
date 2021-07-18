package kr.co.kit.kitdist.utils

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.webkit.JsResult
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.orhanobut.logger.Logger
import kr.co.kit.kitdist.R
import org.apache.cordova.CordovaFragment
import org.apache.cordova.CordovaFragmentActivity

@Suppress("unused")
fun <F : Fragment> AppCompatActivity.getFragment(
    fragmentClass: Class<F>
): F? = (supportFragmentManager.fragments.first() as NavHostFragment)
    .childFragmentManager.fragments.find {
        fragmentClass.isAssignableFrom(it.javaClass)
    }?.let {
        @Suppress("UNCHECKED_CAST")
        it as F
    }

fun AppCompatActivity.getCurrentFragment(): Fragment? =
    (supportFragmentManager.fragments.first() as NavHostFragment)
        .childFragmentManager.fragments.first()

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT): Unit =
    Toast.makeText(this, message, duration).show()

fun Context.showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT): Unit =
    showToast(getString(messageResId), duration)

fun Context.getJsAlertDialog(
    title: String = getString(R.string.alert_dialog_title_default),
    message: String,
    result: JsResult
): AlertDialog = getAlertDialog(
    title,
    message,
    { _, _ -> result.confirm() }
)

fun Context.getJsConfirmDialog(
    title: String = getString(R.string.alert_dialog_title_default),
    message: String,
    result: JsResult
): AlertDialog = getAlertDialog(
    title,
    message,
    { _, _ -> result.confirm() },
    { _, _ -> result.cancel() }
)

private fun Context.getAlertDialog(
    title: String = getString(R.string.alert_dialog_title_default),
    message: String,
    positiveClickListener: DialogInterface.OnClickListener,
    negativeClickListener: DialogInterface.OnClickListener? = null
): AlertDialog = AlertDialog.Builder(this).apply {
    setTitle(title)
    setMessage(message)
    setPositiveButton(R.string.alert_dialog_btn_label_confirm, positiveClickListener)
    negativeClickListener?.let {
        setNegativeButton(R.string.alert_dialog_btn_label_cancel, negativeClickListener)
    }
    setCancelable(false)
}.create()

fun Fragment.showToast(
    message: String?,
    duration: Int = Toast.LENGTH_SHORT
) = message?.let { Toast.makeText(requireContext(), message, duration).show() }
    ?: Logger.w("Toast message is null. Cannot show it.")

fun Context.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}