package kr.fluxion.fx.ui

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.orhanobut.logger.Logger
import kr.fluxion.cordova.plugin.nfc.NFCAdapterHelper
import kr.fluxion.fx.utils.getCurrentFragment
import kr.fluxion.fx.R
import org.apache.cordova.CordovaFragment
import org.apache.cordova.CordovaFragmentActivity

class MainActivity : CordovaFragmentActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        (getCurrentFragment() as CordovaFragment).onNewIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            with(NavHostFragment.create(R.navigation.main_navigation)) {
                supportFragmentManager.beginTransaction()
                    .replace(fragmentContainerId, this)
                    .setPrimaryNavigationFragment(this)
                    .commitNow()
            }
        }
    }

    override fun onResume() {
        val pIntent = PendingIntent.getActivity(
            this, 1300,
            Intent(this, javaClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }, 0
        )
        NFCAdapterHelper.getInstance(this).enableForegroundDispatch(this, pIntent)
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        NFCAdapterHelper.getInstance(this).disableForegroundDispatch(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        (getCurrentFragment() as CordovaFragment).onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        getCurrentFragment()?.let { fragment ->
            Logger.d("[Activity] onActivityResult::reqCode: $requestCode")
            (fragment as CordovaFragment).onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}