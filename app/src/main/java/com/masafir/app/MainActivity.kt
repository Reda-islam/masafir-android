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

        // إذا باغي تستعمل layout فيه WebView (activity_main.xml) عوّض السطور اللي تحت:
        // setContentView(R.layout.activity_main)
        // webView = findViewById(R.id.webview)

        // هنا نخلق WebView بالبرمجة ونعرضها مباشرة
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

                // JavaScript: يخبي أي عنصر لي النص ديالو فيه "حذف" أو "Delete"
                val js = """
                    (function(){
                      function hideByTextKeywords(){
                        var keywords = ['حذف','حَذف','Delete','delete'];
                        var nodes = Array.from(document.querySelectorAll('button, a, [role="button"], input, div, span'));
                        nodes.forEach(function(el){
                          try{
                            var text = (el.textContent || el.innerText || el.value || '').trim();
                            for(var i=0;i<keywords.length;i++){
                              if(text && text.indexOf(keywords[i]) !== -1){
                                el.style.display = 'none';
                                // if parent looks like a card button wrapper, hide parent too
                                if(el.parentElement) el.parentElement.style.display = 'none';
                                break;
                              }
                            }
                          }catch(e){}
                        });
                      }
                      // استعمل الآن وبنفس الوقت راقب DOM
                      hideByTextKeywords();
                      setTimeout(hideByTextKeywords, 300);
                      setTimeout(hideByTextKeywords, 1500);
                      try{
                        var observer = new MutationObserver(function(){ hideByTextKeywords(); });
                        observer.observe(document.body || document.documentElement, { childList:true, subtree:true });
                      }catch(e){}
                    })();
                """.trimIndent()

                view?.evaluateJavascript(js, null)
            }
        }

        webView.webChromeClient = WebChromeClient()

        // حط هنا الدومين/الصفحة ديالك
        webView.loadUrl("https://mellifluous-douhua-9377eb.netlify.app/")

        // Back handling داخل WebView
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
        }
        super.onDestroy()
    }
}
