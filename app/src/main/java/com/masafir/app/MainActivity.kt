package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        val btnCall: Button = findViewById(R.id.btnCall)
        val btnWhatsApp: Button = findViewById(R.id.btnWhatsApp)

        // ✅ بدل الرابط مؤقتًا بـ Google.com باش تختبر
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return handleCustomSchemes(url)
            }
        }
        webView.loadUrl("https://www.google.com")

        // زر الاتصال
        btnCall.setOnClickListener {
            val phone = "+212600000000" // ✨ بدل برقمك
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        }

        // زر واتساب
        btnWhatsApp.setOnClickListener {
            val phone = "212600000000" // ✨ رقم بدون "+"
            val url = "https://wa.me/$phone"
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    setPackage("com.whatsapp")
                }
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "واتساب غير مثبت", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCustomSchemes(url: String): Boolean {
        return when {
            url.startsWith("tel:") -> {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                startActivity(intent)
                true
            }
            url.startsWith("mailto:") -> {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                startActivity(intent)
                true
            }
            url.contains("wa.me") || url.startsWith("whatsapp:") -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("com.whatsapp")
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "واتساب غير مثبت", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
