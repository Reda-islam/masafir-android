package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // عدّل هاد القيم برقم الهاتف ديالك
    private val phoneNumber = "+212600000000"      // للاتصال
    private val whatsappNumber = "212600000000"    // بدون + وبصيغة دولية

    // غيّر الرابط إلى الدومين ديالك النهائي
    private val startUrl = "https://mellifluous-douhua-9377eb.netlify.app/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ربط الواجهات
        webView = findViewById(R.id.webview)
        val btnCall: Button = findViewById(R.id.btnCall)
        val btnWhatsapp: Button = findViewById(R.id.btnWhatsapp)

        // إعدادات WebView
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            allowFileAccess = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        webView.webChromeClient = WebChromeClient()

        // فتح الروابط الخاصة خارج WebView
        webView.webViewClient = object : WebViewClient() {

            // هندل لسكيمات خاصة: tel / mailto / whatsapp
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleCustomSchemes(request.url.toString())
            }

            @Deprecated("for old API")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return handleCustomSchemes(url)
            }

            // إظهار/إخفاء عناصر حسب الصفحة
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // مثال: إخفاء رسالة الرحلة إلا إذا كنا في صفحة البحث
                // بدّل ".trip-note" بالـ selector الحقيقي للرسالة عندك
                val showTrips = url?.contains("search", ignoreCase = true) == true
                val js = if (showTrips) {
                    // خليه يظهر (كنشيل display:none)
                    """(function(){var el=document.querySelector(".trip-note"); if(el){el.style.removeProperty("display");}})();"""
                } else {
                    // خبيه
                    """(function(){var el=document.querySelector(".trip-note"); if(el){el.style.setProperty("display","none");}})();"""
                }
                webView.evaluateJavascript(js, null)

                // (اختياري) سكريبتك القديم لإخفاء أزرار الحذف إن بغيتيه يبقى
                val jsHideDanger = """
                    (function () {
                      function hideDangerButtons(){
                        var keywords=["إزالة","حذف","Delete","Supprimer","Effacer"];
                        var nodes=Array.from(document.querySelectorAll('button, a, [role="button"], .btn, .button'));
                        nodes.forEach(function(el){
                          var t=(el.innerText||el.textContent||"").trim().toLowerCase();
                          for (var i=0;i<keywords.length;i++){
                            if(t.includes(keywords[i].toLowerCase())){el.style.display="none"; el.setAttribute("data-masafir-hidden","true"); break;}
                          }
                        });
                      }
                      try{hideDangerButtons(); new MutationObserver(function(){hideDangerButtons();})
                        .observe(document.documentElement,{childList:true,subtree:true});}catch(e){}
                    })();
                """.trimIndent()
                webView.evaluateJavascript(jsHideDanger, null)
            }
        }

        // حمّل الموقع
        webView.loadUrl(startUrl)

        // زر الرجوع يتعامل داخل الويب فيو
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })

        // زر اتصال
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivitySafe(intent, "ما قدرش يفتح تطبيق الاتصال")
        }

        // زر واتساب
        btnWhatsapp.setOnClickListener {
            val waUrl = "https://wa.me/$whatsappNumber"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
            startActivitySafe(intent, "ما لقيتش واتساب أو متصفح مناسب")
        }
    }

    private fun handleCustomSchemes(url: String): Boolean {
        return when {
            url.startsWith("tel:", true) -> {
                startActivitySafe(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                true
            }
            url.startsWith("mailto:", true) -> {
                startActivitySafe(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                true
            }
            url.startsWith("whatsapp:", true) ||
            url.contains("wa.me", true) ||
            url.contains("api.whatsapp.com", true) -> {
                startActivitySafe(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                true
            }
            else -> false // خليه يفتح داخل WebView
        }
    }

    private fun startActivitySafe(intent: Intent, noAppMsg: String = "لا يوجد تطبيق مناسب") {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, noAppMsg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
