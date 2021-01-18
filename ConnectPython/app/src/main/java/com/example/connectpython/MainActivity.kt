//package com.example.connectpython
//
//import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
//import android.os.AsyncTask
//import android.os.Bundle
//import android.provider.Telephony
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//import androidx.databinding.DataBindingUtil
//import com.example.connectpython.databinding.ActivityMainBinding
//import java.io.DataInputStream
//import java.io.DataOutputStream
//import java.io.IOException
//import java.net.Socket
//import java.net.UnknownHostException
//
//
//class MainActivity : AppCompatActivity() {
//
//    companion object {
//        private const val SERVER_IP = "192.168.55.42"
//        private const val CONNECT_MSG = "connect"
//        private const val STOP_MSG = "stop"
//        private const val BUF_SIZE = 100
//    }
//
//    private var socket: Socket? = null
//    private var dataOutput: DataOutputStream? = null
//    private var dataInput: DataInputStream? = null
//
//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        binding.lifecycleOwner = this
//        setContentView(binding.root)
//        binding.btnSendIp.setOnClickListener {
//            connect()
//        }
//    }
//
//    private fun connect() {
//        Log.w("connect", "연결 하는중");
//// 받아오는거
//        val checkUpdate = Thread {
//            val ip = "192.168.55.42"
//
//            try {
//                socket = Socket(ip, 8080)
//                Log.w("서버 접속됨", "서버 접속됨")
//            } catch (e1: IOException) {
//                Log.w("서버접속못함", "서버접속못함")
//                e1.printStackTrace()
//            }
//            Log.w("edit 넘어가야 할 값 : ", "안드로이드에서 서버로 연결요청")
//
//            try {
//                dataOutput = DataOutputStream(socket?.getOutputStream()); // output에 보낼꺼 넣음
//                dataInput = DataInputStream(socket?.getInputStream()); // input에 받을꺼 넣어짐
//                dataOutput?.writeUTF("안드로이드에서 서버로 연결요청");
//
//            } catch (e: IOException) {
//                e.printStackTrace();
//                Log.w("버퍼", "버퍼생성 잘못됨");
//            }
//            Log.w("버퍼", "버퍼생성 잘됨");
//
//            try {
//                var line = "";
//                var line2 = 0
//                while (true) {
//                    line = dataInput?.readUTF() ?: ""
//                    line2 = dataInput?.read() ?: 0
//                    Log.w("서버에서 받아온 값 ", "" + line);
//                    Log.w("서버에서 받아온 값 ", "" + line2);
//                }
//            } catch (e : Exception) {
//
//            }
//        }
//        checkUpdate.start()
//    }
//}
//
