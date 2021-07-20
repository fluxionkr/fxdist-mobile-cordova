package kr.co.kit.kitdist.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.navArgs
import com.orhanobut.logger.Logger
import kr.co.kit.cordova.plugin.location.*
import kr.co.kit.kitdist.data.UserData
import kr.co.kit.webview.CordovaWebChromeClient
import kr.co.kit.webview.CordovaWebViewClient
import org.apache.cordova.CordovaFragment

class MainFragment : CordovaFragment() {

    private val args: MainFragmentArgs by navArgs()

    override fun init() {
        super.init()
        with(systemWebView) {
            appView.engine.run {
                webViewClient = CordovaWebViewClient(this)
                webChromeClient = CordovaWebChromeClient(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        startLocationService()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUrl(launchUrl)
    }

    fun getUserData(): UserData = args.userData

    private fun startLocationService(): Unit =
        requestLocationPermissionsLauncher.launch(LOCATION_PERMISSIONS)

    private fun startLocationForegroundService(): Unit =
        getLocationServiceIntent(requireContext()).apply {
            action = LocationService.ACTION_START_LOCATION_SERVICE
        }.run { startForegroundLocationService(requireContext(), this) }

    private val requestLocationPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultMap ->
            Logger.d("[LOCATION] GrantedPermsResult: $resultMap")
            if (resultMap.all { it.value == false }) {
                Logger.w("Location perms are denied...")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocationPermissionLauncher.launch(LOCATION_BG_PERMISSION)
                } else {
                    startLocationForegroundService()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestBackgroundLocationPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Logger.d("[LOCATION_BG] GrantedPermsResult: $it")
            if (it) startLocationForegroundService()
            else Logger.e("Why perm is denied?")
        }
}