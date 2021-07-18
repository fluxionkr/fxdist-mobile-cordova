package kr.co.kit.webview

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.orhanobut.logger.Logger
import org.apache.cordova.CordovaWebViewEngine
import org.apache.cordova.engine.SystemWebViewClient
import org.apache.cordova.engine.SystemWebViewEngine

class CordovaWebViewClient<out T : CordovaWebViewEngine> constructor(
    parentEngine: T
) : SystemWebViewClient(parentEngine as SystemWebViewEngine) {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Logger.d("onPageStarted > $url")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Logger.d("onPageFinished > $url")
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        Logger.d("Url: ${view?.url}")
        return super.shouldOverrideUrlLoading(view, request)
    }
}