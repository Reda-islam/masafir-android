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

        // إنشاء WebView
        webView = WebView(this)
        setContentView(webView)

        // إعدادات WebView
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webChromeClient = WebChromeClient()

        // جلب الكلمات من strings.xml
        val keywords = resources.getStringArray(R.array.delete_keywords)
        val jsArray = keywords.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }

        // WebViewClient مع جافاسكريبت لإخفاء الأزرار
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val jsCode = """
                    (function() {
                        function hideButtons() {
                            var keywords = $jsArray;
                            var elements = document.querySelectorAll("button, a, div");
                            elements.forEach(function(el) {
                                var text = (el.innerText || "").trim();
                                keywords.forEach(function(kw) {
                                    if (text.includes(kw)) {
                                        el.style.display = "none";
                                    }
                                });
                            });
                        }
                        hideButtons();
                        // مراقبة تغييرات الصفحة (SPA)
                        var observer = new MutationObserver(hideButtons);
                        observer.observe(document.body, { childList: true, subtree: true });
                    })();
                """.trimIndent()

                view?.evaluateJavascript(jsCode, null)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }

        // رابط الموقع ديالك
        webView.loadUrl("https://mellifluous-douhua-9377eb.netlify.app")

        // زر الرجوع
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}
