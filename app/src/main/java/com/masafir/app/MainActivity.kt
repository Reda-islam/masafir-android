package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // مهم: استعمل الواجهة اللي فيها WebView والأزرار
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        val ws = webView.settings
        ws.javaScriptEnabled = true
        ws.domStorageEnabled = true

        webView.webChromeClient = WebChromeClient()

        // اعتراض جميع الروابط غير http/https (tel/mailto/whatsapp/intent)
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean = handleCustomSchemes(request.url.toString())

            @Deprecated("old API")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =
                handleCustomSchemes(url)
        }

        webView.loadUrl("https://masafir.ma")   // بدّلها إلى موقعك إذا لزم

        // أزرار أسفل الصفحة (اختياري)
        findViewById<android.widget.Button>(R.id.btnCall)?.setOnClickListener {
            // متيمش مباشرة للاتصال — غير يفتح دايلر
            val phone = "0677554433" // ولا جيبه من الصفحة/السيرفر
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        }

        findViewById<android.widget.Button>(R.id.btnWhatsApp)?.setOnClickListener {
            val phoneIntl = "212677554433" // واتساب خاص بصيغة دولية بلا "+"
            val uri = Uri.parse("https://wa.me/$phoneIntl")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun handleCustomSchemes(url: String): Boolean {
        return when {
            url.startsWith("tel:") -> {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                true
            }
            url.startsWith("mailto:") -> {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                true
            }
            url.startsWith("whatsapp:") ||
            url.contains("wa.me") ||
            url.contains("api.whatsapp.com") -> {
                val view = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.setPackage("com.whatsapp")
                try {
                    startActivity(view)
                } catch (e: ActivityNotFoundException) {
                    // إلى ما كانش واتساب منصّب، نخلّيه يشوفها بلا باكيج
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                true
            }
            url.startsWith("intent:") -> {
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    startActivity(intent)
                    true
                } catch (_: Exception) { false }
            }
            else -> false // http/https خليه يكمل فالـWebView
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
