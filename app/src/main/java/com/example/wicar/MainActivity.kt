package com.example.wicar

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.CompoundButton
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
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ch = Channel<String>(0)

        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true // 设置允许访问文件数据
            setSupportZoom(false)
            builtInZoomControls = true
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl("http://192.168.1.166:8081/")
        camSwitch.setOnCheckedChangeListener { _, isChecked ->
            CoroutineScope(Dispatchers.IO).launch {
                if (isChecked) {
                    ch.send("cmd:sudo motion:0:")
                } else {
                    ch.send("cmd:sudo pkill -KILL motion:0:")
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            try {
                val os = Socket("192.168.1.166", 50000).getOutputStream()
                val pw = PrintWriter(os)
                while (true) {
                    pw.write(ch.receive())
                    pw.flush()
                }
            } catch (e: Exception) {
            } finally {

            }
        }

        leftButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        ch.send("car:left:100:")
                    }
                })
        )

        rightButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        ch.send("car:right:100:")
                    }
                })
        )
        forButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        ch.send("car:for:100:")
                    }
                })
        )
        backButton.setOnTouchListener(
            RepeatListener(300, 300,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        ch.send("car:back:100:")
                    }
                })
        )
        srvLeftButton.setOnTouchListener(
            RepeatListener(300, 100,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.hSrvAngle > 3)
                            SrvAngle.hSrvAngle = SrvAngle.hSrvAngle - 3
                        ch.send("srvo:horiz:${SrvAngle.hSrvAngle}:")
                        delay(5)
                    }
                })
        )
        srvRightButton.setOnTouchListener(
            RepeatListener(300, 100,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.hSrvAngle < 177)
                            SrvAngle.hSrvAngle = SrvAngle.hSrvAngle + 3
                        ch.send("srvo:horiz:${SrvAngle.hSrvAngle}:")
                        delay(5)
                    }
                })
        )
        srvUpButton.setOnTouchListener(
            RepeatListener(300, 100,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.vSrvAngle < 170)
                            SrvAngle.vSrvAngle = SrvAngle.vSrvAngle + 1
                        ch.send("srvo:vertic:${SrvAngle.vSrvAngle}:")
                        delay(5)
                    }
                })
        )
        srvDownButton.setOnTouchListener(
            RepeatListener(300, 100,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.vSrvAngle > 60)
                            SrvAngle.vSrvAngle = SrvAngle.vSrvAngle - 1
                        ch.send("srvo:vertic:${SrvAngle.vSrvAngle}:")
                        delay(5)
                    }
                })
        )
        srvInitButton.setOnClickListener() {
            CoroutineScope(Dispatchers.IO).launch {
                ch.send("srv:init:0:")
            }
        }
    }

}