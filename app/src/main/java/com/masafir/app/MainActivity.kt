package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnCall: Button
    private lateinit var btnWhatsApp: Button

    // ✏️ بدّل هاد القيم برابط موقعك ورقمك
    private val PHONE_E164 = "+2126XXXXXXXX"                // مع +
    private val PHONE_INTL = "2126XXXXXXXX"                 // بدون +

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        btnCall = findViewById(R.id.btnCall)
        btnWhatsApp = findViewById(R.id.btnWhatsApp)

        // إعدادات WebView
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.webViewClient = object : WebViewClient() {

            // للأجهزة القديمة (بعضها ماكيستعملش WebResourceRequest)
            @Deprecated("Deprecated in API 24")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleSpecialLinks(url ?: return false)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleSpecialLinks(url)
            }

            private fun handleSpecialLinks(url: String): Boolean {
                // tel:
                if (url.startsWith("tel:")) {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    return true
                }
                // WhatsApp links
                if (url.startsWith("whatsapp:") || url.contains("wa.me") || url.contains("api.whatsapp.com")) {
                    try {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        i.setPackage("com.whatsapp")
                        startActivity(i)
                    } catch (e: ActivityNotFoundException) {
                        // افتح فالمتصفح كبديل أو بلغ المستخدم
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {
                            Toast.makeText(this@MainActivity, "ما قدرناش نفتحو الرابط.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return true
                }
                // باقي الروابط خليه للـ WebView
                return false
            }
        }

        // حمّل الموقع
        webView.loadUrl(START_URL)

        // زر الاتصال: يفتح واجهة المكالمات
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$PHONE_E164"))
            startActivity(intent)
        }

        // زر واتساب: يفتح محادثة
        btnWhatsApp.setOnClickListener {
            val url = "https://wa.me/$PHONE_INTL"
            try {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.whatsapp")
                }
                startActivity(i)
            } catch (e: ActivityNotFoundException) {
                // إذا ماكانش واتساب، جرّب بالمتصفح
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
