package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val startUrl = "https://masafir.yourdomain.tld/" // بدّلها برابط موقعك

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview) // تأكد الـ id موجود فـ activity_main.xml
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {

            // لأجهزة قديمة
            @Deprecated("for < API 24")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleUrl(url)
            }

            // لأجهزة حديثة
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleUrl(request.url?.toString())
            }

            private fun handleUrl(raw: String?): Boolean {
                val url = raw ?: return false
                return try {
                    when {
                        url.startsWith("tel:", true) -> {
                            // اتصال عبر Dialer (أفضل لGoogle Play)
                            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                            true
                        }
                        url.startsWith("mailto:", true) -> {
                            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                            true
                        }
                        url.startsWith("whatsapp://", true) ||
                        url.contains("wa.me/", true) -> {
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            if (i.resolveActivity(packageManager) != null) startActivity(i)
                            else Toast.makeText(this@MainActivity, "WhatsApp غير مُثبت", Toast.LENGTH_SHORT).show()
                            true
                        }
                        url.startsWith("intent://", true) -> {
                            try {
                                val i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                if (i.resolveActivity(packageManager) != null) startActivity(i)
                                else i.getStringExtra("browser_fallback_url")?.let { fb ->
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fb)))
                                }
                            } catch (_: Exception) {}
                            true
                        }
                        url.startsWith("http://") || url.startsWith("https://") -> false // خلّي WebView يكمّل
                        else -> true // أي سكيم مجهول: ما نخليوش WebView يحمّلو
                    }
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@MainActivity, "لا يوجد تطبيق مناسب", Toast.LENGTH_SHORT).show()
                    true
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Link error: ${e.message}", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }

        webView.loadUrl(startUrl)
    }

    // رجوع للخلف داخل الويب فيو
    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
