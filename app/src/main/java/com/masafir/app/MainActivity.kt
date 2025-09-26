package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // غيّر هذا إلى الدومين/الموقع النهائي ديالك
    private val startUrl = "https://mellifluous-douhua-9377eb.netlify.app/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // layout فيه WebView@id/webview

        webView = findViewById(R.id.webview)

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

        // خلي الروابط الخاصة تفتح بتطبيقاتها (tel/mailto/wa)
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleCustomSchemes(request.url.toString())
            }

            @Deprecated("for old API")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return handleCustomSchemes(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // (1) أضف زر "واتساب" بجانب كل رابط tel:
                val jsAddWhatsapp = """
                    (function(){
                      function normalize(num){
                        var d = (num||"").replace(/\D/g,'');
                        if (d.length >= 10 && d[0] === '0') { d = '212' + d.substring(1); }
                        return d;
                      }
                      if (window.__masafir_wa_injected__) return;
                      window.__masafir_wa_injected__ = true;

                      function inject(){
                        var telLinks = Array.from(document.querySelectorAll('a[href^="tel:"]'));
                        telLinks.forEach(function(a){
                          if (a.dataset.masafirWaAdded === '1') return;
                          var raw = a.getAttribute('href').replace('tel:','');
                          var num = normalize(raw);
                          if (!num) return;
                          var wa = document.createElement('a');
                          wa.href = 'https://wa.me/' + num;
                          wa.textContent = 'واتساب';
                          wa.setAttribute('target','_blank');
                          wa.style.marginInlineStart = '12px';
                          wa.style.padding = '6px 10px';
                          wa.style.border = '1px solid #25D366';
                          wa.style.borderRadius = '8px';
                          wa.style.fontSize = '14px';
                          wa.style.textDecoration = 'none';
                          wa.style.color = '#25D366';
                          a.insertAdjacentElement('afterend', wa);
                          a.dataset.masafirWaAdded = '1';
                        });
                      }
                      try {
                        inject();
                        new MutationObserver(inject).observe(document.documentElement,{childList:true,subtree:true});
                      } catch(e){}
                    })();
                """.trimIndent()
                webView.evaluateJavascript(jsAddWhatsapp, null)

                // (2) إخفاء "رحلاتي" دائمًا، وإظهارها فقط بعد الضغط على زر "بحث/بحت" أسفل فورم البحث
                val jsTripsToggle = """
                  (function(){
                    function findTripsBox(){
                      var header = Array.from(document.querySelectorAll('h1,h2,h3,h4'))
                        .find(function(h){ return ((h.innerText||'').trim().indexOf('رحلاتي') !== -1); });
                      return header ? (header.closest('section') || header.parentElement) : null;
                    }
                    function hideTrips(){
                      var box = findTripsBox();
                      if (box){
                        box.style.display = 'none';
                        window.__masafirTripsBox = box;
                        window.__masafirSearchDone = false;
                      }
                    }
                    function showTrips(){
                      var box = window.__masafirTripsBox || findTripsBox();
                      if (box){
                        box.style.display = '';
                        window.__masafirSearchDone = true;
                        try { box.scrollIntoView({behavior:'smooth', block:'start'}); } catch(e){}
                      }
                    }
                    function isSearchExecuteButton(el){
                      var t = (el.innerText || '').trim();
                      var tokens = ['بحث','بحت','🔎','🔍'];
                      var hasToken = tokens.some(function(tok){ return t.indexOf(tok) !== -1; });
                      if (!hasToken) return false;
                      return t !== 'البحث عن رحلة';
                    }
                    function wireButtons(){
                      Array.from(document.querySelectorAll('button,a,input[type="submit"]')).forEach(function(b){
                        if (!isSearchExecuteButton(b)) return;
                        if (!b.dataset.masafirHook){
                          b.dataset.masafirHook = '1';
                          b.addEventListener('click', function(){ setTimeout(showTrips, 300); });
                        }
                      });
                      var navLabels = ['أنشئ رحلة','انشئ رحلة','إنشاء رحلة','إنشئ رحلة','البحث عن رحلة'];
                      var navBtns = Array.from(document.querySelectorAll('button,a'))
                        .filter(function(b){ var t=(b.innerText||'').trim(); return navLabels.some(function(lbl){ return t.indexOf(lbl) !== -1; }); });
                      navBtns.forEach(function(nb){
                        if (!nb.dataset.masafirNav){
                          nb.dataset.masafirNav = '1';
                          nb.addEventListener('click', function(){ setTimeout(hideTrips, 300); });
                        }
                      });
                    }
                    hideTrips();
                    wireButtons();
                    try {
                      new MutationObserver(function(){
                        wireButtons();
                        var box = findTripsBox();
                        if (box && !window.__masafirSearchDone){
                          box.style.display = 'none';
                          window.__masafirTripsBox = box;
                        }
                      }).observe(document.documentElement,{childList:true,subtree:true});
                    } catch(e){}
                  })();
                """.trimIndent()
                webView.evaluateJavascript(jsTripsToggle, null)

                // (3) اختياري: إخفاء أزرار حذف/مسح
                val jsHideDanger = """
                  (function () {
                    function run(){
                      var words=["إزالة","حذف","Delete","Supprimer","Effacer"];
                      var nodes=Array.from(document.querySelectorAll('button,a,[role="button"],.btn,.button'));
                      nodes.forEach(function(el){
                        var t=(el.innerText||el.textContent||"").trim().toLowerCase();
                        for (var i=0;i<words.length;i++){
                          if (t.includes(words[i].toLowerCase())) { el.style.display='none'; break; }
                        }
                      });
                    }
                    try{ run(); new MutationObserver(run).observe(document.documentElement,{childList:true,subtree:true}); }catch(e){}
                  })();
                """.trimIndent()
                webView.evaluateJavascript(jsHideDanger, null)
            }
        }

        // افتح الموقع
        webView.loadUrl(startUrl)

        // زر الرجوع داخل الويب فيو
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun handleCustomSchemes(url: String): Boolean {
        return when {
            url.startsWith("tel:", true) -> {
                startActivitySafe(Intent(Intent.ACTION_DIAL, Uri.parse(url))); true
            }
            url.startsWith("mailto:", true) -> {
                startActivitySafe(Intent(Intent.ACTION_SENDTO, Uri.parse(url))); true
            }
            url.startsWith("whatsapp:", true) ||
            url.contains("wa.me", true) ||
            url.contains("api.whatsapp.com", true) -> {
                startActivitySafe(Intent(Intent.ACTION_VIEW, Uri.parse(url))); true
            }
            else -> false
        }
    }

    private fun startActivitySafe(intent: Intent, noAppMsg: String = "لا يوجد تطبيق مناسب") {
        try { startActivity(intent) } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, noAppMsg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
