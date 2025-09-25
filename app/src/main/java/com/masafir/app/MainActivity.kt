package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // نستعمل ملف الواجهة الجديد
        setContentView(R.layout.activity_main)

        // نجيبو العناصر من الواجهة
        webView = findViewById(R.id.webView)
        val btnCall = findViewById<Button>(R.id.btnCall)
        val btnWhats = findViewById<Button>(R.id.btnWhatsApp)

        // إعدادات WebView
        val ws: WebSettings = webView.settings
        ws.javaScriptEnabled = true
        ws.domStorageEnabled = true
        ws.cacheMode = WebSettings.LOAD_DEFAULT

        // فتح الروابط داخل التطبيق + دعم tel: و mailto: و whatsapp:
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return when {
                    url.startsWith("tel:") -> {
                        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                        true
                    }
                    url.startsWith("mailto:") -> {
                        val email = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                        startActivity(Intent.createChooser(email, "Send email"))
                        true
                    }
                    url.startsWith("whatsapp:") || url.contains("wa.me") -> {
                        openWhatsApp(url)
                        true
                    }
                    else -> {
                        view.loadUrl(url)
                        false
                    }
                }
            }
        }
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl("https://masafir.ma")

        // رقم افتراضي – بدلو باللي باغي، أو قدّموه من الويب عبر JS Interface
        val defaultPhone = "0677554433"

        // زر اتصال => يفتح Dialer
        btnCall.setOnClickListener {
            openDialer(defaultPhone)
        }

        // زر واتساب
        btnWhats.setOnClickListener {
            openWhatsApp("https://wa.me/" + defaultPhone.filter { it.isDigit() })
        }

        // زر الرجوع
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun openDialer(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        startActivity(intent)
    }

    private fun openWhatsApp(uriOrNumber: String) {
        // ممكن يكون uri جاهز (whatsapp:/wa.me) أو غير رقم
        val uri = if (uriOrNumber.startsWith("http") || uriOrNumber.startsWith("whatsapp"))
            Uri.parse(uriOrNumber)
        else
            Uri.parse("https://wa.me/" + uriOrNumber.filter { it.isDigit() })

        val waIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            // نخليوها تفضّل تطبيق واتساب إلا كان متبّت
            setPackage("com.whatsapp")
        }
        try {
            startActivity(waIntent)
        } catch (_: ActivityNotFoundException) {
            // fallback: يفتح المتصفح على wa.me
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}
