package com.masafir.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webView)
        val btnCall: Button = findViewById(R.id.btnCall)
        val btnWhatsApp: Button = findViewById(R.id.btnWhatsApp)

        // إعداد WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://masafir.ma") // غيّر الرابط إلا بغيت

        // زر الاتصال → يفتح تطبيق Dialer
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+212600000000") // غيّر الرقم ديالك
            }
            startActivity(intent)
        }

        // زر واتساب → يفتح المحادثة
        btnWhatsApp.setOnClickListener {
            val url = "https://wa.me/212600000000" // الرقم بصيغة دولية بلا 0
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.whatsapp")
                    data = Uri.parse(url)
                }
                startActivity(intent)
            } catch (e: Exception) {
                // إذا ماكانش واتساب، يفتح المتصفح
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
