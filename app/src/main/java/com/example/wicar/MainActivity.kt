package com.example.wicar

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.PrintWriter
import java.net.Socket
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ch= Channel<String>()
        val url = "http://v.163.com/paike/V8H1BIE6U/VAG52A1KT.html"

        webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true

            loadWithOverviewMode = true
            allowFileAccess = true
            setSupportZoom(true)
            javaScriptCanOpenWindowsAutomatically = false
        }
        webView.webViewClient=                WebViewClient()
        webView.loadUrl(url)
        CoroutineScope(Dispatchers.IO).launch {

            val os = Socket("192.168.1.2", 50000).getOutputStream()
            val pw = PrintWriter(os)
            while (true){
                pw.write(ch.receive())
                pw.flush()
            }
        }

          leftButton.setOnTouchListener(RepeatListener(300, 300,
              View.OnClickListener {
                CoroutineScope(Dispatchers.IO).launch{
                    ch.send("car:leftMotor:100")
                }
              }))

}




}