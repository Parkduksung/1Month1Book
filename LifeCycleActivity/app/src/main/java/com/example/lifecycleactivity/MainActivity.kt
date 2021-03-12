package com.example.lifecycleactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("결과", "MainActivity onCreate")
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        Log.d("결과", "MainActivity onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d("결과", "MainActivity onResume")
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("결과", "MainActivity onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        Log.d("결과", "MainActivity onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("결과", "MainActivity onStop")
        super.onStop()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d("결과", "MainActivity onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onDestroy() {
        Log.d("결과", "MainActivity onDestroy")
        super.onDestroy()
    }
}