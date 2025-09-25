package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WebView مباشرة (إذا بغيت تستعمل layout، بدّل هاد الجزأ بـ setContentView(R.layout.activity_main))
        webView = WebView(this)
        setContentView(webView)

        val ws: WebSettings = webView.settings
        ws.javaScriptEnabled = true
        ws.domStorageEnabled = true
        ws.cacheMode = WebSettings.LOAD_DEFAULT
        ws.loadWithOverviewMode = true
        ws.useWideViewPort = true
        ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webChromeClient = WebChromeClient()

        // نعالجو السكيمات الخاصة (tel / mailto / whatsapp / wa.me)
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean = handleCustomSchemes(request.url.toString())

            @Deprecated("for old API")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =
                handleCustomSchemes(url)
        }

        // حمّل موقعك
        webView.loadUrl("https://masafir.ma")

        // زر الرجوع داخل الويب فيو
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun handleCustomSchemes(url: String): Boolean {
        val lower = url.lowercase()

        // 1) اتصال: افتح لوحة الاتصال (ماشي اتصال مباشر)
        if (lower.startsWith("tel:")) {
            startActivitySafe(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
            return true
        }

        // 2) إيميل: افتح تطبيق البريد
        if (lower.startsWith("mailto:")) {
            startActivitySafe(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
            return true
        }

        // 3) واتساب: whatsapp:// أو wa.me أو api.whatsapp.com
        if (
            lower.startsWith("whatsapp:") ||
            lower.contains("://wa.me/") ||
            lower.contains("api.whatsapp.com")
        ) {
            // جرّب واتساب العادي ثم بزنس، وإلا افتح بالمتصفح
            var i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.whatsapp")
            if (!startActivitySafe(i)) {
                i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.whatsapp.w4b")
                if (!startActivitySafe(i)) {
                    startActivitySafe(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            return true
        }

        // http/https نخليه للويب فيو
        if (lower.startsWith("http://") || lower.startsWith("https://")) return false

        // أي سكيم آخر: جرّب نفتحو بتطبيق خارجي
        startActivitySafe(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        return true
    }

    private fun startActivitySafe(intent: Intent, noAppMsg: String = "التطبيق المناسب غير مُثبت") : Boolean {
        return try {
            startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, noAppMsg, Toast.LENGTH_SHORT).show()
            false
        } catch (_: Exception) {
            Toast.makeText(this, "تعذّر فتح الرابط", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
