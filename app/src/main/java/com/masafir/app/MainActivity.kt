package com.masafir.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // ✅ رقم واتساب بصيغة دولية بدون +
    private val whatsappNumber = "212600000000"

    // ✅ رابط موقعك
    private val startUrl = "https://mellifluous-douhua-9377eb.netlify.app/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

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

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
            @Deprecated("for old api")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectUiHelpers()
            }
        }

        webView.loadUrl(startUrl)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun injectUiHelpers() {
        val js = """
            (function(){
              // 🟦 helper
              function byText(nodes, txt){
                txt = txt.trim();
                return Array.from(nodes).filter(function(el){
                  var t = (el.innerText||el.textContent||"").trim();
                  return t.indexOf(txt) !== -1;
                });
              }

              // 1) زيد زر واتساب حدّ "اتصال"
              function addWhatsappNextToCall(){
                var callEls = byText(document.querySelectorAll('a,button'), 'اتصال');
                callEls.forEach(function(el){
                  if(el.dataset.masafirWaAdded) return;
                  var wa = document.createElement('a');
                  wa.href = 'https://wa.me/${"$"}{whatsappNumber}';
                  wa.innerText = 'واتساب';
                  wa.style.marginInlineStart = '12px';
                  wa.style.color = '#0a7c62';
                  wa.style.textDecoration = 'none';
                  // خليه يبان حد "اتصال"
                  if(el.parentNode){
                    el.parentNode.insertBefore(wa, el.nextSibling);
                  }
                  el.dataset.masafirWaAdded = '1';
                });
              }

              // 2) خبي "رحلاتي" فالصفحة الرئيسية وبيّنها غير من بعد ما يضغط "البحث عن رحلة"
              function toggleTripsSection(){
                // حاول نلقاو أقرب كونتينر فيه عنوان "رحلاتي"
                var tripsSection = null;
                var candidates = Array.from(document.querySelectorAll('section,div'));
                for (var i=0;i<candidates.length;i++){
                  var t = (candidates[i].innerText||'').trim();
                  if(t.indexOf('رحلاتي') !== -1) { tripsSection = candidates[i]; break; }
                }
                if(!tripsSection) return;

                // واش سبق تفعّلت نتيجة البحث؟
                if(!window.__masafirShowTrips){
                  tripsSection.style.display = 'none';
                } else {
                  tripsSection.style.removeProperty('display');
                }

                // كبسة "البحث عن رحلة"
                var searchBtn = byText(document.querySelectorAll('a,button'), 'البحث عن رحلة')[0];
                if(searchBtn && !searchBtn.dataset.masafirHooked){
                  searchBtn.addEventListener('click', function(){
                    window.__masafirShowTrips = true;
                    tripsSection.style.removeProperty('display');
                  });
                  searchBtn.dataset.masafirHooked = '1';
                }
              }

              // شغّل الدوال لأول مرة
              addWhatsappNextToCall();
              toggleTripsSection();

              // راقب تغييرات SPA باش نعاود نشغّلهم
              try{
                new MutationObserver(function(){
                  addWhatsappNextToCall();
                  toggleTripsSection();
                }).observe(document.documentElement,{subtree:true,childList:true});
              }catch(e){}
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
