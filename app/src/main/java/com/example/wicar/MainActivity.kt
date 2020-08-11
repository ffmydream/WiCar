package com.example.wicar

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.PrintWriter
import java.net.Socket

object SrvAngle {
    var hSrvAngle = 90
    var vSrvAngle = 90
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ch = Channel<String>(0)
        val url = "http://v.163.com/paike/V8H1BIE6U/VAG52A1KT.html"

        webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true

            loadWithOverviewMode = true
            allowFileAccess = true
            setSupportZoom(true)
            javaScriptCanOpenWindowsAutomatically = false
        }
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
        CoroutineScope(Dispatchers.IO).launch {

            val os = Socket("192.168.1.2", 50000).getOutputStream()
            val pw = PrintWriter(os)
            while (true) {
                pw.write(ch.receive())
                pw.flush()
            }
        }

        leftButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        ch.send("car:leftMotor:100")
                    }
                })
        )

        rightButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        ch.send("car:rightMotor:100")
                    }
                })
        )
        forButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        ch.send("car:forMotor:100")
                    }
                })
        )
        backButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        ch.send("car:backMotor:100")
                    }
                })
        )
        srvLeftButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.hSrvAngle > 3)
                            SrvAngle.hSrvAngle = SrvAngle.hSrvAngle - 3
                        ch.send("srv:left:${SrvAngle.hSrvAngle}")
                    }
                })
        )
        srvRightButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.hSrvAngle < 177)
                            SrvAngle.hSrvAngle = SrvAngle.hSrvAngle + 3
                        ch.send("srv:right:${SrvAngle.hSrvAngle}")
                    }
                })
        )
        srvUpButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.vSrvAngle < 170)
                            SrvAngle.vSrvAngle = SrvAngle.vSrvAngle + 3
                        ch.send("srv:up:${SrvAngle.vSrvAngle}")
                    }
                })
        )
        srvDownButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.vSrvAngle > 60)
                            SrvAngle.vSrvAngle = SrvAngle.vSrvAngle - 3
                        ch.send("srv:down:${SrvAngle.vSrvAngle}")
                    }
                })
        )
        srvInitButton.setOnClickListener(){
            CoroutineScope(Dispatchers.IO).launch {
                ch.send("srv:init:0:")
            }
        }
    }


}