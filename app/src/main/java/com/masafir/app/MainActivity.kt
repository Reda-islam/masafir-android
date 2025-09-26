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

    // بدّل بالدومين/الموقع ديالك
    private val startUrl = "https://mellifluous-douhua-9377eb.netlify.app/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)   // layout فيه غير WebView@id/webview

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

        // خلي الروابط الخاصة تفتح بتطبيقاتها
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

                // 1) إضافة زر واتساب جنب أي tel:
                val jsAddWhatsapp = """
                    (function(){
                      function normalize(num){
                        // حيد أي شي ماشي رقم
                        var d = (num||"").replace(/\D/g,'');
                        // إلى بدات بصفر مغربي، قلبها 212
                        if (d.length >= 10 && d[0] === '0') { d = '212' + d.substring(1); }
                        return d;
                      }
                      // متدوزش جوج مرات
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
                      // مرة دابا وراقب تغييرات DOM
                      try { inject();
                        new MutationObserver(inject).observe(document.documentElement,{childList:true,subtree:true});
                      } catch(e) {}
                    })();
                """.trimIndent()
                webView.evaluateJavascript(jsAddWhatsapp, null)

                // 2) خبي "رحلاتي" حتى يضغط على "البحث عن رحلة"
                // ملاحظة: إذا السلكتور ما طاحش، بدّل selectors فالسطرين اللي تحت
                val jsTripsToggle = """
                    (function(){
                      try{
                        var tripsHeader = Array.from(document.querySelectorAll('h1,h2,h3,h4'))
                          .find(function(h){ return (h.innerText||'').trim().includes('رحلاتي'); });
                        if (tripsHeader){
                          var box = tripsHeader.closest('section') || tripsHeader.parentElement;
                          if (box){ box.style.display = 'none'; window.__masafirTripsBox = box; }
                        }
                        var searchBtn = Array.from(document.querySelectorAll('button,a'))
                          .find(function(b){ var t=(b.innerText||'').trim(); return t.includes('البحث عن رحلة'); });
                        if (searchBtn && window.__masafirTripsBox){
                          if (!searchBtn.dataset.masafirClick){
                            searchBtn.dataset.masafirClick = '1';
                            searchBtn.addEventListener('click', function(){
                              try{ window.__masafirTripsBox.style.display = ''; }catch(e){}
                            });
                          }
                        }
                      }catch(e){}
                    })();
                """.trimIndent()
                webView.evaluateJavascript(jsTripsToggle, null)

                // 3) (اختياري) إخفاء أزرار حذف/مسح
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
