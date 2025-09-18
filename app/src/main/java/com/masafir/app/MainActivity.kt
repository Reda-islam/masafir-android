package com.masafir.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val START_URL = "https://masafir-ma-prod.web.app"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val w = findViewById<WebView>(R.id.webview)
        w.settings.javaScriptEnabled = true
        w.settings.domStorageEnabled = true
        w.settings.cacheMode = WebSettings.LOAD_DEFAULT
        w.webViewClient = WebViewClient()
        w.webChromeClient = WebChromeClient()
        w.loadUrl(START_URL)
    }

    override fun onBackPressed() {
        val w = findViewById<WebView>(R.id.webview)
        if (w.canGoBack()) w.goBack() else super.onBackPressed()
    }
}
