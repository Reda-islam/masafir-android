package com.example.masafir

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myWebView = WebView(this)
        myWebView.webViewClient = WebViewClient()

        // نفعّل الجافاسكريبت باش كلشي يخدم
        val webSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        // ها الرابط ديالك الجديد على Netlify 👇
        myWebView.loadUrl("https://mellifluous-douhua-9377eb.netlify.app")

        setContentView(myWebView)
    }
}
