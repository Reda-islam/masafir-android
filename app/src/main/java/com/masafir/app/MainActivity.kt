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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // 🔁 بدّل هذا بالرابط ديال موقعك (HTTPS مفضّل)
    private val startUrl = "[PUT_YOUR_URL_HERE]"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        // إعدادات WebView الأساسية
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        // إلى كان عندك صور/سكربتات HTTP وسط صفحة HTTPS، فعّل هاد السطر:
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // نعالج الروابط الخاصة (tel:/mailto:/whatsapp/wa.me/intent://) خارج WebView
        webView.webViewClient = object : WebViewClient() {

            @Deprecated("for < API 24")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleUrl(url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleUrl(request.url?.toString())
            }

            private fun handleUrl(raw: String?): Boolean {
                val url = raw ?: return false

                return try {
                    when {
                        // هاتف: نفتح الـDialer (ما كيحتاج حتى صلاحية)
                        url.startsWith("tel:", true) -> {
                            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                            true
                        }
                        // بريد إلكتروني
                        url.startsWith("mailto:", true) -> {
                            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                            true
                        }
                        // واتساب: whatsapp:// أو wa.me
                        url.startsWith("whatsapp://", true) || url.contains("wa.me/", true) -> {
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            if (i.resolveActivity(packageManager) != null) startActivity(i)
                            else Toast.makeText(this@MainActivity, "WhatsApp غير مُثبّت", Toast.LENGTH_SHORT).show()
                            true
                        }
                        // روابط intent:// (deeplinks لتطبيقات أخرى)
                        url.startsWith("intent://", true) -> {
                            try {
                                val i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                if (i.resolveActivity(packageManager) != null) startActivity(i)
                                else i.getStringExtra("browser_fallback_url")?.let { fb ->
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fb)))
                                }
                            } catch (_: Exception) { /* ignore */ }
                            true
                        }
                        // http/https نخليو WebView يحمّلهم عادي
                        url.startsWith("http://") || url.startsWith("https://") -> false

                        // أي سكيم آخر ما نحمّلوش داخل WebView
                        else -> true
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

        // حمّل موقعك
        webView.loadUrl(startUrl)
    }

    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
