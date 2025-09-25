package com.masafir.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
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
        s.setSupportMultipleWindows(true)
        s.javaScriptCanOpenWindowsAutomatically = true
        s.cacheMode = WebSettings.LOAD_DEFAULT

        // يلتقط الروابط الخاصة ويحولها للتطبيقات المناسبة
        webView.webViewClient = object : WebViewClient() {

            // للأجهزة الجديدة
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleSpecialSchemes(url)
            }

            // توافق مع الأجهزة القديمة
            @Deprecated("Deprecated in API 24")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleSpecialSchemes(url ?: return false)
            }
        }

        // يدير handle حتى للروابط اللي كتخرج بنافذة جديدة (target=_blank / window.open)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val temp = WebView(this@MainActivity)
                temp.settings.javaScriptEnabled = true
                temp.settings.domStorageEnabled = true
                temp.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(v: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        val u = url ?: return
                        if (handleSpecialSchemes(u)) {
                            v?.destroy()
                        } else {
                            webView.loadUrl(u)
                            v?.destroy()
                        }
                    }
                }
                (resultMsg?.obj as? WebView.WebViewTransport)?.apply {
                    webView = temp
                }
                resultMsg?.sendToTarget()
                return true
            }
        }

        webView.loadUrl("https://masafir.ma")

        // زر الرجوع داخل الويب
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    /** DEAL مع tel/mailto/whatsapp/intent… بدون اتصال مباشر */
    private fun handleSpecialSchemes(url: String): Boolean {
        val uri = Uri.parse(url)
        return when (uri.scheme?.lowercase()) {
            "http", "https" -> false // خلي الويب فيو يفتحها
            "tel" -> {
                // يفتح Dialer بالرقم (بدون اتصال مباشر)
                startActivity(Intent(Intent.ACTION_DIAL, uri))
                true
            }
            "mailto" -> {
                startActivity(Intent(Intent.ACTION_SENDTO, uri))
                true
            }
            "sms", "smsto" -> {
                startActivity(Intent(Intent.ACTION_SENDTO, uri))
                true
            }
            "intent" -> {
                try {
                    val i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    if (i.resolveActivity(packageManager) != null) startActivity(i)
                    else i.`package`?.let { pkg ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                    }
                } catch (_: Exception) {}
                true
            }
            else -> {
                // أي سكيم آخر: جرّب نفتحوه بتطبيق خارجي ونمنعو من الويب فيو
                try {
                    val i = Intent(Intent.ACTION_VIEW, uri)
                    if (i.resolveActivity(packageManager) != null) startActivity(i)
                } catch (_: Exception) {}
                true
            }
        }
    }
}
