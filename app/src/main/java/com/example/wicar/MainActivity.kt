package com.example.wicar

import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket


object SrvAngle {
    var hSrvAngle = 90
    var vSrvAngle = 90
}

class MainActivity : AppCompatActivity() {

    private val ch = Channel<String>(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)

            var srvUrl = sp.getString("RaspiAddr", "")
            var RaspiPort = sp.getString("RaspiPort", "")
            var motionPort = sp.getString("MotionPort", "")

            var motionMonitor=false
        CoroutineScope(Dispatchers.IO).launch {
            val sc = Socket()
            try {

                sc.connect(InetSocketAddress(srvUrl,motionPort!!.toInt()), 5000)
                motionMonitor=  sc.isConnected
                sp.edit().putBoolean("motionOn",motionMonitor).apply()
                Log.e("motion","$motionMonitor")
            } catch (e: Exception) {
                Log.e("e","$e")
            } finally {
                sc.close()
            }
        }








            if (srvUrl != "" && motionPort != "") {
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
                webView.loadUrl("http://$srvUrl:$motionPort/")
            } else
            {
                Toast.makeText(this,"服务器IP或端口没有设置，摄像头未能打开.",Toast.LENGTH_SHORT).show()
            }

        if(srvUrl==""||RaspiPort==""){
            Toast.makeText(this,"服务器IP或端口没有设置，请检查.",Toast.LENGTH_SHORT).show()
        }else
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val os = Socket(srvUrl, RaspiPort!!.toInt()).getOutputStream()

                val pw = PrintWriter(os)
                while (true) {
        
                    pw.write(ch.receive())
                    pw.flush()
                    delay(10)
                }
            } catch (e: Exception) {
            } finally {
            }
        }

        floatingActionButton.setOnClickListener() {
            val intent = Intent()
            intent.setClass(this, SettingsActivity::class.java)
            startActivity(intent)
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
            RepeatListener(300, 50,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.hSrvAngle > 3)
                            SrvAngle.hSrvAngle = SrvAngle.hSrvAngle - 2
                        ch.send("srvo:horiz:${SrvAngle.hSrvAngle}:")
                        delay(5)
                    }
                })
        )
        srvRightButton.setOnTouchListener(
            RepeatListener(300, 50,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.hSrvAngle < 177)
                            SrvAngle.hSrvAngle = SrvAngle.hSrvAngle + 2
                        ch.send("srvo:horiz:${SrvAngle.hSrvAngle}:")
                        delay(5)
                    }
                })
        )
        srvUpButton.setOnTouchListener(
            RepeatListener(300, 50,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.vSrvAngle < 170)
                            SrvAngle.vSrvAngle = SrvAngle.vSrvAngle + 2
                        ch.send("srvo:vertic:${SrvAngle.vSrvAngle}:")
                        delay(5)
                    }
                })
        )
        srvDownButton.setOnTouchListener(
            RepeatListener(300, 50,
                View.OnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (SrvAngle.vSrvAngle > 60)
                            SrvAngle.vSrvAngle = SrvAngle.vSrvAngle - 2
                        ch.send("srvo:vertic:${SrvAngle.vSrvAngle}:")
                        delay(5)
                    }
                })
        )
        srvInitButton.setOnClickListener() {
            CoroutineScope(Dispatchers.IO).launch {
                SrvAngle.vSrvAngle=90
                SrvAngle.hSrvAngle=90
                ch.send("srvo:init:0:")
            }
        }
    }

    private val mListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "motionOn" -> {
                    val b = sharedPreferences.getBoolean(key, false)
                    CoroutineScope(Dispatchers.IO).launch {
                        if (b){
                            ch.send("cmd:sudo motion:0:")
                            delay(2000)
                        }

                        else
                            ch.send("cmd:pkill -KILL motion:0:")
                        delay(2000)
                    }
                }
                "RaspiPW" -> {
                    val b = sharedPreferences.getBoolean(key, false)

                    CoroutineScope(Dispatchers.IO).launch {
                        if (b)
                            ch.send("cmd:sudo halt:0:")

                    }
                }
            }
        }

    override fun onResume() {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .registerOnSharedPreferenceChangeListener(mListener)
        super.onResume()
    }
}