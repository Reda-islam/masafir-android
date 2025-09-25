package com.masafir.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // نعرض WebView مباشرة
        webView = WebView(this)
        setContentView(webView)

        // إعدادات WebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT

        // لمعالجة حوارات/اختيارات داخل الصفحة
        webView.webChromeClient = WebChromeClient()

        // 👇 هنا التعديل المهم: دعم tel/mail/whatsapp
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                return handleCustomSchemes(view, url)
            }

            @Deprecated("For old Android versions")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return handleCustomSchemes(view, url)
            }

            private fun handleCustomSchemes(view: WebView, url: String): Boolean {
                return when {
                    // اتصال هاتفي
                    url.startsWith("tel:") -> {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                        view.context.startActivity(intent)
                        true
                    }
                    // إرسال بريد
                    url.startsWith("mailto:") -> {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                        view.context.startActivity(intent)
                        true
                    }
                    // واتساب
                    url.contains("wa.me") || url.contains("api.whatsapp.com") -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        view.context.startActivity(intent)
                        true
                    }
                    // باقي الروابط تفتح داخل التطبيق
                    else -> false
                }
            }
        }

        // رابط الموقع ديالك
        webView.loadUrl("https://masafir.ma")

        // زر الرجوع: يرجع للصفحة السابقة داخل الويب
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
}
