package com.example.masafir

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myWebView = WebView(this)

        // باش يبقى التصفح داخل التطبيق وما يخرجش للبراوزر
        myWebView.webViewClient = WebViewClient()

        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.loadsImagesAutomatically = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // رابط Netlify ديالك
        myWebView.loadUrl("https://mellifluous-douhua-9377eb.netlify.app")

        setContentView(myWebView)
    }
}
