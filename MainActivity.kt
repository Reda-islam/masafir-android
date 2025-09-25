package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.webkit.*
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // عدّل الأرقام هنا
    private val SUPPORT_PHONE = "+212677554433"      // رقم الاتصال
    private val WHATSAPP_NUMBER = "+212677554433"    // نفسو أو رقم آخر

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        val btnCall: ImageButton = findViewById(R.id.btnCall)
        val btnWhats: ImageButton = findViewById(R.id.btnWhatsApp)

        // إعداد WebView
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleSchemes(url)
            }

            @Deprecated("Deprecated in API 24")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleSchemes(url ?: return false)
            }
        }

        // يدير handle حتى للروابط اللي كتخرج بنافذة جديدة
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val temp = WebView(this@MainActivity)
                temp.settings.javaScriptEnabled = true
                temp.settings.domStorageEnabled = true
                temp.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(v: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        val u = url ?: return
                        if (handleSchemes(u)) {
                            v?.destroy()
                        } else {
                            webView.loadUrl(u)
                            v?.destroy()
                        }
                    }
                }
                (resultMsg?.obj as? WebView.WebViewTransport)?.apply {
                    webView = temp
                }
                resultMsg?.sendToTarget()
                return true
            }
        }

        webView.loadUrl("https://masafir.ma")

        // زر الرجوع داخل الويب
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })

        // أزرار علو WebView
        btnCall.setOnClickListener {
            // ما كنديروش اتصال مباشر، غير نفتح Dialer بالرقم
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE"))
            startActivity(intent)
        }

        btnWhats.setOnClickListener {
            // نفضّلو wa.me باش يخدم حتى بلا التطبيق
            val waUri = Uri.parse("https://wa.me/${WHATSAPP_NUMBER.replace("+", "").replace(" ", "")}")
            val i = Intent(Intent.ACTION_VIEW, waUri)
            try { startActivity(i) } catch (_: ActivityNotFoundException) {
                // fallback إلى واتساب سكيم إلا كان متبّت
                val alt = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=${WHATSAPP_NUMBER}"))
                try { startActivity(alt) } catch (_: Exception) {}
            }
        }
    }

    /** التعامل مع tel/mailto/whatsapp/intent… داخل WebView */
    private fun handleSchemes(url: String): Boolean {
        val uri = Uri.parse(url)
        return when (uri.scheme?.lowercase()) {
            "http", "https" -> false
            "tel" -> { // يفتح Dialer (ماشي اتصال مباشر)
                startActivity(Intent(Intent.ACTION_DIAL, uri))
                true
            }
            "mailto" -> {
                startActivity(Intent(Intent.ACTION_SENDTO, uri))
                true
            }
            "sms", "smsto" -> {
                startActivity(Intent(Intent.ACTION_SENDTO, uri))
                true
            }
            "intent" -> {
                try {
                    val i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    if (i.resolveActivity(packageManager) != null) startActivity(i)
                    else i.`package`?.let { pkg ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                    }
                } catch (_: Exception) {}
                true
            }
            else -> {
                // جرب أي سكيم آخر
                try {
                    val i = Intent(Intent.ACTION_VIEW, uri)
                    if (i.resolveActivity(packageManager) != null) startActivity(i)
                } catch (_: Exception) {}
                true
            }
        }
    }
}
