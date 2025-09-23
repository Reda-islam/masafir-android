package com.masafir.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
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

        // WebView settings
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            allowFileAccess = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // سكريبت آمن لإخفاء زرّ "حذف" فقط
                val js = """
                   (function(){
                     function hideDeleteOnly(){
                       var nodes = Array.from(
                         document.querySelectorAll('button, a, [role="button"], input[type="button"], input[type="submit"]')
                       );
                       nodes.forEach(function(el){
                         try{
                           var text = (el.textContent || el.innerText || el.value || '').trim();
                           if (/^(حذف|Delete)$/i.test(text)) {
                             el.style.display = 'none';
                           }
                         }catch(e){}
                       });
                     }
                     hideDeleteOnly();
                     setTimeout(hideDeleteOnly, 300);
                     setTimeout(hideDeleteOnly, 1500);
                     try{
                       var obs = new MutationObserver(hideDeleteOnly);
                       obs.observe(document.body || document.documentElement, {childList:true, subtree:true});
                     }catch(e){}
                   })();
                """.trimIndent()

                view?.evaluateJavascript(js, null)
            }
        }

        webView.webChromeClient = WebChromeClient()

        // 🔷 ضع هنا الدومين ديالك على Netlify
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
