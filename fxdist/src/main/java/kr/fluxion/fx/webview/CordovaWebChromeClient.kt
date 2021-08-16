package kr.fluxion.webview

import android.util.Log
import android.webkit.ConsoleMessage
import com.orhanobut.logger.Logger
import org.apache.cordova.CordovaWebViewEngine
import org.apache.cordova.engine.SystemWebChromeClient
import org.apache.cordova.engine.SystemWebViewEngine

class CordovaWebChromeClient<out T : CordovaWebViewEngine> constructor(
    parentEngine: T
) : SystemWebChromeClient(parentEngine as SystemWebViewEngine) {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        Log.v("FLUXION",
            "[CONSOLE.LOG] ${consoleMessage.message()}  " +
                    "(${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})"
        )
        return true
    }
}