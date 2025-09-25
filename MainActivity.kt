package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.setSupportMultipleWindows(true)
        s.javaScriptCanOpenWindowsAutomatically = true

        webView.webViewClient = object : WebViewClient() {

            // API 24+
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleSpecialSchemes(url)
            }

            // للأجهزة القديمة
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleSpecialSchemes(url ?: return false)
            }

            private fun handleSpecialSchemes(url: String): Boolean {
                val uri = Uri.parse(url)
                val scheme = uri.scheme?.lowercase()

                when (scheme) {
                    "http", "https" -> return false // خلي الـWebView يفتحها
                    "tel" -> {
                        startActivity(Intent(Intent.ACTION_DIAL, uri))
                        return true
                    }
                    "mailto" -> {
                        startActivity(Intent(Intent.ACTION_SENDTO, uri))
                        return true
                    }
                    "sms", "smsto" -> {
                        startActivity(Intent(Intent.ACTION_SENDTO, uri))
                        return true
                    }
                    "whatsapp" -> { // مثلا whatsapp://send?phone=...
                        val i = Intent(Intent.ACTION_VIEW, uri)
                        try { startActivity(i) } catch (_: Exception) {}
                        return true
                    }
                    "intent" -> { // بعض المواقع كتعطي intent://
                        try {
                            val i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            if (i.resolveActivity(packageManager) != null) {
                                startActivity(i)
                            } else {
                                // جرّب افتحها فالمتجر إذا كان عندها fallback
                                val pkg = i.`package`
                                if (pkg != null) {
                                    val market = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=$pkg")
                                    )
                                    startActivity(market)
                                }
                            }
                        } catch (_: Exception) {}
                        return true
                    }
                    else -> {
                        // أي سكيم آخر: جرّب نفتحو بأي تطبيق مناسب
                        val i = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            if (i.resolveActivity(packageManager) != null) {
                                startActivity(i)
                                return true
                            }
                        } catch (_: ActivityNotFoundException) { }
                        return true // ما نخليهش للـWebView باش ما يطيحش فـ ERR_UNKNOWN_URL_SCHEME
                    }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            // إلى كان target="_blank"
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = webView
                resultMsg?.sendToTarget()
                return true
            }
        }

        webView.loadUrl("https://masafir.ma")

        // زر الرجوع
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }
}
