package com.masafir.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webView)
        val btnCall: Button = findViewById(R.id.btnCall)
        val btnWhatsApp: Button = findViewById(R.id.btnWhatsApp)

        // إعداد WebView
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
         webView.loadUrl("https://www.google.com")// بدّل بالرابط ديالك

        // زر الاتصال
        btnCall.setOnClickListener {
            val phone = "+2126XXXXXXXX" // 🔴 بدّل الرقم هنا
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        }

        // زر واتساب
        btnWhatsApp.setOnClickListener {
            val phone = "2126XXXXXXXX" // 🔴 بدّل الرقم هنا بدون +
            val url = "https://wa.me/$phone"

            try {
                val i = Intent(Intent.ACTION_VIEW)
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                startActivity(i)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "واتساب غير مثبت على الجهاز", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
