package com.masafir.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        // إعدادات WebView
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            allowFileAccess = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // إبقاء التصفح داخل WebView
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // سكريبت لإخفاء أزرار الحذف بعد التحميل وأثناء تغيّر الـ DOM
                val js = """
                    (function () {
                      function hideDangerButtons() {
                        var keywords = ["حذف","إزالة","Delete","Supprimer","Effacer"];
                        var nodes = Array.from(document.querySelectorAll('button, a, [role="button"], .btn, .button'));
                        nodes.forEach(function(el){
                          var t = ((el.innerText || el.textContent || "") + "").trim().toLowerCase();
                          for (var i=0; i<keywords.length; i++){
                            if (t.includes(keywords[i].toLowerCase())) {
                              el.style.display = "none";
                              el.setAttribute("data-masafir-hidden","true");
                              break;
                            }
                          }
                        });
                      }

                      hideDangerButtons();

                      // لو الصفحة كتبدّل المحتوى (SPA)، نراقبو تغييرات الـ DOM ونعيد الإخفاء
                      try {
                        var obs = new MutationObserver(function(){ hideDangerButtons(); });
                        obs.observe(document.documentElement, {childList:true, subtree:true});
                      } catch(e) {}
                    })();
                """.trimIndent()

                webView.evaluateJavascript(js, null)
            }

            // خليه يفتح الروابط داخل نفس الويب فيو
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = false
        }

        // لدعم JS dialogs الخ...
        webView.webChromeClient = WebChromeClient()

        // 🔷 دومين نتلايفي ديالك
        webView.loadUrl("https://mellifluous-douhua-9377eb.netlify.app/")

        // رجوع للخلف داخل الويب فيو
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
